package se.centevo.config;

import java.util.ArrayList;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
class OpenApiConfig implements WebMvcConfigurer {
	@Override
	public void addViewControllers(@NonNull final ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/swagger-ui.html");
		registry.addRedirectViewController("/swagger-ui", "/swagger-ui.html");
	}
}

@Configuration
class RequestLoggingConfiguration {
	@Bean
	CommonsRequestLoggingFilter logFilter() {
		CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter() {
			@Override
			protected void afterRequest(@NonNull HttpServletRequest request, @NonNull String message) {
			}
		};
		loggingFilter.setIncludeQueryString(true);
		loggingFilter.setIncludePayload(true);
		loggingFilter.setMaxPayloadLength(10000);
		loggingFilter.setIncludeHeaders(true);
		loggingFilter.setHeaderPredicate(header -> !header.toLowerCase().equals("authorization"));
		loggingFilter.setBeforeMessagePrefix("Incoming request [");
		return loggingFilter;
	}
}

@Configuration
class ActuatorConfiguration {
	@Bean
	HttpExchangeRepository httpTraceRepository() {
		InMemoryHttpExchangeRepository repository = new InMemoryHttpExchangeRepository();
		repository.setCapacity(100);
		return repository;
	}
}

@Configuration
class SpringdocConfig {

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement()
						.addList("basicAuth"))
				.components(new Components()
						.addSecuritySchemes(
								"basicAuth", new SecurityScheme()
										.name("basicAuth")
										.type(SecurityScheme.Type.HTTP)
										.scheme("basic"))

				)
				.info(new Info().version("2.0.0").description(
						"""
								#### Data objects
								Data objects are divided into two categories, *first* and *second* class objects.
								The *first* class objects are the ones managed with the API. They are fully updateable and supports all HTTP methods.
								The *second* class objects are value objects, the only reason for them to exist is to construct the First class objects. That is why they only support HTTP Method **GET**.

								Note:

								* Batch endpoints (/batch) return a response in a different format with a list of successful payloads and payloads returning errors.
								If there is a mix of successful and unsuccesful insertions the method will return status 407 Multi-status (https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/207).
								* If-Modified-Since header value can be used to limit data retrieval to only get the new and modified objects (https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/If-Modified-Since).
								* All data objects can be filtered on their fields values. Add the field as a request parameter and the filter will be applied. Filter on multiple values of the same field are supported.
								If the field is of type date, the api will return objects mathcing the date. If two values of a date field is provided, the api will return all objects with a value between the dates (inclusive).
								* Errors are returned accordingly to Problem Details specification (https://datatracker.ietf.org/doc/html/rfc7807).
								**The error message contains a fields named trace-id and timestamp and both should be included in the communication to Centevo when raising a support ticket.**

								#### File objects
								File objects can be downloaded using their /download endpoint.

								#### Data lists
								Data lists are calculaded data like Assets/Debts, Time-weighted return, Profit/Loss. The data are always accessed with HTTP method **GET**.
								"""));
	}

	@Bean
	OperationCustomizer customizeOperation() {
		return (operation, handlerMethod) -> operation
				.responses(
						operation
								.getResponses()
								.addApiResponse("401", new ApiResponse().description("Unauthenticated"))
								.addApiResponse("403", new ApiResponse().description("Unauthorized")));
	}

	@Bean
	OpenApiCustomizer consumerTypeHeaderOpenAPICustomizer() {
		return openApi -> {
			openApi.getPaths().entrySet().removeIf(path -> path.getValue().readOperations().stream().anyMatch(
					operation -> operation.getTags().stream()
							.anyMatch(tag -> tag.endsWith("property-reference-controller"))));

			openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
					.forEach(operation -> {
						String tagName = operation.getTags().get(0);
						if (tagName.endsWith("entity-controller")) {
							String entityName = tagName.substring(0, tagName.length() - 18);
							operation.getTags().set(0, entityName);
						}
					});

			openApi.getPaths().values().forEach(
					pathItem -> {
						var operationList = new ArrayList<Operation>();
						if (pathItem.getPost() != null)
							operationList.add(pathItem.getPost());
						if (pathItem.getPatch() != null)
							operationList.add(pathItem.getPatch());
						if (pathItem.getPut() != null)
							operationList.add(pathItem.getPut());

						operationList.forEach(
								operation -> {
									ResolvedSchema errResSchema = ModelConverters.getInstance()
											.resolveAsResolvedSchema(new AnnotatedType(ProblemDetail.class));
									Content content = new Content().addMediaType("*/*",
											new MediaType().schema(errResSchema.schema));

									operation.getResponses().addApiResponse("400", new ApiResponse()
											.description(HttpStatus.BAD_REQUEST.getReasonPhrase()).content(content));

								}

						);
						pathItem.readOperations().forEach(operation -> {
							operation.getResponses().addApiResponse(
									Integer.valueOf(HttpStatus.UNAUTHORIZED.value()).toString(),
									new ApiResponse().description(HttpStatus.UNAUTHORIZED.getReasonPhrase()));
						});
					});

		};
	}

}
