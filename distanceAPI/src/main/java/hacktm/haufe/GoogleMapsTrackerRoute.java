package hacktm.haufe;

import org.apache.camel.Exchange;
import org.apache.camel.component.http4.HttpOperationFailedException;

import de.haufe.akademie.commons.BaseRouteBuilder;
import de.haufe.akademie.commons.Constants;
import de.haufe.akademie.commons.StatusCode;
import de.haufe.akademie.commons.processor.ErrorResponseGenerator;
import de.haufe.akademie.commons.processor.Http4ErrorHandler;

public class GoogleMapsTrackerRoute extends BaseRouteBuilder {
	
	private final String GOOGLE_API_KEY="AIzaSyBtjxSs8jtkIgYto7kvefTu9bZPq9zPGS0";
	private final String GOOGLE_URI_MAPS="https://maps.googleapis.com/maps/api/directions/";
	private final String RESPONSE_TYPE="xml";

    @Override
    public void configure() throws Exception {
    	
    	String dest1="Timisoara";
    	String dest2="Oradea";
	    
	    registerSchemaValidationErrorHandler();

	    onException(HttpOperationFailedException.class)
        	.handled(true)
        	.process(new Http4ErrorHandler())
        	.choice()
            	.when(exchangeProperty("status").isEqualTo(StatusCode.BAD_REQUEST.toString()))
	            	.process(new ErrorResponseGenerator("Wrong sapID format of ${header.id}", StatusCode.BAD_REQUEST))
	            .when(exchangeProperty("status").isEqualTo(StatusCode.FORBIDDEN.toString()))
               		.process(new ErrorResponseGenerator("Forbidden. Authentication and authorization failed. Missing or invalid client certificate.", StatusCode.FORBIDDEN))
                .when(exchangeProperty("status").isEqualTo(StatusCode.NOT_FOUND.toString()))
               		.process(new ErrorResponseGenerator("Nothing found for ${header.id}", StatusCode.NOT_FOUND));
//	    http://localhost:4567/fha/distance?start=Oradea&dest=Timisoara
       from("direct:getGoogleDistance")
       		.log("base: " + Constants.CSERV_BASE_PATH)
            .removeHeader(Exchange.HTTP_PATH)
            .removeHeader(Exchange.HTTP_QUERY)
            .log("will call URL: "+ GOOGLE_URI_MAPS+RESPONSE_TYPE+"?origin="+dest1+"&destination="+dest2+"&key="+GOOGLE_API_KEY)
            .setHeader(Exchange.HTTP_URI, simple(GOOGLE_URI_MAPS+RESPONSE_TYPE+"?origin=${header.start}&destination=${header.dest}&key="+GOOGLE_API_KEY))
      	    .to(Constants.HTTP4_HTTPS_DUMMY_ENDPOINT)
      	    .setProperty("distance", xpath("/DirectionsResponse/route/leg/distance/text/text()"))
      	    .log("${property.distance}")
      	      .process(new ResponseProcessor())
//      	    .setBody(simple("Distance ${property.distance}"))
//      	    .to(Constants.HTTP4_HTTPS_DUMMY_ENDPOINT)
      	    
      	    ;
//       	    .log("URI:  ${header.CamelHttpUri}")
//       	    .process(new InstructorLinksAppenderProcessor())
//       	    .choice()
//               .when(validationEnabled())
//               .to("xslt:xslt/transform-instructor.xsl?saxon=true")
//               .to("validator:schema/instructor-v1.0.xsd")
//               .log("VALIDATION: Validation succeeded");

    }

}