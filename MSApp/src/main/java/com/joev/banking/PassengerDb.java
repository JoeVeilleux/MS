package com.joev.banking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joev.util.SimpleJsonDb;

public class PassengerDb {
	private static final Logger logger = LogManager.getLogger(PassengerDb.class);

	protected static final String DB = "Customer";
	private static final String TABLE = "Passengers";
	
	private static final SimpleJsonDb db = new SimpleJsonDb(DB);
	
	public void initTables() throws IOException {
		db.initTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.initTable(TABLE);
	}
	
	public void rmTables() throws IOException {
		db.rmTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.rmTable(TABLE);
	}
	
	public List<Customer> getPassengers() {
		List<Customer.Builder> passengerBuilders = db.readTable(Customer.Builder.class, "Passengers");
		List<Customer> passengers = new ArrayList<>();
		for (Customer.Builder b : passengerBuilders) {
			passengers.add(b.build());
		}
		return passengers;
	}
	
	public void putPassengers(List<Customer> passengers) {
		db.writeTable("Passengers", passengers);
	}
	
	public Customer createPassenger(Customer.Builder passenger) {
		logger.info("Creating Customer with: {}", passenger);
		if (!passenger.id().isPresent()) {
			passenger.id(String.valueOf(db.nextId()));
			logger.info("Assigned next available id: {}", passenger.id().get());
		}
		List<Customer> passengers = getPassengers();
		passengers.add(passenger.build());
		putPassengers(passengers);
		return passenger.build();
	}
	
	public Customer readPassenger(String id) {
		logger.info("Reading Customer with id: {}", id);
		List<Customer> passengers = getPassengers();
		for (Customer p : passengers) {
			if (p.id().equals(id)) {
				return p;
			}
		}
		logger.warn("Cannot locate Customer with id: {}", id);
		return null;
	}
	
	public void updatePassenger(Customer passenger) {
		logger.info("Updating Customer: {}", passenger);
		List<Customer> passengers = getPassengers();
		List<Customer> newPassengers = new ArrayList<>();
		boolean foundPassenger = false;
		for (Customer p : passengers) {
			if (p.id().equals(passenger.id())) {
				foundPassenger = true;
				newPassengers.add(passenger);
			} else {
				newPassengers.add(p);
			}
		}
		if (foundPassenger) {
			putPassengers(newPassengers);
		} else {
			logger.warn("Cannot locate Customer with id: {}", passenger.id());
			throw new IllegalArgumentException("Could not update passsenger "
					+ passenger.id() + ". Not found in database.");
		}
	}
	
	public void deletePassenger(String id) {
		logger.info("Deleting Customer with id: {}", id);
		List<Customer> passengers = getPassengers();
		List<Customer> newPassengers = new ArrayList<>();
		boolean foundPassenger = false;
		for (Customer p : passengers) {
			if (p.id().equals(id)) {
				foundPassenger = true;
			} else {
				newPassengers.add(p);
			}
		}
		if (foundPassenger) {
			putPassengers(newPassengers);
		} else {
			logger.warn("Cannot locate Customer with id: {}", id);
			throw new IllegalArgumentException("Could not delete passsenger "
					+ id + ". Not found in database.");
		}
	}
}
