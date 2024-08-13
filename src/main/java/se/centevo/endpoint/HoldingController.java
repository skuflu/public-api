package se.centevo.endpoint;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class HoldingController {
    final private HoldingRepository repository;

    @GetMapping(path = "/holdings", produces = "application/json")
    public List<Map<String, Object>> getMethodName(@RequestBody HoldingRequest parameters) {
        return repository.holdings(parameters);
    }
    
}
record HoldingRequest(Long customerId, Long portfolioId, LocalDate fromDate, LocalDate toDate) {}

@Repository
@RequiredArgsConstructor
class HoldingRepository {
    final private JdbcClient jdbcClient;

    List<Map<String, Object>> holdings(HoldingRequest parameters) {
        return jdbcClient.sql("""
            EXEC Core.spHoldingGet @CustomerId=:customerId, @PortfolioId=:portfolioId, @FromDate=:fromDate, @ToDate=:toDate
        """).paramSource(parameters).query().listOfRows();
    }
}

