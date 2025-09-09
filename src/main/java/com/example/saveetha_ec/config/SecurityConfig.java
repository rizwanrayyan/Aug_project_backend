package com.example.saveetha_ec.config;


import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.saveetha_ec.service.MyUserDetailService;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	MyUserDetailService myUserDetailService;
	@Autowired
	private JwtFilter jwtFilter;
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(customizer->customizer.disable())
				.formLogin(form -> form.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(request->request
				.requestMatchers("/api/user/**").permitAll()
				 .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
				.requestMatchers("/api/verify/send").permitAll()
				.requestMatchers("/api/verify/verify").permitAll()
				.requestMatchers("/api/gold/verify").permitAll()
				.requestMatchers("/api/gold/buy").permitAll()
				.requestMatchers("/api/verify/**").hasAuthority("ROLE_USER")
				)
				.sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider()) 
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).
				build();
	}
	@SuppressWarnings("deprecation")
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
		provider.setUserDetailsService(myUserDetailService);
		provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
		return provider;
	}
	@Bean
	public AuthenticationManager manager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://127.0.0.1:5500","http://localhost:5173")); // your frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}

