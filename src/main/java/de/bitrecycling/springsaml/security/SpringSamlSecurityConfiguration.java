package de.bitrecycling.springsaml.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSamlSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final String LOGOUT_REQUEST_URL = "/logout";
	private static final String LOGOUT_RESPONSE_URL = "/logged_out";
	private final RelyingPartyRegistrationRepository myRelyingPartyRegistrationRepository;
	private final Saml2LogoutRequestResolver myLogoutRequestResolver;
	

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.saml2Login().relyingPartyRegistrationRepository(myRelyingPartyRegistrationRepository);
//				.authenticationManager(new ProviderManager(myAuthenticationProvider)
//				);
		
		http.saml2Logout().logoutRequest().logoutUrl(LOGOUT_REQUEST_URL).logoutRequestResolver(myLogoutRequestResolver);
		http.saml2Logout().logoutResponse().logoutUrl(LOGOUT_RESPONSE_URL);
		
	}

	
}
