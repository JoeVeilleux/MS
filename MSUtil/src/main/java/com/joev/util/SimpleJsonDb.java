package com.joev.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.type.TypeFactory;

public class SimpleJsonDb {

	private static final Logger logger = LogManager.getLogger(SimpleJsonDb.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String DEFAULT_DB_DIR = "/tmp/db";
	private static String dbDir = DEFAULT_DB_DIR;
	public static final String DB_METADATA_TABLE = "Metadata";

	protected final String dbName;

	/**
	 * Create a test database parent directory and use it for this execution
	 * 
	 * @throws IOException
	 */
	public static void mkTestDbDir() throws IOException {
		dbDir = Files.createTempDirectory("dbtest").toString();
		logger.info("Using test dbDir={}", dbDir);
	}

	/**
	 * Clean up and delete the test database parent directory. Note that this
	 * routine will *not* remove the real/default database directory; it is intended
	 * to be used only during testing, when a temporary test database directory has
	 * been created (e.g. by mkTestDbDir, above).
	 * 
	 * @throws IOException
	 */
	public static void rmTestDbDir() throws IOException {
		if (dbDir.equals(DEFAULT_DB_DIR)) {
			logger.error("NOT REMOVING DATABASE DIRECTORY: {}", dbDir);
		} else {
			logger.info("Removing test dbDir={}", dbDir);
			File dbDirFile = new File(dbDir);
			for (File f : dbDirFile.listFiles()) {
				f.delete();
			}
			dbDirFile.delete();
		}
	}

	public SimpleJsonDb(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @param table table name
	 * @return Filename in which the specified database table is stored
	 */
	String getDbFilename(String table) {
		return String.format("/DB_%s_%s.json", dbName, table);
	}

	/**
	 * @param table table name
	 * @return File in which the specified database table is stored
	 */
	File getDbFile(String table) {
		return new File(dbDir, getDbFilename(table));
	}

	/**
	 * Initialize a database table file: if it doesn't already exist, create it by
	 * copying a sample from the program's resources directory to the directory
	 * where it should be.
	 * 
	 * @param table table name
	 * @throws IOException
	 */
	public void initTable(String table) throws IOException {
		File dbFile = getDbFile(table);
		if (dbFile.exists()) {
			logger.info("initTable: Database={} table={} already exists");
		} else {
			InputStream is = getClass().getResourceAsStream(getDbFilename(table));
			// TODO: Handle the case where there is no sample in the resources (create
			// empty)
			Path target = Paths.get(dbFile.toURI());
			logger.info("initTable: Creating database={} table={}:", dbName, table);
			logger.info("  Source: resource {}", dbFile.getName());
			logger.info("  Target: file {}", target);
			Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	/**
	 * Delete a database table file
	 * 
	 * @param table table name
	 * @throws IOException
	 */
	public void rmTable(String table) throws IOException {
		File dbFile = getDbFile(table);
		if (dbFile.exists()) {
			logger.info("rmTable: Removing database={} table={}", dbName, table);
			dbFile.delete();
		}
	}

	/**
	 * Read a table
	 * 
	 * @param <T>   Type defining the rows to be returned
	 * @param clazz Class of the rows to be returned
	 * @param table Name of the table
	 * @return list of rows; each row is of type T
	 */
	public <T> List<T> readTable(Class<T> clazz, String table) {
		File dbFile = getDbFile(table);
		logger.info("readTable(): Reading from: " + dbFile.getAbsolutePath());
		List<T> contents = new ArrayList<>();
		try (FileInputStream is = new FileInputStream(dbFile)) {
			contents = mapper.readValue(is, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contents;
	}

	/**
	 * (Re)write a database table, with possibly updated contents
	 * 
	 * @param table Name of the table
	 * @param value the contents of the table (list of objects)
	 */
	public void writeTable(String table, Object value) {
		File dbFile = getDbFile(table);
		logger.info("writeTable(): Writing to: " + dbFile.getAbsolutePath());
		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
		try {
			writer.writeValue(dbFile, value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get the next unique id to be assigned to a newly-created database item, and
	 * increment the 'nextId' counter so that the next call will get the next value.
	 * 
	 * @return a unique Id value to assign to a newly-created item
	 */
	public int nextId() {
		List<DbMetadata> dbMetadata = readTable(DbMetadata.class, DB_METADATA_TABLE);
		int nextId = dbMetadata.get(0).nextId;
		dbMetadata.get(0).nextId++;
		writeTable(DB_METADATA_TABLE, dbMetadata);
		return nextId;
	}

	// TODO: Consider generalizing this by making it a simple Map<String, Object>?
	private static class DbMetadata {
		@JsonProperty("nextId")
		public Integer nextId;
	}

}
