package io.educative.api.tests;

import static org.testng.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.educative.api.helpers.StudentServiceHelper;
import io.restassured.response.Response;

public class TestAuthentication extends BaseTest {

	private StudentServiceHelper serviceHelper;
	private static Logger LOG = LoggerFactory.getLogger(TestAuthentication.class);

	@BeforeClass
	public void init() {
		serviceHelper = new StudentServiceHelper();
	}

	/**
	 * Basic authentication using Valid username and password
	 */
	@Test
	public void test_authentication_ValidCredentials() {

		String valid_userName = "testuser";
		String valid_password = "testpass";

		Response resp = serviceHelper.getStudentAuth(valid_userName, valid_password);
		assertEquals(resp.getStatusCode(), HttpStatus.SC_OK, "http status");
		LOG.info("It will return a valid response");
		resp.getBody().prettyPrint();
	}

	/**
	 * Basic authentication using In-valid username and password
	 */
	@Test
	public void test_authentication_InvalidCredentials() {

		String invalid_userName = "testuser1";
		String valid_password = "testpass";

		Response resp = serviceHelper.getStudentAuth(invalid_userName, valid_password);
		assertEquals(resp.getStatusCode(), HttpStatus.SC_UNAUTHORIZED, "http status");

	}

	/**
	 * Basic authentication using Auth token
	 */
	@Test
	public void test_authentication_AuthToken() {

		String authCode = "Basic dGVzdHVzZXI6dGVzdHBhc3M=";

		Response resp = serviceHelper.getStudentAuthToken(authCode);

		LOG.info("It will return a valid response");
		resp.getBody().prettyPrint();
		assertEquals(resp.getStatusCode(), HttpStatus.SC_OK, "http status");
	}

}