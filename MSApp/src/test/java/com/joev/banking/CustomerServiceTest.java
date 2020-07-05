package com.joev.banking;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.joev.util.SHClient;
import com.joev.util.SHClient.SHResp;
import com.joev.util.SimpleJsonDb;

public class CustomerServiceTest {
	private static final Logger logger = LogManager.getLogger(CustomerServiceTest.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Rule public TestName testName = new TestName();

	private static final int TEST_PORT = 9000;
	private static final String URL_BASE = "http://localhost:" + TEST_PORT;
	Server server;
	
	private static final String TEST_ID = "100";
	private static final String TEST_ID2 = "101";
	private static final String TEST_ID_BAD = "999";
	private static final String TEST_NEWUSER_NAME = "New User";
	private static final String TEST_NEWUSER_ADDRESS = "1234 Sesame Street Minneapolis MN";
	private static final String TEST_NEWUSER_JSON = "{"
			+ "\"name\": \"" + TEST_NEWUSER_NAME + "\", "
			+ "\"address\": \"" + TEST_NEWUSER_ADDRESS + "\""
			+ "}";
	private static final String TEST_MODUSER_NAME = "Danew Name";
	private static final String RESPONSE_HEADER_KEY_LOCATION = "Location";
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		logger.info("Making test database directory");
		SimpleJsonDb.mkTestDbDir();
	}
	
	@AfterClass
	public static void afterClass() throws IOException {
		logger.info("Removing test database directory");
		SimpleJsonDb.rmTestDbDir();
	}
	
	@Before
	public void before() throws Exception {
		logger.info("================================================================================");
		logger.info("Starting test {}.{}", this.getClass().getSimpleName(), testName.getMethodName());
		// Initialize the test databases using 'seed' contents from test/resources
		new CustomerDb().initTables();
		// Start the service, using an alternate port for testing
		logger.info("before(): Starting server");
		server = CustomerService.createServer(TEST_PORT);
		server.start();
		logger.info("before(): Server is started");
	}
	
	@After
	public void after() throws Exception {
		logger.info("after(): Stopping server");
		server.stop();
		logger.info("after(): Server is stopped");
		// Remove the test databases
		new CustomerDb().rmTables();
	}
	
	/** Test getting the Customers list */
	@Test
	public void testGetCustomers() throws Exception {
		SHResp response = SHClient.doHttp(URL_BASE + "/customers", SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		SHClient.logResponse(response);
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.OK.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.OK.getReasonPhrase());
		assertThat(response.responseBody).isNotEmpty();
		// Parse response body to list of Customer.Builders. This implicitly verifies that the
		// response is valid JSON syntax representing a list of objects, each of which is in the
		// correct form to represent a Customer.Builder (otherwise the parse would fail).
		Customer.Builder customers[] = mapper.readValue(response.responseBody, Customer.Builder[].class);
		logger.info("Successfully parsed response body from JSON to a list of {} customers:",
			customers.length);
		for (Customer.Builder pb : customers) {
			logger.info("  {}", pb.build());
		}
		assertThat(customers).isNotEmpty();
	}
	
	/** Test creating a new Customer */
	@Test
	public void testCreateCustomer() throws Exception {
		SHResp response = SHClient.doHttp(URL_BASE + "/customers", SHClient.RM.POST, TEST_NEWUSER_JSON, SHClient.RP.CON_JSON, SHClient.RP.ACC_JSON);
		SHClient.logResponse(response);
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.CREATED.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.CREATED.getReasonPhrase());
		assertThat(response.responseBody).isNotEmpty();
		// Ensure that 'Location' header is the URI of the newly-created Customer
		assertThat(response.responseHeaders).containsKey(RESPONSE_HEADER_KEY_LOCATION);
		List<String> locations = response.responseHeaders.get(RESPONSE_HEADER_KEY_LOCATION);
		assertThat(locations.size()).isEqualTo(1);
		String location = locations.get(0);
		assertThat(location).startsWith(URL_BASE + "/customers");
		// TODO: Consider enhancing this verification to:
		// 1. Actually GET the 'location' URL, expecting it to return the newly-created Customer
		// 2. Access the database directly to count the items before/after, or maybe even to
		//    retrieve the newly-created Customer
	}
	
	/** Test reading a specific Customer's information */
	@Test
	public void testGetCustomer() throws Exception {
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		SHClient.logResponse(response);
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.OK.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.OK.getReasonPhrase());
		assertThat(response.responseBody).isNotEmpty();
		// Parse response body to a Customer.Builder. This implicitly verifies that the response is
		// valid JSON syntax representing a Customer.Builder (otherwise the parse would fail).
		Customer.Builder customer = mapper.readValue(response.responseBody, Customer.Builder.class);
		logger.info("Successfully parsed response body from JSON to a Customer: {}",
			customer.build());
		assertThat(customer.id().get()).isEqualTo(TEST_ID);
	}
	
	/** Test reading a nonexistent Customer's information */
	@Test
	public void testGetCustomer_NotExist() throws Exception {
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID_BAD, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		SHClient.logResponse(response);
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
		assertThat(response.responseBody).isNotEmpty();
	}
	
	/** Test updating a Customer */
	@Test
	public void testUpdateCustomer() throws Exception {
		// ARRANGE: get the Customer's original data, then modify part of it
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		Customer.Builder origPB = mapper.readValue(response.responseBody, Customer.Builder.class);
		Customer origP = origPB.build();
		logger.info("Original Customer: {}", origP);
		Customer modP = Customer.builder().id(origP.id()).name(TEST_MODUSER_NAME).address(origP.address()).build();
		logger.info("Modified Customer: {}", modP);

		// ACT: Make an HTTP 'PUT' request to update the customer
		String modPJson = mapper.writeValueAsString(modP);
		response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.PUT, modPJson, SHClient.RP.CON_JSON);
		SHClient.logResponse(response);
		
		// ASSERT: Response=OK, and GET shows that the Customer has been updated
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.OK.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.OK.getReasonPhrase());
		response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		Customer verifyP = mapper.readValue(response.responseBody, Customer.Builder.class).build();
		logger.info("Customer from GET after update: {}", verifyP);
		assertThat(verifyP).isEqualTo(modP);
	}

	/** Test updating a nonexistent Customer */
	@Test
	public void testUpdateCustomer_NotExist() throws Exception {
		// ARRANGE: get a sample Customer's data, then modify it to appear to be for a Customer
		// that does not exist
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		Customer.Builder pb = mapper.readValue(response.responseBody, Customer.Builder.class);
		pb.id(TEST_ID_BAD);

		// ACT: Make an HTTP 'PUT' request to update the customer
		String modPJson = mapper.writeValueAsString(pb.build());
		response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID_BAD, SHClient.RM.PUT, modPJson, SHClient.RP.CON_JSON);
		SHClient.logResponse(response);
		
		// ASSERT: Response=NOT_FOUND
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
	}

	/**
	 * Test updating a Customer with a mismatched Id specification (specify one Id in the URL and
	 * a different Id in the JSON payload)
	 */
	@Test
	public void testUpdateCustomer_MismatchId() throws Exception {
		// ARRANGE: get the Customer's original data, then modify part of it
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		Customer.Builder origPB = mapper.readValue(response.responseBody, Customer.Builder.class);
		Customer origP = origPB.build();
		logger.info("Original Customer: {}", origP);
		Customer modP = Customer.builder().id(origP.id()).name(TEST_MODUSER_NAME).address(origP.address()).build();
		logger.info("Modified Customer: {}", modP);

		// ACT: Make an HTTP 'PUT' request to update the customer, but specify a different ID in
		// the URL than in the JSON payload
		String modPJson = mapper.writeValueAsString(modP);
		response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID2, SHClient.RM.PUT, modPJson, SHClient.RP.CON_JSON);
		SHClient.logResponse(response);
		
		// ASSERT: Response=BAD_REQUEST
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.BAD_REQUEST.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.BAD_REQUEST.getReasonPhrase());
	}

	/** Test deleting a specific Customer */
	@Test
	public void testDeleteCustomer() throws Exception {
		SHResp delResponse = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.DELETE, null, SHClient.RP.ACC_TEXT);

		SHClient.logResponse(delResponse);
		assertWithMessage("Response code").that(delResponse.responseCode).isEqualTo(Status.OK.getStatusCode());
		assertWithMessage("Response message").that(delResponse.responseMessage).isEqualTo(Status.OK.getReasonPhrase());
		assertThat(delResponse.responseBody).isNotEmpty();
		// Verify that the Customer has in fact been deleted, by trying to GET it (should fail)
		SHResp getResponse = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID, SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
		SHClient.logResponse(getResponse);
		assertWithMessage("Response code").that(getResponse.responseCode).isEqualTo(Status.NOT_FOUND.getStatusCode());
	}
	
	/** Test deleting a nonexistent Customer */
	@Test
	public void testDeleteCustomer_NotExist() throws Exception {
		SHResp response = SHClient.doHttp(URL_BASE + "/customers/" + TEST_ID_BAD, SHClient.RM.DELETE, null, SHClient.RP.ACC_JSON);
		SHClient.logResponse(response);
		assertWithMessage("Response code").that(response.responseCode).isEqualTo(Status.NOT_FOUND.getStatusCode());
		assertWithMessage("Response message").that(response.responseMessage).isEqualTo(Status.NOT_FOUND.getReasonPhrase());
		assertThat(response.responseBody).isNotEmpty();
	}
}
