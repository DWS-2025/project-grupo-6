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
					
					//---------------------------------------------------------------------

					// TODO ESTO SE TIENE QUE REVISAR PARA QUE SEA CORRECTO 

					//---------------------------------------------------------------------

					// PUBLIC API ENDPOINTS
					.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
					.requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
					.requestMatchers( "/api/auth/**").permitAll()
					
					// USER API ENDPOINTS (authenticated users)
					.requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/users/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.POST, "/api/reviews/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.POST, "/api/orders/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/api/orders/**").hasAnyRole("USER", "ADMIN")
					
					// ADMIN ONLY API ENDPOINTS
					.requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
					.requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
					.requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAnyRole("USER", "ADMIN") // Backend handles specific permissions
					.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
					
					// Default: require authentication for any other API endpoint
					.anyRequest().authenticated()
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
						// STATIC RESOURCES - Always public
						.requestMatchers("/", "/login", "/register", "/loginerror").permitAll()
						.requestMatchers("/css/**", "/js/**", "/Images/**", "/images/**", "/uploads/**", "/image/**").permitAll()
						.requestMatchers("/error").permitAll()
						
						// PUBLIC PRODUCT PAGES - Reading only
						.requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/search-products").permitAll()
						.requestMatchers(HttpMethod.GET, "/search-products-json").permitAll()
						.requestMatchers(HttpMethod.GET, "/product-details").permitAll()
						.requestMatchers(HttpMethod.GET, "/download-return-policy/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/get-cart-quantity").permitAll()
						
						// CART AND ORDER ENDPOINTS - User authenticated
						.requestMatchers("/add-to-order", "/remove-from-order", "/order", "/cart-prompt").hasAnyRole("USER", "ADMIN")
						
						// REVIEW ENDPOINTS - User authenticated
						.requestMatchers(HttpMethod.POST, "/add-review").hasAnyRole("USER", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/delete-review").hasAnyRole("USER", "ADMIN") // Backend handles specific permissions
						
						// USER PROFILE ENDPOINTS
						.requestMatchers("/profile", "/edit-profile", "/edit-profile/**").hasAnyRole("USER", "ADMIN")
						
						// ADMIN ONLY ENDPOINTS - Product management
						.requestMatchers("/create-product", "/add-product").hasRole("ADMIN")
						.requestMatchers("/edit-product/**", "/update-product").hasRole("ADMIN")
						.requestMatchers("/delete-product", "/delete-product/**").hasRole("ADMIN")
						.requestMatchers("/product-management").hasRole("ADMIN")
						
						// ADMIN ONLY ENDPOINTS - User management
						.requestMatchers("/users-management", "/users-management/**").hasRole("ADMIN")
						.requestMatchers("/delete-user/**").hasRole("ADMIN")
						
						// ADMIN ONLY ENDPOINTS - System management
						.requestMatchers("/review-management", "/review-management/**").hasRole("ADMIN")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						
						// Default: require authentication for any other web endpoint
						.anyRequest().authenticated()
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
