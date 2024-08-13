package se.centevo.endpoint;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "transactions")
public interface TransReadRepository extends PagingAndSortingRepository<Trans, Long> {
}
