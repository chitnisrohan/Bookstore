package com.bookstore.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bookstore.service.impl.UserSecurityService;
import com.bookstore.utility.SecurityUtility;

// when spring boot initializes it will initialize this configuration
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	private Environment env;
	
	@Autowired
	private UserSecurityService userSecurityService;
	
	private BCryptPasswordEncoder passwordEncoder() {
		return SecurityUtility.passwordEncoder();
	}
	
	private static final String[] PUBLIC_MATCHERS = {
			"/css/**",
			"/js/**",
			"/images/**",
			"/",
			"/myAccount"
	};
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
//		meaning - any request in PUBLIC_MATCHERS will be permitAll and for others we need authentication
		http
			.authorizeRequests()
			.antMatchers(PUBLIC_MATCHERS)
			.permitAll()
			.anyRequest()
			.authenticated();

//		disable cross site restriction
		http
			.csrf()
			.disable()
			.cors()
			.disable()
			.formLogin()
			.failureUrl("/login?error")
			.defaultSuccessUrl("/")
			.loginPage("/login")
			.permitAll()
			.and()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.logoutSuccessUrl("/?logout")
			.deleteCookies("remember-me")
			.permitAll()
			.and()
			.rememberMe();
	}

	
//	when user enters login credentials, we are going to invoke userDetailsService to apply userSecurityService and encrypt password before storing in DB
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userSecurityService).passwordEncoder(passwordEncoder());
	}
	
	
}
