package es.xpressaly.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import es.xpressaly.security.jwt.JwtRequestFilter;
import es.xpressaly.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	RepositoryUserDetailsService userDetailsService;

	@Autowired
	private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		
		http.authenticationProvider(authenticationProvider());
		
		http
			.securityMatcher("/api/**")
			.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));
		
		http
			.authorizeHttpRequests(authorize -> authorize
                    // PRIVATE ENDPOINTS
					.requestMatchers(HttpMethod.GET, "/api/users/").hasRole("USER")
                    .requestMatchers(HttpMethod.POST,"/api/products/").hasRole("USER")
                    .requestMatchers(HttpMethod.PUT,"/api/products/**").hasRole("USER")
                    .requestMatchers(HttpMethod.DELETE,"/api/products/**").hasRole("ADMIN")
					.requestMatchers(HttpMethod.DELETE,"/api/users/**").hasRole("ADMIN")
					// PUBLIC ENDPOINTS
					.anyRequest().permitAll()
			);
		
        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
		
		// Require HTTPS for all API requests
		http.requiresChannel(channel -> channel.anyRequest().requiresSecure());

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

		http.authenticationProvider(authenticationProvider());

		http
				.authorizeHttpRequests(authorize -> authorize
						// PUBLIC PAGES
						.requestMatchers("/").permitAll()
						.requestMatchers("/images/**").permitAll()
						.requestMatchers("/css/**").permitAll()
						.requestMatchers("/js/**").permitAll()
						.requestMatchers("/products/**").permitAll()
						.requestMatchers("/error").permitAll()
						.requestMatchers("/product-details/**").permitAll()
						.requestMatchers("/cart-prompt").permitAll()
						// PRIVATE PAGES
						.requestMatchers("/profile").hasAnyRole("USER","ADMIN")
						.requestMatchers("/create-product").hasRole("ADMIN")
						.requestMatchers("/add-product").hasRole("ADMIN")
						.requestMatchers("/edit-profile").hasAnyRole("USER","ADMIN")
						.requestMatchers("/edit-profile/**").hasAnyRole("USER","ADMIN")
						.requestMatchers("/delete-product/**").hasRole("ADMIN")
						.requestMatchers("/users-management").hasRole("ADMIN")
						.requestMatchers("/product-management").hasRole("ADMIN")
						.requestMatchers("/review-management").hasRole("ADMIN")
						.anyRequest().permitAll()
				)
				.formLogin(formLogin -> formLogin
						.loginPage("/login")
						.usernameParameter("email")
            			.passwordParameter("password")
						.failureUrl("/loginerror")
						.defaultSuccessUrl("/products")
						.permitAll()
				)
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.permitAll()
				)
				.csrf(csrf->csrf.disable());
				
		// Require HTTPS for all web requests
		http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
				
		return http.build();
	}
}
