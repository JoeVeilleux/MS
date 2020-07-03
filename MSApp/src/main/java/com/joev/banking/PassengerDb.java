package com.joev.banking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joev.banking.Passenger;
import com.joev.util.SimpleJsonDb;

public class PassengerDb {
	private static final Logger logger = LogManager.getLogger(PassengerDb.class);

	protected static final String DB = "Passenger";
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
	
	public List<Passenger> getPassengers() {
		List<Passenger.Builder> passengerBuilders = db.readTable(Passenger.Builder.class, "Passengers");
		List<Passenger> passengers = new ArrayList<>();
		for (Passenger.Builder b : passengerBuilders) {
			passengers.add(b.build());
		}
		return passengers;
	}
	
	public void putPassengers(List<Passenger> passengers) {
		db.writeTable("Passengers", passengers);
	}
	
	public Passenger createPassenger(Passenger.Builder passenger) {
		logger.info("Creating Passenger with: {}", passenger);
		if (!passenger.id().isPresent()) {
			passenger.id(String.valueOf(db.nextId()));
			logger.info("Assigned next available id: {}", passenger.id().get());
		}
		List<Passenger> passengers = getPassengers();
		passengers.add(passenger.build());
		putPassengers(passengers);
		return passenger.build();
	}
	
	public Passenger readPassenger(String id) {
		logger.info("Reading Passenger with id: {}", id);
		List<Passenger> passengers = getPassengers();
		for (Passenger p : passengers) {
			if (p.id().equals(id)) {
				return p;
			}
		}
		logger.warn("Cannot locate Passenger with id: {}", id);
		return null;
	}
	
	public void updatePassenger(Passenger passenger) {
		logger.info("Updating Passenger: {}", passenger);
		List<Passenger> passengers = getPassengers();
		List<Passenger> newPassengers = new ArrayList<>();
		boolean foundPassenger = false;
		for (Passenger p : passengers) {
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
			logger.warn("Cannot locate Passenger with id: {}", passenger.id());
			throw new IllegalArgumentException("Could not update passsenger "
					+ passenger.id() + ". Not found in database.");
		}
	}
	
	public void deletePassenger(String id) {
		logger.info("Deleting Passenger with id: {}", id);
		List<Passenger> passengers = getPassengers();
		List<Passenger> newPassengers = new ArrayList<>();
		boolean foundPassenger = false;
		for (Passenger p : passengers) {
			if (p.id().equals(id)) {
				foundPassenger = true;
			} else {
				newPassengers.add(p);
			}
		}
		if (foundPassenger) {
			putPassengers(newPassengers);
		} else {
			logger.warn("Cannot locate Passenger with id: {}", id);
			throw new IllegalArgumentException("Could not delete passsenger "
					+ id + ". Not found in database.");
		}
	}
}
