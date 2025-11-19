package de.brianp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.requestMatchers("/", "/error", "/webjars/**").permitAll()
                .requestMatchers("/login-success.html", "/login-failure.html", "/calendar.html").permitAll()
                .requestMatchers("/api/auth/**").permitAll().requestMatchers("/api/calendar/status").permitAll()

                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.loginPage("/oauth2/authorization/google")
                        .defaultSuccessUrl("/login-success.html", true).failureUrl("/login-failure.html"))
                .logout(logout -> logout.logoutSuccessUrl("/").invalidateHttpSession(true).clearAuthentication(true))
                .csrf(csrf -> csrf.disable()).headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }
}
