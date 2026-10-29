package com.Gerdau.Desafio_GC_Gerdau_App.config;

import java.util.LinkedHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder paths = PathPatternRequestMatcher.withDefaults();
        OrRequestMatcher rotasDeApi = new OrRequestMatcher(paths.matcher("/api/**"), paths.matcher("/servicos/**"));

        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/de-para/confirmacoes").hasAnyRole("COMPRADOR", "APROVADOR")
                        .requestMatchers("/ui/revisao/**", "/ui/aprovacao/**", "/ui/consultas/**", "/api/de-para/**").hasRole("APROVADOR")
                        .requestMatchers("/api/importacoes/aprovacao/**", "/ui/importacoes/aprovacao/**").hasRole("APROVADOR")
                        .requestMatchers("/api/importacoes/**", "/ui/importacoes/**").hasAnyRole("COMPRADOR", "APROVADOR")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error"))
                .logout(l -> l.logoutSuccessUrl("/login?logout"))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint(rotasDeApi)));
        return http.build();
    }

    /** A API responde 401; qualquer outra rota sem login (inclusive sem header Accept) vai para a tela de login. */
    private AuthenticationEntryPoint entryPoint(RequestMatcher rotasDeApi) {
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> porRota = new LinkedHashMap<>();
        porRota.put(rotasDeApi, new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
        DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(porRota);
        entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
        return entryPoint;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
