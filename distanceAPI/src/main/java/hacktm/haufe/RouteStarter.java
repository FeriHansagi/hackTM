package hacktm.haufe;

import hacktm.haufe.initial.BaseRouteBuilder;


/**
 * All the calls for inner routes are made based on the routes defined here.
 * 
 * @author HansagiF
 *
 */
public class RouteStarter extends BaseRouteBuilder {

	@Override
	public void configure() throws Exception {
		interceptFromJetty();
		interceptToJetty();

		registerSchemaValidationErrorHandler();
		
		rest("/fha/distance").get().to("direct:getGoogleDistance");

				
	}

	public static void main(String[] args) throws Exception {
		Main.main(args);
	}

}