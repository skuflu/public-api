package se.centevo.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.rest.webmvc.mapping.DefaultLinkCollector;
import org.springframework.data.rest.webmvc.mapping.LinkCollector;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import jakarta.persistence.EntityManager;
import lombok.SneakyThrows;

@Configuration
@EnableJpaAuditing
class RestConfiguration implements RepositoryRestConfigurer {
    @Autowired
    private EntityManager entityManager;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.setDefaultMediaType(MediaType.APPLICATION_JSON);
        config.useHalAsDefaultJsonMediaType(false);
        config.exposeIdsFor(entityManager.getMetamodel().getEntities().stream().map(e -> e.getJavaType()).collect(Collectors.toList()).toArray(new Class[0]));
    }

    @Override
    public void configureHttpMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.add(new ResourceHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        validatingListener.addValidator("beforeCreate", validator());
        validatingListener.addValidator("beforeSave", validator());
    }

    @Override
    @SneakyThrows
    public LinkCollector customizeLinkCollector(LinkCollector collector) {
        Field entitiesField = collector.getClass().getDeclaredField("entities");
        entitiesField.setAccessible(true);
        var entities = (PersistentEntities) entitiesField.get(collector);

        Field associationLinksField = collector.getClass().getDeclaredField("associationLinks");
        associationLinksField.setAccessible(true);
        var associationLinks = (Associations) associationLinksField.get(collector);

        Field linksField = collector.getClass().getDeclaredField("links");
        linksField.setAccessible(true);
        var links = (SelfLinkProvider) linksField.get(collector);

        return new DefaultLinkCollector(entities, links, associationLinks) {
            @Override
            public Links getLinksForNested(Object object, Links existing) {
                return Links.of();
            }

            
        };
    }

    @Bean
	LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}
    
}