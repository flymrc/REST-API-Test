package io.educative.api.helpers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpStatus;
import org.asynchttpclient.Dsl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.educative.api.constants.Endpoints;
import io.educative.api.model.Student;
import io.educative.api.utils.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class StudentServiceHelper {

	private static final Logger LOG = LoggerFactory.getLogger(StudentServiceHelper.class);

	private static final String BASE_URL = ConfigManager.getInstance().getString("api.base_url");

	public StudentServiceHelper() {
		RestAssured.useRelaxedHTTPSValidation();
		RestAssured.baseURI = BASE_URL;
		RestAssured.filters(new io.qameta.allure.restassured.AllureRestAssured());
	}

	@Step
	public List<Student> getStudents() {
		LOG.info("fetching all students");
		Type type = new TypeReference<List<Student>>() {
		}.getType();
		Response response = RestAssured.given()
				.get(Endpoints.GET_ALL_STUDENTS)
				.andReturn();
		assertEquals(response.getStatusCode(), HttpStatus.SC_OK, "http status");
		List<Student> students = response.as(type);
		LOG.info("response => {}", students);
		return students;
	}

	@Step
	public Student getStudent(long id) {
		LOG.info("fetching student with id '{}'", id);
		Response response = RestAssured.given()
				.pathParam("id", id)
				.get(Endpoints.GET_STUDENT_BY_ID)
				.andReturn();
		assertEquals(response.getStatusCode(), HttpStatus.SC_OK, "http status");
		Student student = response.as(Student.class);
		LOG.info("response => {}", student);
		return student;
	}

	@Step
	public Response createStudent() {
		LOG.info("Create a new Student ");

		Student body = new Student();
		body.setFirstName("David");
		body.setLastName("Paul");
		body.setGender("Male");

		Response response = RestAssured.given().header("accept", "application/json")
				.header("content-type", "application/json")
				.body(body)
				.post(Endpoints.CREATE_STUDENT)
				.andReturn();
		assertEquals(response.getStatusCode(), HttpStatus.SC_CREATED, "http status");
		return response;
	}

	@Step
	public Response deleteStudent(Long id) {

		LOG.info("Deleting student with id '{}'", id);
		Response response = RestAssured.given()
				.pathParam("id", id)
				.delete(Endpoints.GET_STUDENT_BY_ID)
				.andReturn();
		assertTrue(response.getStatusCode() == 204);
		return response;
	}

	@Step
	public Response getStudentAuth(String userName, String password) {

		LOG.info("API call using Auth credentials");
		Response response = RestAssured
				.given()
				.auth().basic(userName, password)
				.get(Endpoints.AUTH_GET_ALL_STUDENTS)
				.andReturn();
		return response;
	}

	@Step
	public Response getStudentAuthToken(String token) {

		LOG.info("API call using Authentication token");
		Response response = RestAssured
				.given()
				.header("authorization", token)
				.get(Endpoints.AUTH_GET_ALL_STUDENTS)
				.andReturn();
		return response;
	}

	@Step
	public org.asynchttpclient.Response asyncAPIRequest()
			throws InterruptedException, ExecutionException, TimeoutException {

		LOG.info("Test Async API");
		String url = "https://reqres.in/api/users?delay=3";
		Future<org.asynchttpclient.Response> whenResponse = Dsl.asyncHttpClient().prepareGet(url).execute();
		org.asynchttpclient.Response response = whenResponse.get();
		return response;
	}

}
