package de.bitrecycling.springsaml.saml2.config;

import de.bitrecycling.springsaml.saml2.tool.KeyReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.security.x509.X509Support;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class SamlConfiguration {
    
    private final KeyReader keyReader;

    @Value("${de.bitrecycling.springsaml.security.saml.sp.registration.id:spring_saml_demo}")
    private String spRegistrationId;
    @Getter
    @Value("${de.bitrecycling.springsaml.security.saml.sp.entity.id:http://localhost:8080/spring-saml-demo}")
    private String spEntityId;
    /**
     * this can either be a URL (http://...) or classpath resource (file in filesystem): classpath:
     */
    @Value("${de.bitrecycling.springsaml.security.saml.idp.metadata.url:http://localhost:8181/realms/SPRING_SAML/protocol/saml/descriptor}")
    private String idpMetadataEndpoint;

//    @Value("${de.bitrecycling.springsaml.security.saml.rsaprivatekey.classpath:classpath:keys/spring_saml.privatekey}")
//    RSAPrivateKey privateKey;
    @Value("${de.bitrecycling.saml.sp-certificate:classpath:keys/spring_saml.crt}")
    private Resource publicCertSP;
    
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
//                .decryptionX509Credentials(
//                        (c) -> c.add(Saml2X509Credential.decryption(this.privateKey,
//                                readX509Cert(this.publicCertSP))))
                .decryptionX509Credentials(
                        (c) -> c.add(Saml2X509Credential.decryption(keyReader.readPrivateKeyFromKeyStore(),
                                keyReader.readCertificateFromKeyStore())))
                .signingX509Credentials(
                        (c) -> c.add(Saml2X509Credential.signing(keyReader.readPrivateKeyFromKeyStore(),
                                keyReader.readCertificateFromKeyStore())))
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
     * SP / relying party (=our) SAML metadata can be reached here
     * http://localhost:8080/spring-saml-demo/saml2/service-provider-metadata/spring_saml
     *
     * @return
     */
    @Bean
    public Saml2MetadataFilter metadataEndpointFilter() {
        return new Saml2MetadataFilter(myRegistrationResolver(), new OpenSamlMetadataResolver());
    }

    @Bean
    public RelyingPartyRegistrationResolver myRegistrationResolver() {
        return new DefaultRelyingPartyRegistrationResolver(myRelyingPartyRegistrationRepository());
    }

    @Bean
    Saml2LogoutRequestResolver myLogoutRequestResolver(RelyingPartyRegistrationResolver registrationResolver) {
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


    public X509Certificate readX509Cert(Resource certificate) {
        try {
            return X509Support.decodeCertificate(readFileAsString(certificate));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String readFileAsString(Resource res) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(res.getInputStream(),
                StandardCharsets.UTF_8));

        final String rawString = bufferedReader.lines().collect(Collectors.joining());
        final String keyString = rawString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");
        return rawString;
        
    }

}
