package se.centevo.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
@Table(schema = "PublicApi", name = "Transactions")
public class Trans {
    @Id()
    Long transactionId;
    Long parentTransactionId;
    String originId;
    String portfolioCode;
    String accountCode;
    String cashAccountCode;
    String referenceAccountCode;
    String instrumentCode;
    String referenceInstrumentCode;
    String transactionTypeCode;
    LocalDate tradeDate;
    LocalDate settlementDate;
    String currencyCode;
    BigDecimal quantity;
    BigDecimal price;
    BigDecimal interest;
    BigDecimal fxRate;
    BigDecimal portfolioFxRate;
    String brokerCode;
    String sourceCode;
    String transactionText;
    Boolean cancel;
    LocalDate bookDate;
    Integer bookValueOrder;
    Boolean bookValueUncertain;
    BigDecimal bookValue;
    BigDecimal bookValueLocal;
    Boolean fourEyePending;
    String fourEyeApproverCode;
    LocalDate fourEyeDate;

    @MappedCollection(idColumn = "ParentTransactionId")
    Set<TransChildren> children;

    @JsonIgnore
    @InsertOnlyProperty
    String childrenAsJson;

    @LastModifiedDate
    LocalDateTime lastModifiedDate;
    @Version
    Long version;
}

@AllArgsConstructor
@Data
@Table(schema = "PublicApi", name = "TransactionsChildren")
class TransChildren {
    @Id()
    Long transactionId;
    Long parentTransactionId;
    String instrumentCode;
    String currencyCode;
    String transactionTypeCode;
    BigDecimal quantity;
    BigDecimal localTotalAmount;
    BigDecimal fxRate;
    BigDecimal transactionTotalAmount;
    BigDecimal portfolioFxRate;
    BigDecimal portfolioTotalAmount;
}
