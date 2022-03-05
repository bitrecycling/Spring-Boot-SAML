package de.bitrecycling.springsaml.saml2.tool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * the defaults work with a keystore that was created like this:
 * keytool -genkeypair -alias spring_saml -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -dname "cn=bitrecycling,dc=de" -keypass password  -validity 365 -storetype PKCS12 -storepass password -keystore springsaml.p12
 */
@Service
public class KeyReader {
	@Value("${de.birecycling.spring_saml.keystore:classpath:keys/springsaml.p12}")
	private Resource keyStore;
	@Value("${de.birecycling.spring_saml.keystore.password:password}")
	private String keystorePass;
	@Value("${de.birecycling.spring_saml.keystore.password:spring_saml}")
	private String privateKeyName;
	@Value("${de.birecycling.spring_saml.keystore.password:password}")
	private String privateKeyPass;
	@Value("${de.birecycling.spring_saml.keystore.certificateAlias:spring_saml}")
	private String certificateAlias;
	
	public PrivateKey readPrivateKeyFromKeyStore(){
		try {
			return (PrivateKey) KeyStore.getInstance(keyStore.getFile(), keystorePass.toCharArray())
					.getKey(privateKeyName,privateKeyPass.toCharArray());
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException 
				| CertificateException | UnrecoverableKeyException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public X509Certificate readCertificateFromKeyStore(){
		try {
			return (X509Certificate)KeyStore.getInstance(keyStore.getFile(), keystorePass.toCharArray())
					.getCertificate(certificateAlias);
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new RuntimeException(e);
		}
	}
}
