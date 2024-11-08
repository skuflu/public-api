package se.centevo.endpoint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;

@NoRepositoryBean
interface BaseRepository<T, ID> extends QuerydslBinderCustomizer<EntityPath<T>> {
    
    @Override
    default void customize(@NonNull QuerydslBindings bindings, @NonNull EntityPath<T> entityPathBase) {
        bindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            final List<? extends String> pathValues = new ArrayList<>(values);

            if(pathValues.size() == 1) {
                return Optional.ofNullable(path.like(pathValues.get(0).replace("*", "%")));
            } else {
                BooleanBuilder builder = new BooleanBuilder();
                Iterator<? extends String> iterator = values.iterator();
                while (iterator.hasNext()) {
                    builder.or(path.like(iterator.next().replace("*", "%")));
                }
                return Optional.of(builder);
            }
        });

        bindings.bind(LocalDate.class)
                .all((final DatePath<java.time.LocalDate> path, final Collection<? extends LocalDate> values) -> {
                    final List<? extends LocalDate> dates = new ArrayList<>(values);
                    if (dates.size() == 1) {
                        return Optional.ofNullable(path.eq(dates.get(0)));
                    } else if (dates.size() == 2) {
                        Collections.sort(dates);
                        return Optional.ofNullable(path.between(dates.get(0), dates.get(1)));
                    }
                    throw new IllegalArgumentException(
                            "2 date params(from & to) expected for:" + path + " found:" + values);
                });

        bindings.bind(LocalDateTime.class).all((final DateTimePath<java.time.LocalDateTime> path,
                final Collection<? extends LocalDateTime> values) -> {
            final List<? extends LocalDateTime> dates = new ArrayList<>(values);
            if (dates.size() == 1) {
                return Optional.ofNullable(path.eq(dates.get(0)));
            } else if (dates.size() == 2) {
                Collections.sort(dates);
                return Optional.ofNullable(path.between(dates.get(0), dates.get(1)));
            }
            throw new IllegalArgumentException("2 date params(from & to) expected for:" + path + " found:" + values);
        });
    }
}

@NoRepositoryBean
interface EditableRepository<T, ID> extends BaseRepository<T, ID>, JpaRepository<T, ID> {

}

@NoRepositoryBean
interface ReadRepository<T, ID> extends BaseRepository<T, ID>, PagingAndSortingRepository<T, ID>,
        QuerydslPredicateExecutor<T> {
        public Iterable<T> findAll();

        public Iterable<T> findAllById(Iterable<ID> ids);
    
        public Optional<T> findById(ID id);
}
