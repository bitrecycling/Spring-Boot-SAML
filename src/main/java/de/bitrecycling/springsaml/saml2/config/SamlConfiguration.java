package de.bitrecycling.springsaml.saml2.config;

import de.bitrecycling.springsaml.saml2.tool.KeyReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.AbstractSaml2AuthenticationRequest;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.HttpSessionSaml2AuthenticationRequestRepository;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2AuthenticationRequestRepository;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml4LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;

@Configuration
@RequiredArgsConstructor
public class SamlConfiguration {
    
    private final KeyReader keyReader;
    private static final String LOGOUT_REQUEST_URL = "{baseUrl}/logout/saml2/slo";
    @Value("${de.bitrecycling.springsaml.security.saml.sp.registration.id:spring_saml}")
    private String spRegistrationId;
    @Getter

    /**
     * this can either be a URL (http://...) or classpath resource (file in filesystem): classpath:
     */
//    @Value("${de.bitrecycling.springsaml.security.saml.idp.metadata.url:http://localhost:8181/realms/SPRING_SAML/protocol/saml/descriptor}")
    @Value("${de.bitrecycling.springsaml.security.saml.idp.metadata.url:classpath:metadata/idp_metadata.xml}")
    private String idpMetadataEndpoint;
    
    /**
     * this uses the working from metadata but sets the wantAuthnRequestsSigned(true)
     *
     * @return
     */
    @Bean
    public RelyingPartyRegistrationRepository myRelyingPartyRegistrationRepository() {
        return new InMemoryRelyingPartyRegistrationRepository(relyingPartyRegistration());
    }


    private RelyingPartyRegistration relyingPartyRegistration() {
        return
                RelyingPartyRegistration.withRelyingPartyRegistration(fromIPDMetaData()).assertingPartyDetails(
                        details -> details.wantAuthnRequestsSigned(true) //make sure this is true
                        
                ).build();
    }

    private RelyingPartyRegistration fromIPDMetaData() {
        return RelyingPartyRegistrations
                .fromMetadataLocation(idpMetadataEndpoint)
                .registrationId(spRegistrationId)
                .decryptionX509Credentials(
                        (c) -> c.add(Saml2X509Credential.decryption(keyReader.readPrivateKeyFromKeyStore(),
                                keyReader.readCertificateFromKeyStore())))
                .signingX509Credentials(
                        (c) -> c.add(Saml2X509Credential.signing(keyReader.readPrivateKeyFromKeyStore(),
                                keyReader.readCertificateFromKeyStore())))
                .singleLogoutServiceResponseLocation(LOGOUT_REQUEST_URL)
                .singleLogoutServiceLocation(LOGOUT_REQUEST_URL)
                .build();
    }

    /**
     * deviate here to provide a custom Saml2AuthenticationRequestRepository, e.g. to overcome
     * https://github.com/spring-projects/spring-security/issues/10550#
     *
     */
    @Bean
    Saml2AuthenticationRequestRepository<AbstractSaml2AuthenticationRequest> authenticationRequestRepository() {
        return new HttpSessionSaml2AuthenticationRequestRepository(); //this is the default
    }

    /**
     * SP / relying party (=our) SAML metadata can be reached here (if not deviating from default config)
     * http://localhost:8080/saml2/service-provider-metadata/spring_saml
     */
    @Bean
    public Saml2MetadataFilter metadataEndpointFilter() {
        return new Saml2MetadataFilter(myRegistrationResolver(), new OpenSamlMetadataResolver());
    }

    @Bean
    public RelyingPartyRegistrationResolver myRegistrationResolver() {
        return new DefaultRelyingPartyRegistrationResolver(myRelyingPartyRegistrationRepository());
    }

    /**
     * necessary as log as Spring security only handles nameId logout requests (instead of e.g. encryptedId)
     * see https://github.com/spring-projects/spring-security/issues/10663
     * and https://github.com/spring-projects/spring-security/pull/10689
     * @param registrationResolver
     * @return
     */
    @Bean
    Saml2LogoutRequestResolver nameIdLogoutRequestResolver(RelyingPartyRegistrationResolver registrationResolver) {
        OpenSaml4LogoutRequestResolver logoutRequestResolver =
                new OpenSaml4LogoutRequestResolver(registrationResolver);
        logoutRequestResolver.setParametersConsumer((parameters) -> {
            String name =
                    ((Saml2AuthenticatedPrincipal) parameters.getAuthentication().getPrincipal()).getName();
            LogoutRequest logoutRequest = parameters.getLogoutRequest();
            NameID nameId = logoutRequest.getNameID();
            nameId.setValue(name);
            nameId.setFormat(NameIDType.PERSISTENT); 
        });
        return logoutRequestResolver;
    }
    
}
