//package se.centevo.endpoint;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.relational.core.mapping.Table;
//
//import java.math.BigDecimal;
//
//@AllArgsConstructor
//@Data
//@Table(schema = "PublicApi", name = "TransactionsChildren")
//public class TransChildren {
//    @Id
//    Long transactionId;
//    Long parentTransactionId;
//    String instrumentCode;
//    String currencyCode;
//    String transactionTypeCode;
//    BigDecimal quantity;
//    BigDecimal localTotalAmount;
//    BigDecimal fxRate;
//    BigDecimal transactionTotalAmount;
//    BigDecimal portfolioFxRate;
//    BigDecimal portfolioTotalAmount;
//}
