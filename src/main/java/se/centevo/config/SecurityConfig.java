package se.centevo.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
class SecurityConfig {
    private final BasicAuthUserConfiguration basicAuthUserConfiguration;
    private final AdminProperties adminProperties;

    @Bean
    InMemoryUserDetailsManager userDetailsManager() {

        var builder = User.builder().passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode);
        var userDetailsList = new ArrayList<UserDetails>();
        userDetailsList.addAll(basicAuthUserConfiguration.users.stream().map(u -> builder.username(u.username()).password(u.password()).roles("USER").build()).toList());
        if(adminProperties.getUsername() != null && adminProperties.getPassword() != null)
        userDetailsList.add(builder.username(adminProperties.getUsername()).password(adminProperties.getPassword()).roles("ADMIN").build());

        return new InMemoryUserDetailsManager(userDetailsList);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request.requestMatchers("/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/").permitAll()
                    .requestMatchers("/actuator/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf((csrf) -> csrf.disable())
            .formLogin(Customizer.withDefaults());
        return http.build();
    }
}

@Component
@ConfigurationProperties(prefix = "tenants.security")
@RequiredArgsConstructor
class BasicAuthUserConfiguration {
    private final TenantDataSourceProperties tenantDataSourceProperties;

    record User(String username, String password) {};

    List<User> users = new ArrayList<>();

    void setUsers(Map<String, Map<String, String>> users) {
        users.forEach((key, value) -> {
            if(tenantDataSourceProperties.getDatasources().containsKey(key))
                this.users.add(new User(key, value.get("password")));
        });
    }
}

@Configuration
@ConfigurationProperties(prefix = "security.admin")
@Getter
@Setter
class AdminProperties {
    private String username;
    private String password;
}