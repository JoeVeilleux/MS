package com.joev.util;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class SimpleJsonDbTest {
	private static final Logger logger = LogManager.getLogger(SimpleJsonDbTest.class);
	
	@Rule public TestName testName = new TestName();

	private static final String DB = "SimpleJsonDbSample";
	private static final String TABLE = "MyTable";
	private static final int EXPECTED_NROWS_BEFORE = 2;
	
	public SimpleJsonDb db;
	
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
		db = new SimpleJsonDb(DB);
		db.initTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.initTable(TABLE);
	}
	
	@After
	public void after() throws Exception {
		// Clean up tables created for this test
		db.rmTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.rmTable(TABLE);
	}
	
	/** Test reading a table */
	@Test
	public void testReadTable() {
		List<MyTableRow> rows = db.readTable(MyTableRow.class, TABLE);
		logger.info("Table contains {} rows", rows.size());
		for (MyTableRow row : rows) {
			logger.info("Row: {}", row);
		}
		assertThat(rows.size()).isEqualTo(EXPECTED_NROWS_BEFORE);
	}
	
	/** Test reading, modifying, and then re-writing a table */
	@Test
	public void testWriteTable() {
		List<MyTableRow> rows = db.readTable(MyTableRow.class, TABLE);
		showMyTable("BEFORE", rows);
		// Modify a specific value (column 's1' in the first row)...
		String newValue = "Updated value of Row 1 S1";
		rows.get(0).s1 = newValue;
		// Write out the modified table
		db.writeTable(TABLE, rows);
		// Read it back in and display it, to prove that it was updated
		rows = db.readTable(MyTableRow.class, TABLE);
		showMyTable("AFTER", rows);
		assertThat(rows.size()).isEqualTo(EXPECTED_NROWS_BEFORE);
		assertWithMessage("Updated row 1 s2").that(rows.get(0).s1).isEqualTo(newValue);
	}
	
	/** Test db.nextId() by calling it repeatedly */
	@Test
	public void testNextId() {
		int origNextId = db.nextId();
		for (int n = 1; n < 5; n++) {
			int nextId = db.nextId();
			assertWithMessage("Id of new item #" + n).that(nextId).isEqualTo(origNextId + n);
		}
	}
	
	/** Test creating a new item in the database, including assigning it a unique id */
	@Test
	public void testNewItem() {
		List<MyTableRow> rows = db.readTable(MyTableRow.class, TABLE);
		showMyTable("BEFORE", rows);
		// Create a new item
		MyTableRow newRow = new MyTableRow();
		String nextId = String.valueOf(db.nextId());
		logger.info("Id of new item: {}", nextId);
		newRow.id = nextId;
		newRow.s1 = "New ROW!!! S1";
		newRow.s2 = "New ROW!!! S2";
		rows.add(newRow);
		// Write out the modified table
		db.writeTable(TABLE, rows);
		// Redisplay it, to prove that it was updated
		rows = db.readTable(MyTableRow.class, TABLE);
		showMyTable("AFTER", rows);
		assertThat(rows.size()).isEqualTo(EXPECTED_NROWS_BEFORE + 1);
		assertWithMessage("Id of new item").that(rows.get(rows.size() - 1).id).isEqualTo(nextId);
	}
	
	// Utility routines below
	
	private void showMyTable(String tag, List<MyTableRow> rows) {
		logger.info("{}: Table contains {} rows", tag, rows.size());
		for (MyTableRow row : rows) {
			logger.info("{}: Row: {}", tag, row);
		}
	}
	
	private static class MyTableRow {
		@JsonProperty("id") public String id;
		@JsonProperty("s1") public String s1;
		@JsonProperty("s2") public String s2;
		public String toString() {
			return String.format("id='%s' s1='%s' s2='%s'", id, s1, s2);
		}
	}

}
