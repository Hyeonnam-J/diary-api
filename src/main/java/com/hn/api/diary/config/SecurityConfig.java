package com.hn.api.diary.config;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

import com.hn.api.diary.filter.AccessFilter;
import com.hn.api.diary.repository.MemberRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.hn.api.diary.dto.member.MyUserDetails;
import com.hn.api.diary.entity.Member;
import com.hn.api.diary.exception.AccessDeniedHandler;
import com.hn.api.diary.exception.InvalidValue;
import com.hn.api.diary.filter.SignInFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity(debug = false)
@Configuration
public class SecurityConfig {

    private final MemberRepository memberRepository;

    static final List<String> CLIENTS_IP = Arrays.asList(
            "http://localhost:3000",
            "https://my-diary.life"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedOrigins(CLIENTS_IP);
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<AccessFilter> registration(AccessFilter filter) {
        FilterRegistrationBean<AccessFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/favicon.ico"))
                .requestMatchers(AntPathRequestMatcher.antMatcher("/error"))
                .requestMatchers(AntPathRequestMatcher.antMatcher("/test"))
                .requestMatchers(AntPathRequestMatcher.antMatcher("/profile"))
                .requestMatchers(toH2Console());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((req) ->
                        req
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/user")).hasAnyRole("USER", "ADMIN")
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/admin")).hasRole("ADMIN")

                                .requestMatchers(AntPathRequestMatcher.antMatcher("/signUp/**")).permitAll()
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/freeBoard/**")).permitAll()
                                .anyRequest().authenticated()
                )
                // AccessFilter에서 쿠키로 받은 토큰을 검증하지 않는 로직들은 다음 signInFilter로 넘김.
                // signInFilter에서는 signIn 요청만 처리하고 나머지는 다음 필터로 넘김.
                .addFilterBefore(signInFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(accessFilter(), SignInFilter.class)
                .exceptionHandling(handling -> {
                    handling
                            .accessDeniedHandler(accessDeniedHandler());
                });

        return http.build();
    }

    @Bean
    AccessFilter accessFilter(){
        return new AccessFilter();
    }

    @Bean
    SignInFilter signInFilter(){
        SignInFilter signInFilter = new SignInFilter();
        signInFilter.setAuthenticationManager(authenticationManager());
        return signInFilter;
    }

    @Bean
    AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService());
        return new ProviderManager(provider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    UserDetailsService userDetailsService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                Member member = memberRepository.findByEmail(email)
                        .orElseThrow(InvalidValue::new);

                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(member.getEmail())
                        .password(member.getPassword())
                        .roles(member.getRole())
                        .build();

                return new MyUserDetails(userDetails, member.getId(), member.getNick());
            }
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler();
    }
}
