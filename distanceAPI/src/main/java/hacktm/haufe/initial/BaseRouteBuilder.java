package hacktm.haufe.initial;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.model.InterceptDefinition;
import org.apache.camel.model.InterceptSendToEndpointDefinition;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;

import de.haufe.akademie.commons.StatusCode;
import de.haufe.akademie.commons.processor.ErrorResponseGenerator;
import de.haufe.akademie.commons.processor.SchemaValidationErrorHandler;

/**
 * A base route builder for sharing common functionality.
 *
 * @author Oliver Weiler (weiler@predic8.de)
 */
public abstract class BaseRouteBuilder extends RouteBuilder {
    /**
     * An XPath builder which uses Saxon. Always use this one instead of xpath().
     *
     * @param text
     * @return
     */
    protected XPathBuilder xpath2(String text) {
        return xpath(text).saxon();
    }

    /**
     * An XPath builder which uses Saxon. Always use this one instead of xpath().
     *
     * @param text
     * @param clazz
     * @return
     */
    protected XPathBuilder xpath2(String text, Class<?> clazz) {
        return xpath(text, clazz).saxon();
    }

    protected InterceptSendToEndpointDefinition interceptToJetty() throws UnsupportedEncodingException {
        return interceptSendToEndpoint("jetty:http:.*")
            // prevents ContentServ from gzip-compressing responses
            .removeHeader("Accept-Encoding")
            // it is generally a good idea to remove PATH and QUERY headers before performing an HTTP request
            .removeHeader(Exchange.HTTP_PATH)
            .removeHeader(Exchange.HTTP_QUERY)
            .setHeader("Authorization", simple("Basic " + EncodingUtils.encodeBase64("PRODOMO\\restTestUser:{{cserv.password}}")));
    }

    protected InterceptDefinition interceptFromJetty() {
        return interceptFrom("jetty:http:.*")
            .log("Client_DN_OU: ${header.SSL_CLIENT_S_DN_OU}");
    }

    protected void registerSchemaValidationErrorHandler() {
        onException(ValidationException.class)
            .handled(true)
            .process(new SchemaValidationErrorHandler(log));
    }

    /**
     * Resolves a property by name.
     *
     * @param name the property name
     * @return the resolved property
     * @throws Exception
     */
    protected String prop(String name) throws Exception {
      
	    return getContext().resolvePropertyPlaceholders("{{" + name + "}}");
        
    }

    protected Predicate validationEnabled() {
        return new Predicate() {
            public boolean matches(Exchange exchange) {
                try {
                    return prop("routes.validate").equals("true");
                } catch (Exception e) {
                    return false;
                }
            }
        };
    }
    
    /**
	 * Set the SSL keystore and pwd to the Jetty Component
	 * @param sslKeystorePath The path to the SSL keystore. Specify the location of the Java keystore files  
	 * @param sslTruststorePath The path to the SSL truststore
	 * @param sslKeyPassword The pwd for the keystore when using SSL. It is required to access the certificates entry in the keystore
	 * @throws Exception
	 */
    public void  setupKeyStore(String sslKeystorePath, String sslTruststorePath, String sslKeyPassword) throws Exception  {

  		KeyStoreParameters ksp = new KeyStoreParameters();
  		ksp.setResource(prop(sslKeystorePath));
  		ksp.setPassword(prop(sslKeyPassword));

  		KeyStoreParameters tsp = new KeyStoreParameters();
  		tsp.setResource(prop(sslTruststorePath));
  		tsp.setPassword(prop(sslKeyPassword));

  		KeyManagersParameters kmp = new KeyManagersParameters();
  		kmp.setKeyStore(ksp);
  		kmp.setKeyPassword(prop(sslKeyPassword));

  		TrustManagersParameters trustp = new TrustManagersParameters();
  		trustp.setKeyStore(tsp);

  		SSLContextParameters scp = new SSLContextParameters();
  		scp.setKeyManagers(kmp);
  		scp.setTrustManagers(trustp);

  		HttpComponent http4 = getContext().getComponent("https4", HttpComponent.class);
  		http4.setSslContextParameters(scp);
	}

    protected void exceptionHandlings() {
		onException(ValidationException.class)
		 .routeId("onValidationException")
		 .handled(true)
		 .log(LoggingLevel.ERROR,
		 "Validation failed for ${header.format} catalog. \n Exception: ${exception.message}")
		 .process(new
		 ErrorResponseGenerator("Validation failed for ${header.format}. There are inconsistencies AKA systems!",
		 StatusCode.PRECONDITION_FAILED));

		onException(CamelException.class)
		 .handled(true)
		 .log(LoggingLevel.ERROR,
		 "The route will stop! \n Exception: ${exception.message}")
		 .log(LoggingLevel.DEBUG,
		 "Exception Stracktrace:  ${exception.stacktrace}" )
		 .process(new
		 ErrorResponseGenerator("This request can not be proccessed for ${header.format}. There are inconsistencies AKA systems!",
		 StatusCode.PRECONDITION_FAILED));

		onException(IOException.class)
		 .handled(true)
		 .log(LoggingLevel.ERROR,
		 "The route will stop! \n Exception: ${exception.message}")
		 .log(LoggingLevel.DEBUG,
		 "Exception Stracktrace:  ${exception.stacktrace}" )
		 .process(new
		 ErrorResponseGenerator("Products export generatopm failed. Request can not be proccessed for ${header.format} catalog. There are inconsistencies AKA systems!",
		 StatusCode.PRECONDITION_FAILED));
	}
    
    /**
     * Return standard AKAI log message containing information about JMS destination, sender and operation performed
     * @param destination JMS Queue/Topic
     * @param operation Operation used. Can be create/update/delete
     * @param message Log Message
     * @return AKAI Log Message
     */
    public String logJmsOperation(String destination, String operation, String message) {
		return destination
				+  "; CorrelationId: ${header.JMSCorrelationID}; Object: ${header.objectType}; "
				+ operation + "; "+message;
	}
    
    public String logOperation(String message) {
		return  "CorrelationId: ${header.JMSCorrelationID}; "+ message;
	}
    
}
