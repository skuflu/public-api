//package se.centevo.endpoint;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.rest.webmvc.BasePathAwareController;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
//import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
//import org.springframework.stereotype.Repository;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import java.util.List;
//
//@BasePathAwareController
//@RequiredArgsConstructor
//public class TransController {
//    final private TransWriteRepository writeRepository;
//
//    @PostMapping(value = "/transactions/batch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public @ResponseBody ResponseEntity<Iterable<Trans>> batch(@RequestBody List<Trans> transactions,
//                                                                     @RequestParam(defaultValue = "false") boolean useCairoPrice,
//                                                                     @RequestParam(defaultValue = "false") boolean useCairoPriceMinusInterestRate,
//                                                                     @RequestParam(defaultValue = "false") boolean useCairoFxRate,
//                                                                     @RequestParam(defaultValue = "false") boolean useCairoPortfolioFxRate,
//                                                                     @RequestParam(defaultValue = "false") boolean adjustPortfolioAccountStartDate,
//                                                                     @RequestParam(defaultValue = "false") boolean useUniqueOriginId) {
//        return ResponseEntity.ok(writeRepository.insert(transactions));
//    }
//}
//
//@Repository
//@RequiredArgsConstructor
//class TransWriteRepository {
//    final private TransReadRepository readRepository;
//    final private JdbcTemplate jdbcTemplate;
//
//    Iterable<Trans> insert(List<Trans> transactions) {
//
//        for(var transaction : transactions) {
//            try {
//                transaction.setChildrenAsJson(new ObjectMapper().writeValueAsString(transaction.getChildren()));
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        var insertTransactions = new SimpleJdbcInsert(jdbcTemplate).withTableName("Transactions").withSchemaName("PublicApi");
//
//        int[] ids = insertTransactions.executeBatch(SqlParameterSourceUtils.createBatch(transactions));
//        List<String> originIds = transactions.stream().map(Trans::getOriginId).toList();
//        return readRepository.findAllById(List.of(4323066L));
//    }
//}
//
