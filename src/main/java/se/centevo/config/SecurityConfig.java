package se.centevo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
		InMemoryUserDetailsManager userDetailsManager() {

			var builder = User.builder().passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder()::encode);

			var centevo = builder.username("centevo").password("centevo").roles("USER").build();
			var peak = builder.username("peak").password("peak").roles("USER").build();
            var norcap = builder.username("norcap").password("norcap").roles("USER").build();

			return new InMemoryUserDetailsManager(centevo, peak, norcap);
		}

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests((authorize) -> authorize
                            .anyRequest().authenticated()
                    )
                    .httpBasic(Customizer.withDefaults())
                    .csrf((csrf) -> csrf.disable())
                    .formLogin(Customizer.withDefaults());
            return http.build();
        }
}
