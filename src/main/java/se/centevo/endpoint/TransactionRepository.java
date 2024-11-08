package se.centevo.endpoint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import se.centevo.endpoint.TransWriteRepository.ErrorTransaction;
import se.centevo.jdbc.SQLServerDataTableConverter;

@RepositoryRestResource(path = "transactions")
interface TransactionRepository extends ReadRepository<Transaction, Long> {
    @RestResource(exported = false)
    List<Transaction> findAllByImportReferenceIn(List<String> importReference);
}

@BasePathAwareController
@RequiredArgsConstructor
@Controller
class TransactionBatchController {
   final private TransWriteRepository writeRepository;
   final private TransactionRepository readRepository;

   @Operation(summary = "Insert transactions in batch", responses = {
    @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "207", description = "Multi-Status", useReturnTypeSchema = true),
    @ApiResponse(responseCode = "400", description = "Bad Request", useReturnTypeSchema = true)
    })
   @PostMapping(value = "/transactions/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public @ResponseBody ResponseEntity<TransactionBatchResponse> batch(@RequestBody List<Transaction> transactions,
                                                                    @RequestParam(defaultValue = "false") boolean reducePriceWithInterestRate,
                                                                    @RequestParam(defaultValue = "false") boolean adjustPortfolioAccountStartDate,
                                                                    @RequestParam(defaultValue = "false") boolean checkOriginIdIsUnique) {
        var insertResult = writeRepository.insert(transactions, new TransWriteRepository.InsertProcedureParameters(reducePriceWithInterestRate, adjustPortfolioAccountStartDate, checkOriginIdIsUnique));

        List<Transaction> successful = List.of();
        if(insertResult.successfulTransactions().size() > 0) {
            successful = readRepository.findAllByImportReferenceIn(insertResult.successfulTransactions().stream().map(st -> st.transactionImportReference()).toList());
        }

        List<ProblemDetail> errors = List.of();
        if(insertResult.errorTransactions().size() > 0) {
            errors = insertResult.errorTransactions().stream().map(et -> createProblemDetail(et)).toList();
        }

    HttpStatus status = createHttpStatus(successful.size(), errors.size());
       return ResponseEntity.status(status).body(new TransactionBatchResponse(successful, errors));
   }

   ProblemDetail createProblemDetail(ErrorTransaction errorTransaction) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("");
        problemDetail.setInstance(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problemDetail.setDetail("Object can not be saved before constraint violations have been resolved.");

        var errorMessages = List.of(errorTransaction.errorMessage());// ex.getErrors().getFieldErrors().stream().map(e -> String.valueOf(e.getField()) + " : " + e.getDefaultMessage()).toList();
        problemDetail.setProperties(Map.of("errors", errorMessages, "resource", errorTransaction.transaction()));

        return problemDetail;
   }

   HttpStatus createHttpStatus(int successful, int errors) {
        HttpStatus status = HttpStatus.OK;
        if(successful > 0 && errors > 0)
            status = HttpStatus.MULTI_STATUS;
        else if (successful == 0 && errors > 0) 
            status = HttpStatus.BAD_REQUEST;
        else if (successful > 0 && errors == 0) 
            status = HttpStatus.OK;
        return status;
   }

   record TransactionBatchResponse(List<Transaction> created, List<ProblemDetail> errors) {}
}

@BasePathAwareController
@RequiredArgsConstructor
@Controller
class TransactionController {
   final private TransWriteRepository writeRepository;
   final private TransactionRepository readRepository;


   @DeleteMapping(value = "/transactions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
   public @ResponseBody ResponseEntity<Transaction> cancel(@PathVariable Long id) {
        writeRepository.cancel(id);
        return  ResponseEntity.ok().body(readRepository.findById(id).get());
   }
}

@Repository
@RequiredArgsConstructor
class TransWriteRepository {
   final private JdbcClient jdbcClient;

   record InsertProcedureParameters(boolean reducePriceWithInterestRate, boolean adjustPortfolioAccountStartDate, boolean checkOriginIdIsUnique) {}

   TransactionOutcome insert(List<Transaction> transactions, InsertProcedureParameters procedureParameters) {

        for(var transaction : transactions) {
            try {
                transaction.setChildrenAsJson(new ObjectMapper().writeValueAsString(transaction.getChildren()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        var insertTransactionsParameter = new SQLServerDataTableConverter<Transaction>(jdbcClient, "PublicApi.InsertTransactionsParameter", transactions).convert();
        
        List<ErrorTransaction> errorTransactions = new ArrayList<>();
        List<SuccessfulTransaction> successfulTransactions = new ArrayList<>();
        jdbcClient.sql("{call PublicApi.spInsertTransactions(:InsertTransactionsParameter)}")
        .param("InsertTransactionsParameter", insertTransactionsParameter)
        .param("ReducePriceWithInterestRate", procedureParameters.reducePriceWithInterestRate)
        .param("AdjustPortfolioAccountStartDate", procedureParameters.adjustPortfolioAccountStartDate)
        .param("CheckOriginIdIsUnique", procedureParameters.checkOriginIdIsUnique)
        .query((RowCallbackHandler) rch -> {
            var transaction = transactions.get(rch.getInt("InsertReferenceId"));
            String validationErrorMessage = rch.getString("ValidationErrorMessage");
            if(validationErrorMessage != null) {
                errorTransactions.add(new ErrorTransaction(transaction, validationErrorMessage));
            } else {
                successfulTransactions.add(new SuccessfulTransaction(transaction, rch.getString("TransactionImportReference")));
            }
        });

        return new TransactionOutcome(successfulTransactions, errorTransactions);
   }

   record TransactionOutcome(List<SuccessfulTransaction> successfulTransactions, List<ErrorTransaction> errorTransactions) {}

   record ErrorTransaction(Transaction transaction, String errorMessage) {}
   
   record SuccessfulTransaction(Transaction transaction, String transactionImportReference) {}

   void cancel(Long transactionId) {
    jdbcClient.sql("""
        UPDATE Core.Transactions
            SET Cancel = 1
        WHERE TransactionId = :transactionId
    """)
    .param("transactionId", transactionId)
    .update();
   }
}

@Entity(name = "Transactions")
@Getter @Setter
class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long transactionId;
    Long parentTransactionId;
    String originId;

    @NotNull
    @Size(min = 1, max = 50)
    String portfolioCode;

    @NotNull
    @Size(min = 1, max = 50)
    String accountCode;

    @Size(min = 1, max = 50)
    String cashAccountCode;

    @Size(min = 1, max = 50)
    String referenceAccountCode;

    @NotNull
    @Size(min = 1, max = 50)
    String instrumentCode;

    @Size(min = 1, max = 50)
    String referenceInstrumentCode;

    @NotNull
    @Size(min = 1, max = 50)
    String transactionTypeCode;

    LocalDate tradeDate;
    LocalDate settlementDate;

    @Size(min = 1, max = 50)
    String currencyCode;

    @Digits(integer = 22, fraction = 9)
    BigDecimal quantity;

    @Digits(integer = 16, fraction = 9)
    @Positive
    BigDecimal price;

    @Digits(integer = 22, fraction = 9)
    BigDecimal interest;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(insertable = false, updatable = false)
    BigDecimal localTotalAmount;

    @Digits(integer = 16, fraction = 9)
    @Positive
    BigDecimal fxRate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(insertable = false, updatable = false)
    BigDecimal transactionTotalAmount;

    @Digits(integer = 16, fraction = 9)
    @Positive
    BigDecimal portfolioFxRate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(insertable = false, updatable = false)
    BigDecimal portfolioTotalAmount;

    @NotNull
    @Size(min = 1, max = 50)
    String brokerCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String sourceCode;

    @Size(min = 1, max = 512)
    String transactionText;

    Boolean cancel;
    LocalDate bookDate;
    Integer bookValueOrder;
    Boolean bookValueUncertain;

    @Digits(integer = 19, fraction = 2)
    @Positive
    BigDecimal bookValue;

    @Digits(integer = 19, fraction = 2)
    @Positive
    BigDecimal bookValueLocal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(insertable = false, updatable = false)
    Boolean fourEyePending;

    @Size(min = 1, max = 50)
    String fourEyeApproverCode;
    LocalDate fourEyeDate;

    @OneToMany(mappedBy = "parentTransactionId")
    @Column(insertable = false, updatable = false)
    Set<TransactionChild> children;

    @JsonIgnore
    String childrenAsJson;

    String importReference;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(insertable = false, updatable = false)
    Boolean preliminary;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer portfolioOrderId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer cancelTransactionId;

    @NotNull
    LocalDateTime createDate;

    LocalDateTime updateDate;

    @LastModifiedDate
    @Formula("COALESCE(UpdateDate, CreateDate)")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    LocalDateTime lastModifiedDate;

    @Version
    @Column(name="VersionId", insertable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long versionId;
}


@Entity
@Table(name = "TransactionsChildren")
@Getter @Setter
class TransactionChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long transactionId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long parentTransactionId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String instrumentCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    String currencyCode;

    @NotNull
    @Size(min = 1, max = 50)
    String transactionTypeCode;

    @NotNull
    @Digits(integer = 22, fraction = 9)
    BigDecimal quantity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    BigDecimal localTotalAmount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    BigDecimal fxRate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    BigDecimal transactionTotalAmount;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    BigDecimal portfolioFxRate;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    BigDecimal portfolioTotalAmount;
}
