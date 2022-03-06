package de.bitrecycling.springsaml.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSamlSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final String LOGOUT_REQUEST_URL = "/logout/saml2/slo";
	private static final String LOGOUT_RESPONSE_URL = "/logout/saml2/slo";
	private final RelyingPartyRegistrationRepository myRelyingPartyRegistrationRepository;
	private final Saml2LogoutRequestResolver nameIdLogoutRequestResolver;
	private final AuthenticationProvider springSamlAuthenticationProvider;


	@Override
	public void configure(HttpSecurity http) throws Exception {
		//part 1: public anonymous paths
		http.authorizeHttpRequests().antMatchers("/").permitAll();
		//part 2: secured paths
		http.authorizeHttpRequests().antMatchers("/secure*","/secure/*").authenticated();
		

		//part 3: saml details config
		http.saml2Login() //make it saml login
				.relyingPartyRegistrationRepository(myRelyingPartyRegistrationRepository) //make saml config effective
				.authenticationManager(new ProviderManager(springSamlAuthenticationProvider))
		.defaultSuccessUrl("/"); // custom auth
		
		// part 4: logout details
		http.saml2Logout() // activate logout with following properties
				.logoutRequest().logoutUrl(LOGOUT_REQUEST_URL) //endpoint to receive logout request from idp
				.logoutRequestResolver(nameIdLogoutRequestResolver); //use this to generate logout request towards idp
		
		http.saml2Logout()
				.logoutResponse().logoutUrl(LOGOUT_RESPONSE_URL); // endpoint to receive logout response from idp
	}
}
