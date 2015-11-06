package de.haufe.instructor;

import static com.jayway.restassured.RestAssured.expect;
import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.resourceContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.xebialabs.restito.server.StubServer;

public class InstructorIDTest {

	private StubServer server;

	@Before
	public void start() {
		server = new StubServer().run();
		RestAssured.port = server.getPort();
	}

	@After
	public void stop() {
		server.stop();
	}

	@Test
	public void verifyInstructorByIDTest() {
		
		whenHttp(server).match(get("/v1/instructors/00001009")).then(
				contentType("application/xml "), resourceContent("instructor-00001009.xml"));

		Response response = expect().when().get("/v1/instructors/00001009");

		String bodyString = response.getBody().asString();

		assertEquals(200, response.getStatusCode());
		assertTrue(bodyString.contains("<id>00001009</id>"));
		assertTrue(bodyString.contains("<firstname>KLAUS</firstnam"));
		assertTrue(bodyString.contains("<lastname>HOCKK</lastname>"));
		assertTrue(bodyString.contains("<email type=\"account\">HOCK@TESTMAIL.COM</email>"));
	}

}
