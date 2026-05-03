package org.abdrafikov.groupbuy.config;

import org.abdrafikov.groupbuy.service.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String REMEMBER_ME_KEY = "groupbuy-remember-me-key";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService customUserDetailsService
    ) throws Exception {
        PathPatternRequestMatcher apiRequestMatcher = PathPatternRequestMatcher.withDefaults().matcher("/api/**");

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(apiRequestMatcher)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/login",
                                "/auth/register",
                                "/css/**",
                                "/error",
                                "/js/**",
                                "/images/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                apiRequestMatcher
                        )
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/workspaces", true)
                        .failureUrl("/auth/login?error")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key(REMEMBER_ME_KEY)
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                        .userDetailsService(customUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
