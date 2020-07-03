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

public class PassengerDbTest {
	private static final Logger logger = LogManager.getLogger(PassengerDbTest.class);
	
	@Rule public TestName testName = new TestName();

	private static final String TEST_ID = "100";
	private static final String TEST_ID_BAD = "999";
	private static final int EXPECTED_NROWS = 4; // Number of rows in sample/test data
	
	public PassengerDb db;
	
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
		db = new PassengerDb();
		// Initialize the test databases using 'seed' contents from test/resources
		db.initTables();
	}
	
	@After
	public void after() throws Exception {
		// Remove the test databases
		db.rmTables();
	}
	
	/** Test getting the Passengers list */
	@Test
	public void testGetPassengers() {
		List<Passenger> data = db.getPassengers();
		showData("DATA", data);
		assertThat(data.size()).isEqualTo(EXPECTED_NROWS);
	}
	
	/** Test creating a new Passenger */
	@Test
	public void testCreatePassenger() {
		List<Passenger> beforeData = db.getPassengers();
		showData("BEFORE", beforeData);

		// Create a new Passenger
		Passenger.Builder newPB = Passenger.builder().name("New Person").address("123 Sesame Street");
		Passenger newP = db.createPassenger(newPB);
		logger.info("New Passenger: " + newP);

		// Redisplay all the data, to prove that the new Passenger was added
		List<Passenger> afterData = db.getPassengers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Passenger list")
			.that(afterData.size())
			.isEqualTo(beforeData.size() + 1);
		assertThat(afterData).contains(newP);
	}
	
	/** Test reading a specific passenger's information */
	@Test
	public void testReadPassenger() {
		// Read a Passenger
		Passenger p = db.readPassenger(TEST_ID);
		logger.info("Passenger with id={}: {}", TEST_ID, p);

		assertThat(p).isNotNull();
		assertThat(p.id()).isEqualTo(TEST_ID);
	}
	
	/** Test reading a nonexistent passenger's information. */
	@Test
	public void testReadPassenger_BadId() {
		// Read a Passenger that does not exist
		Passenger p = db.readPassenger(TEST_ID_BAD);
		logger.info("Passenger with id={}: {}", TEST_ID_BAD, p);

		assertThat(p).isNull();
	}
	
	/** Test updating a Passenger */
	@Test
	public void testUpdatePassenger() {
		List<Passenger> beforeData = db.getPassengers();
		showData("BEFORE", beforeData);
		Passenger origP = beforeData.get(0);
		logger.info("Original Passenger: {}", origP);

		String newName = origP.name() + "X";
		Passenger newP = Passenger.builder().id(origP.id()).name(newName).address(origP.address()).build();
		logger.info("New Passenger: {}", newP);
		db.updatePassenger(newP);

		List<Passenger> afterData = db.getPassengers();
		showData("AFTER", afterData);
		assertThat(afterData.size()).isEqualTo(beforeData.size());
		Passenger actualP = afterData.get(0);
		assertWithMessage("'id' field").that(actualP.id()).isEqualTo(origP.id());
		assertWithMessage("'name' field").that(actualP.name()).isEqualTo(newName);
		assertWithMessage("'address' field").that(actualP.address()).isEqualTo(origP.address());
	}
	
	/** Test updating a nonexistent Passenger */
	@Test
	public void testUpdatePassenger_BadId() {
		List<Passenger> beforeData = db.getPassengers();
		showData("BEFORE", beforeData);

		Passenger newP = Passenger.builder().id(TEST_ID_BAD).name("Nosuch User").address("123 Anystreet").build();
		logger.info("New Passenger: {}", newP);
		try {
			db.updatePassenger(newP);
			fail("Updating a nonexistent passenger was expected to throw an Exception, but did not");
		} catch (IllegalArgumentException expected) {
			logger.info("Updating a nonexistent passenger threw an Exception (AS EXPECTED)", expected);
		}

		List<Passenger> afterData = db.getPassengers();
		showData("AFTER", afterData);
		assertThat(afterData.size()).isEqualTo(beforeData.size());
	}
	
	/** Test deleting a passenger */
	@Test
	public void testDeletePassenger() {
		List<Passenger> beforeData = db.getPassengers();
		showData("BEFORE", beforeData);
		// Create a new Passenger (so that we can delete them)
		Passenger.Builder newPB = Passenger.builder().name("New Person To Be Deleted")
			.address("123 Sesame Street");
		Passenger newP = db.createPassenger(newPB);
		logger.info("New Passenger: " + newP);
		
		// Delete the passenger
		db.deletePassenger(newP.id());

		// Redisplay all the data, to prove that the new Passenger (added above) was deleted
		List<Passenger> afterData = db.getPassengers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Passenger list")
			.that(afterData.size())
			.isEqualTo(beforeData.size());
		assertThat(afterData).doesNotContain(newP);
	}
	
	/** Test deleting a nonexistent passenger */
	@Test
	public void testDeletePassenger_BadId() {
		List<Passenger> beforeData = db.getPassengers();
		showData("BEFORE", beforeData);
		
		// Attempt to delete a nonexistent passenger
		try {
			db.deletePassenger(TEST_ID_BAD);
			fail("Deleting a nonexistent passenger was expected to throw an Exception, but did not");
		} catch (IllegalArgumentException expected) {
			logger.info("Deleting a nonexistent passenger threw an Exception (AS EXPECTED)", expected);
		}

		// Redisplay all the data, to prove that the Passenger was not deleted, nor were any other
		// changes made to the list-of-passengers
		List<Passenger> afterData = db.getPassengers();
		showData("AFTER", afterData);
		assertWithMessage("Size of Passenger list")
			.that(afterData.size())
			.isEqualTo(beforeData.size());
	}
	
	// Utility routines below
	
	private void showData(String tag, List<Passenger> data) {
		logger.info("{}: Passengers table contains {} rows", tag, data.size());
		for (Passenger p : data) {
			logger.info("  Passenger: {}", p);
		}
	}
}
