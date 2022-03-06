package de.bitrecycling.springsaml.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSamlSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final String LOGOUT_REQUEST_URL = "/logout";
	private static final String LOGOUT_RESPONSE_URL = "/logout";
	private final RelyingPartyRegistrationRepository myRelyingPartyRegistrationRepository;
//	private final Saml2LogoutRequestResolver myLogoutRequestResolver;
	private final AuthenticationProvider springSamlAuthenticationProvider;
	

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//part 1: secure and anonymous paths
		http.authorizeHttpRequests()
				.mvcMatchers("/").permitAll()
				.antMatchers("/secure*").authenticated();
				
		//part 2: saml details config
		http.saml2Login() //make it saml login
				.relyingPartyRegistrationRepository(myRelyingPartyRegistrationRepository) //make saml config effective
				.authenticationManager(new ProviderManager(springSamlAuthenticationProvider)) // custom auth
				.and()
				.saml2Logout() // activate logout with following properties
				.logoutRequest().logoutUrl(LOGOUT_REQUEST_URL) //endpoint to receive logout request from idp
		;
//				.logoutRequestResolver(myLogoutRequestResolver) //use this to generate logout request towards idp
//				.and()
//				.logoutResponse().logoutUrl(LOGOUT_RESPONSE_URL); // endpoint to receive logout response from idp
	}
}
