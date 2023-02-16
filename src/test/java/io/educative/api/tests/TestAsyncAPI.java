package io.educative.api.tests;

import static org.testng.Assert.assertNotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.educative.api.helpers.StudentServiceHelper;

public class TestAsyncAPI extends BaseTest {

	private static Logger LOG = LoggerFactory.getLogger(TestAsyncAPI.class);

	private StudentServiceHelper serviceHelper;

	@BeforeClass
	public void init() {
		serviceHelper = new StudentServiceHelper();
	}

	@Test
	public void asyncTest() throws InterruptedException, ExecutionException, TimeoutException {
		org.asynchttpclient.Response rsp = serviceHelper.asyncAPIRequest();
		LOG.info(rsp.getResponseBody());
		assertNotNull(rsp.getResponseBody(), "Response Body is not null");
	}
}