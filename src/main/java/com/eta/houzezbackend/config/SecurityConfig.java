package com.eta.houzezbackend.config;

import com.eta.houzezbackend.auth.AgentDetailService;
import com.eta.houzezbackend.auth.JwtAuthenticationEntryPoint;
import com.eta.houzezbackend.filter.JwtUsernameAndPasswordAuthenticationFilter;
import com.eta.houzezbackend.service.JwtService;
import com.eta.houzezbackend.util.SystemParam;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.SecretKey;
import java.util.List;

@Setter
@ConfigurationProperties(prefix = "cors")
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] AUTH_URL_WHITELIST = {
            "/agents",
            "/agents/sign-in"
    };
    private final AgentDetailService agentDetailService;
    private final JwtService jwtService;
    private final SystemParam systemParam;

    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .cors().configurationSource(request -> {
                    var cors = new CorsConfiguration();
                    cors.setAllowedOrigins(allowedOrigins);
                    cors.setAllowedMethods(allowedMethods);
                    cors.setAllowedHeaders(allowedHeaders);
                    return cors;
                })
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(authorize ->
                        authorize.antMatchers(AUTH_URL_WHITELIST).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilter(
                        new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(),
                                jwtService,
                                systemParam))
                //.addFilterAfter(new JwtTokenVerifier(secretKey, jwtConfig), JwtUsernameAndPasswordAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .and().build();

    }

    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        provider.setUserDetailsService(agentDetailService);
        return new ProviderManager(provider);
    }
}
