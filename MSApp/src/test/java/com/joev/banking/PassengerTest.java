package com.joev.banking;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Unit test for Passenger.
 */
public class PassengerTest {
	private static final Logger logger = LogManager.getLogger(PassengerTest.class);
	
	@Rule public TestName testName = new TestName();
	
	private static final String TEST_ID = "123";
	private static final String TEST_NAME = "Joe V";
	private static final String TEST_ADDR = "123 Sesame Street";
	
	@Before
	public void before() throws IOException {
		logger.info("================================================================================");
		logger.info("Starting test {}.{}", this.getClass().getSimpleName(), testName.getMethodName());
	}

	@Test
    public void testBuild() {
		Passenger p = Passenger.builder().id(TEST_ID).name(TEST_NAME).address(TEST_ADDR).build();
		System.out.println("Constructed Passenger: " + p.toString());
		assertThat(p.id()).isEqualTo(TEST_ID);
		assertThat(p.name()).isEqualTo(TEST_NAME);
		assertThat(p.address()).isEqualTo(TEST_ADDR);
    }
}
