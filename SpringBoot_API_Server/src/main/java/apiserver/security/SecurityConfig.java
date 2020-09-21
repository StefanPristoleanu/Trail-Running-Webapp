package apiserver.security;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

/** @author */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  // private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  // @Autowired private UserDetailsServiceImpl userDetailsService;
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // Override the http Cross-Origin Resource Sharing configuration in order to expose the header
    // with the authentication bearer
    http.cors()
        .configurationSource(
            (HttpServletRequest request) -> {
              CorsConfiguration config = new CorsConfiguration();
              config.setAllowedHeaders(Collections.singletonList("*"));
              config.setAllowedMethods(Collections.singletonList("*"));
              config.addAllowedOrigin("*");
              config.setAllowCredentials(true);
              config.addExposedHeader("authorization");
              return config;
            });
    http.csrf()
        .disable()
        // make sure we use stateless session; session won't be used to store user's state:
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        // permit all access to /rest-jpa/ for speed test and also for register and login api:
        .antMatchers(
            "/api-server/user/register",
            "/api-server/user/login",
            "/api-server/trail/find-all",
            "/v2/api-docs/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/swagger-ui.html**",
            "/swagger-ui/**")
        .permitAll()
        // all other api methods must be authenticated:
        .anyRequest()
        .authenticated()
        .and()
        // add filter for all other requests to check the presence of JWT in header:
        .addFilterBefore(new AuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }

  // @Autowired
  // public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
  // PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  // System.out.println("~~~~~ pass: " + passwordEncoder.encode("{SHA-256}222"));
  /*auth.inMemoryAuthentication()
  .passwordEncoder(passwordEncoder)
  .withUser("test").password("{noop}T1234567").roles("USER")
  .and()
  //.passwordEncoder(passwordEncoder)
  .withUser("test2").password("{noop}222").roles("USER");
   */
  // or:
  // auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  // }
}
