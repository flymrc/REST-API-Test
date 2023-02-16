package io.educative.api.tests;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import io.educative.api.server.RESTApplication;

public abstract class BaseTest {

	@BeforeSuite
	public void startServer() {
		RESTApplication.startServer();
	}

	@AfterSuite
	public void stopServer() {
		RESTApplication.stopServer();
	}
}
