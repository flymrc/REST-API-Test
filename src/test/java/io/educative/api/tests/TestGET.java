package io.educative.api.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.educative.api.helpers.StudentServiceHelper;
import io.educative.api.model.Student;

public class TestGET extends BaseTest {

	private StudentServiceHelper serviceHelper;
	private static Logger LOG = LoggerFactory.getLogger(TestGET.class);

	@BeforeClass
	public void init() {
		serviceHelper = new StudentServiceHelper();
	}

	@Test
	public void testGetAllStudents() {
		LOG.info("Test GET all students method.");
		List<Student> students = serviceHelper.getStudents();
		assertNotNull(students, "students list is empty");
		assertFalse(students.isEmpty(), "students list is empty");
	}

	@Test
	public void testGetStudentById() {
		LOG.info("Test GET student by id method.");
		long id = 101;
		Student student = serviceHelper.getStudent(id);
		assertNotNull(student, "unable to find student with id " + id);
	}
}
