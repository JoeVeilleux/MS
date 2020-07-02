package com.joev.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.joev.util.SHClient.RM;
import com.joev.util.SHClient.RP;
import com.joev.util.SHClient.SHResp;

public class SHClientTest {
	private static final Logger logger = LogManager.getLogger(SHClientTest.class);
	
	@Rule public TestName testName = new TestName();
	
	@Before
	public void before() throws Exception {
		logger.info("================================================================================");
		logger.info("Starting test {}.{}", this.getClass().getSimpleName(), testName.getMethodName());
	}

	@Test
	public void testDoHttp() throws Exception {
		SHResp r = SHClient.doHttp("http://www.google.com", RM.GET, null, RP.ACC_TEXT);
		SHClient.logResponse(r);
	}
	
	// TODO: ADD MORE TESTS

}
