package com.joev.banking;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.joev.util.SimpleJsonDb;

public class CustomerDbTest {
	private static final Logger logger = LogManager.getLogger(CustomerDbTest.class);
	
	@Rule public TestName testName = new TestName();

	private static final String TEST_ID = "100";
	private static final String TEST_ID_BAD = "999";
	private static final int EXPECTED_NROWS = 4; // Number of rows in sample/test data
	
	public CustomerDb db;
	
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
	public void before() throws IOException {
		logger.info("================================================================================");
		logger.info("Starting test {}.{}", this.getClass().getSimpleName(), testName.getMethodName());
		db = new CustomerDb();
		// Initialize the test databases using 'seed' contents from test/resources
		db.initTables();
	}
	
	@After
	public void after() throws Exception {
		// Remove the test databases
		db.rmTables();
	}
	
	/** Test getting the Customers list */
	@Test
	public void testGetCustomers() {
		List<Customer> data = db.getCustomers();
		showData("DATA", data);
		assertThat(data.size()).isEqualTo(EXPECTED_NROWS);
	}
	
	/** Test creating a new Customer */
	@Test
	public void testCreateCustomer() {
		List<Customer> beforeData = db.getCustomers();
		showData("BEFORE", beforeData);

		// Create a new Customer
		Customer.Builder newPB = Customer.builder().name("New Person").address("123 Sesame Street");
		Customer newP = db.createCustomer(newPB);
		logger.info("New Customer: " + newP);

		// Redisplay all the data, to prove that the new Customer was added
		List<Customer> afterData = db.getCustomers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Customer list")
			.that(afterData.size())
			.isEqualTo(beforeData.size() + 1);
		assertThat(afterData).contains(newP);
	}
	
	/** Test reading a specific customer's information */
	@Test
	public void testReadCustomer() {
		// Read a Customer
		Customer p = db.readCustomer(TEST_ID);
		logger.info("Customer with id={}: {}", TEST_ID, p);

		assertThat(p).isNotNull();
		assertThat(p.id()).isEqualTo(TEST_ID);
	}
	
	/** Test reading a nonexistent customer's information. */
	@Test
	public void testReadCustomer_BadId() {
		// Read a Customer that does not exist
		Customer p = db.readCustomer(TEST_ID_BAD);
		logger.info("Customer with id={}: {}", TEST_ID_BAD, p);

		assertThat(p).isNull();
	}
	
	/** Test updating a Customer */
	@Test
	public void testUpdateCustomer() {
		List<Customer> beforeData = db.getCustomers();
		showData("BEFORE", beforeData);
		Customer origP = beforeData.get(0);
		logger.info("Original Customer: {}", origP);

		String newName = origP.name() + "X";
		Customer newP = Customer.builder().id(origP.id()).name(newName).address(origP.address()).build();
		logger.info("New Customer: {}", newP);
		db.updateCustomer(newP);

		List<Customer> afterData = db.getCustomers();
		showData("AFTER", afterData);
		assertThat(afterData.size()).isEqualTo(beforeData.size());
		Customer actualP = afterData.get(0);
		assertWithMessage("'id' field").that(actualP.id()).isEqualTo(origP.id());
		assertWithMessage("'name' field").that(actualP.name()).isEqualTo(newName);
		assertWithMessage("'address' field").that(actualP.address()).isEqualTo(origP.address());
	}
	
	/** Test updating a nonexistent Customer */
	@Test
	public void testUpdateCustomer_BadId() {
		List<Customer> beforeData = db.getCustomers();
		showData("BEFORE", beforeData);

		Customer newP = Customer.builder().id(TEST_ID_BAD).name("Nosuch User").address("123 Anystreet").build();
		logger.info("New Customer: {}", newP);
		try {
			db.updateCustomer(newP);
			fail("Updating a nonexistent customer was expected to throw an Exception, but did not");
		} catch (IllegalArgumentException expected) {
			logger.info("Updating a nonexistent customer threw an Exception (AS EXPECTED)", expected);
		}

		List<Customer> afterData = db.getCustomers();
		showData("AFTER", afterData);
		assertThat(afterData.size()).isEqualTo(beforeData.size());
	}
	
	/** Test deleting a customer */
	@Test
	public void testDeleteCustomer() {
		List<Customer> beforeData = db.getCustomers();
		showData("BEFORE", beforeData);
		// Create a new Customer (so that we can delete them)
		Customer.Builder newPB = Customer.builder().name("New Person To Be Deleted")
			.address("123 Sesame Street");
		Customer newP = db.createCustomer(newPB);
		logger.info("New Customer: " + newP);
		
		// Delete the customer
		db.deleteCustomer(newP.id());

		// Redisplay all the data, to prove that the new Customer (added above) was deleted
		List<Customer> afterData = db.getCustomers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Customer list")
			.that(afterData.size())
			.isEqualTo(beforeData.size());
		assertThat(afterData).doesNotContain(newP);
	}
	
	/** Test deleting a nonexistent customer */
	@Test
	public void testDeleteCustomer_BadId() {
		List<Customer> beforeData = db.getCustomers();
		showData("BEFORE", beforeData);
		
		// Attempt to delete a nonexistent customer
		try {
			db.deleteCustomer(TEST_ID_BAD);
			fail("Deleting a nonexistent customer was expected to throw an Exception, but did not");
		} catch (IllegalArgumentException expected) {
			logger.info("Deleting a nonexistent customer threw an Exception (AS EXPECTED)", expected);
		}

		// Redisplay all the data, to prove that the Customer was not deleted, nor were any other
		// changes made to the list-of-customers
		List<Customer> afterData = db.getCustomers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Customer list")
			.that(afterData.size())
			.isEqualTo(beforeData.size());
	}
	
	// Utility routines below
	
	private void showData(String tag, List<Customer> data) {
		logger.info("{}: Customers table contains {} rows", tag, data.size());
		for (Customer p : data) {
			logger.info("  Customer: {}", p);
		}
	}
}
