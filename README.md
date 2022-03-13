# Spring Boot SAML Authentication
A very short SAML with Spring overview and a demo Spring Boot project with SAML2 authentication using Keycloak.

## About SAML and Spring

### A short terminology
Read up [elsewhere](https://docs.oasis-open.org/security/saml/v2.0/saml-glossary-2.0-os.pdf) for more details.

#### Entities 
in essence we care about 3 Entities: User, Service Provider, Identity Provider:
- **User (aka Subject)** A usually human being interacting with the system. Wants to use services provided by the Service Provider. Must be entitled to do so.
- **Service Provider (aka SP or relying party)** An application that provides services to the User. Knows and trusts the Identity Provider that the information and authenticaiton / authorization is valid.
- **Identity Provider (aka IDP or asserting party)** A system that allows a User to authenticate (login) and hence entitles the User according to configuration to use services provided by a Service Provider.

#### SAML Documents
The Security Assertion Markup Language defines a set of XML documents that are used throughout the login and logout. 

#### Assertion
All data that is transferred upon successful login by the IDP (hence asserting party) to the SP (hence relying party). This data is usually related to the Subject (a User) and usually includes identifiers, names, authorities...

Usually there is 1 Assertion containing n attributes, technically however more than one assertion is possible. Sometimes the attributes are referred to as "assertions" although this is wrong.

### Web Browser Single Sign On (SSO) Profile
Spring currently supports only the Web Browser SSO Profile for logins with a SAML IDP using "spring security saml2 provider".

The Web Browser SSO specifies that all communication is relayed via the User's browser, there's no backchannel (SP and IDP communicate directly) involved. Together with POST or REDIRECT binding this is widely supported and used typical web-application scenarios. This demo only covers the POST 
part.

### Single Logout (SLO) Profile
Spring supports both IDP and SP initiated logouts. 

### Metadata, Encryption and Signatures
Trust between SP and IDP is established through preliminary exchange of SAML Metadata: SP must be "registered" at the IDP using it's metadata. That metadata contains certificates, identifiers and other data that is used throughout the SAML communication. The aforementioned metadata contains 
certificates that enable secure the communication through encryption and signatures of the exchanged SAML Documents. Even if no SSL / https is used (please don't) the SAML Login and Logout can be considered secure if encryption (noone else can read) and 
signatures (noone else can manipulate) are used.

### Authentication Flow
If a user enters the SP / application (or hits the api) anonymously or logged out, a "redirect" to the IDP takes the user to the login form provided by the IDP. The redirect contains some information (the SAML AuthenticationRequest) about the SP that initiated the authentication on behalf of the 
user. Hence the IDP knows where to send the user after successful authentication and also which certificate to use to encrypt the assertion. If the user enters valid data, the browser is redirected back to the Application with a SAML Response that gets evaluated by the SP / application. If the SP 
validates the SAML Reponse and evaluates the Assertion, the user is "logged in" an can act based upon the Authorization given by SP, e.g. by evaluating authorities from the Assertion or some other means. 

### Session, Cookies, Tokens
After successful authentication, the only thing that is left is an authenticated session. Usually in Spring the session is *cookie based*, hence for it to work the cookie must be sent by the browser. 

Why - you may ask - am I mentioning this here? Because "modern" Browsers only send the cookie under certain circumstances for security reasons. 

There is a slightly less secure way that is transferring the session identifier (aka Token) using HTTP-Headers. This also means the client application has to deal with that token. Also if that token get's stolen, the attacker can easily take over the session. Both of this is much harder to to if 
a cookie is used, since that cookie can be configured to be more secure. Buzzwords: SameSite Secure HttpOnly. See example.




# Demo Application with Spring Boot (SP) and Keycloak (IDP)

The spring boot demo application is based on servlet technology, not reactive. The source comes with some preconceived details that match the ones given in the keycloak realm that can be imported, see below.

SAML Tracer is a useful browser plugin (available for both firefox and chrome) that makes life easier inspecting the SAML communication, although the browser's development tools shall be fine as well.

## Keys and Certificates Encryption and Signatures
Both encryption and document signing is used on the IDP and SP to secure SAML communication. To create SP-related key / certificate you need keytool from jdk to be installed:
```
    keytool -genkeypair -alias spring_saml -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -dname "cn=bitrecycling,dc=de" -keypass password  -validity 365 -storetype PKCS12 -storepass password -keystore springsaml.p12
```

## Spring Boot Demo Application as SP
The demo source contains a tiny api with some endpoints demo pages to show the login, logout and authorization and user details using the SAML assertion attributes. It runs on http://localhost:8080/samldemo

### SAML Metadata
The SP SAML Metadata is used to register with the IDP. The SAML Metadata endpoint is exposed under http://localhost:8080/saml2/service-provider-metadata/spring_saml. This is of course only if the defaults are used.
Download the xml file and save it to a known location we need later.

### Login
navigate to http://localhost:8080/samldemo and try some link. If you're not already authenticated, the browser get's redirected to the IDP login page. Login with the user created (see below). After that the browser gets redirected (actually submits a form POST) to the application that now knows 
you as the user you logged in with.

## Keycloak as IDP (dockerized)
from https://www.keycloak.org/getting-started/getting-started-docker

Start keycloak in docker on local port 8181 with admin / admin. This is **NOT A SAFE PRODUCTION CONFIG**
```shell
docker run -p 8181:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:17.0.0 start-dev
```
### Create (import) Realm 
1) Open http://localhost:8181 and navigate to Administration Console. Log in with admin/admin, see above.
2) Navigate to Add Realm (top left corner dropdown)
3) press "Select File" and locate the realm_spring_saml-with-client.json file in test/resources
4) the name must be SPRING_SAML in order for the further examples to work
   
You should now have a new "Realm" that contains a "Client". A client in keycloak terms equals an SP in SAML terminology.

### Add User
You can now add users to the realm like this:
1) Navigate to "Manage / Users" (left nav) and click "Add user" on the far right table head.
2) enter a Username, leave rest empty, click "save"
3) on the "Credentials" tab: enter a password (and confirmation), unselect "Temporary" and click "Set Password"

You should now have created a user that you can use in the demo application to log in to the application.

## Real World Challenges
In production scenarios the world gets a little more difficult, as usual. Since the Spring SAML support was "just recently" adopted by the spring security core team there's some more or less critical features missing and some bugs. Until covered by official release some of them need still to be 
addressed "manually". see https://github. com/spring-projects/spring-security/issues?q=is%3Aissue+saml2+

Apart from those there's some more things to think about in a production scenario.

### Load Balancing
if the SP nodes run behind a load balancer, if there's no sticky sessions, then the session has to be shared between the nodes. This used to be (currently still is unless Spring Security 5.7 gets released) problematic since the session also contains the
Representation of the Authentication Requests (AuthNRequest). Those were not implemented as serializable pre 5.7.x. A workaround using a custom RequestRepository was necessary.
https://github.com/spring-projects/spring-security/issues/10550#

### RESTful API and Single Page Application CORS
Afer SAML authentication, Spring normally identifies the user's session by the session cookie. Browsers prevent cookies to be
served cross-origin for security reasons (XSRF / CSRF). If SPA and API are hosted on different hosts (with different hostnames or domains) that means that the SPA needs to access the API on a different host it was served from. 
There's multiple ways around that:
- provide a reverse proxy serving with under the same hostname / domain with different paths. ==> same origin ==> no problems with cookie
- configure Spring to serve session token not as cookie but custom HTTP Header. the SPA then has to "manually" handle that token (resend it with every request) 

### Single Logout Profile, cookies, hosts and SameSite
If SLO between multiple SPs needs to be supported and should the SPs and IDPs run on different hosts there is another challenge. 
When a user triggers the logout on an arbitrary SP, the logout shall be performed all the other SPs as well. For the Single Logout Profile that means:
- Browser is redirected to IDP SLO page after logout was triggered ==> Browser is running in IDPs Context
- Browser needs to trigger (relay IDPs LogoutResponse by form POSTing) logout on all connected SPs 
- SameSite Cookie Policy needs to be "none", otherwise the connected SPs cannot identify the session that shall be logged out since the cookie is not transferred.
 
### Multiple Attribute Values
For Spring to work, currently Multiple Attribute Values (e.g. Roles) must be encoded in a List like structure as value under an Attribute Name and not multiple Attributes with the same name, see
https://github.com/spring-projects/spring-security/issues/10684


## Links
Spring Security SAML2 Provider
https://docs.spring.io/spring-security/reference/servlet/saml2/index.html

SAML 2.0 Overview
https://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html

SAML 2.0 Specification Documents OASIS
https://docs.oasis-open.org/security/saml/v2.0/
