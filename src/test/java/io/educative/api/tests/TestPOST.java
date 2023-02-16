package io.educative.api.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.educative.api.helpers.StudentServiceHelper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class TestPOST extends BaseTest {

	private StudentServiceHelper serviceHelper;
	private static Logger LOG = LoggerFactory.getLogger(TestPOST.class);

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
	public void createNewStudent() {

		LOG.info("Test POST Request --> create a new Student.");
		Response response = serviceHelper.createStudent();

		JsonPath jpath = response.jsonPath();
		Long id = jpath.getLong("id");
		LOG.info("Id of the newly created student `{}`", id);
		setsId(id);

	}

	@AfterClass
	public void cleanUp() {
		Response response = serviceHelper.deleteStudent(getsId());
		LOG.info("Status line: " + response.getStatusLine());
	}

}
