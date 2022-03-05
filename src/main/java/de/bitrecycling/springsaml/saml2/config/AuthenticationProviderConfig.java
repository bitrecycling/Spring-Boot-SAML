package de.bitrecycling.springsaml.saml2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

@Configuration
public class AuthenticationProviderConfig{
	//@Bean
	public AuthenticationProvider myAuthenticationProvider(){
//		OpenSaml4AuthenticationProvider my = new OpenSaml4AuthenticationProvider();
//		my.setResponseAuthenticationConverter(responseToken -> {
//			responseToken.getToken();
//			Saml2Authentication saml2Authentication =
//					OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter().convert(responseToken);
//			// here goes custom code that is relevant to your project's UserDetails / Authentication
//			//this might be useful
//			Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) saml2Authentication.getPrincipal();
//			return principal;
//		});
//		
		return null;
	}
}