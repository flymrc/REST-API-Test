package io.educative.api.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.educative.api.helpers.StudentServiceHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TestDelete extends BaseTest {

	private StudentServiceHelper serviceHelper;
	private static Logger LOG = LoggerFactory.getLogger(TestDelete.class);
	private Long sId;

	public Long getsId() {
		return sId;
	}

	public void setsId(Long sId) {
		this.sId = sId;
	}

	@BeforeClass
	public void init() {
		serviceHelper = new StudentServiceHelper();
	}

	@Test
	public void createANewStudent() {

		LOG.info("Test POST Request --> create a new Student.");
		Response response = serviceHelper.createStudent();
		JsonPath jpath = response.jsonPath();
		Long id = jpath.getLong("id");
		LOG.info("Id of the newly creaed student `{}`", id);
		setsId(id);
	}

	@Test(dependsOnMethods = { "createANewStudent" })
	public void deleteCreatedStudent() {

		LOG.info("Test DELETE Request --> delete a Student.");
		Response response = serviceHelper.deleteStudent(getsId());
		LOG.info("Status line: " + response.getStatusLine());
	}

}
