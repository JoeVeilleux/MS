package com.joev.banking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.joev.util.SimpleJsonDb;

public class CustomerDb {
	private static final Logger logger = LogManager.getLogger(CustomerDb.class);

	protected static final String DB = "Customer";
	private static final String TABLE = "Customers";
	
	private static final SimpleJsonDb db = new SimpleJsonDb(DB);
	
	public void initTables() throws IOException {
		db.initTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.initTable(TABLE);
	}
	
	public void rmTables() throws IOException {
		db.rmTable(SimpleJsonDb.DB_METADATA_TABLE);
		db.rmTable(TABLE);
	}
	
	public List<Customer> getCustomers() {
		List<Customer.Builder> customerBuilders = db.readTable(Customer.Builder.class, "Customers");
		List<Customer> customers = new ArrayList<>();
		for (Customer.Builder b : customerBuilders) {
			customers.add(b.build());
		}
		return customers;
	}
	
	public void putCustomers(List<Customer> customers) {
		db.writeTable("Customers", customers);
	}
	
	public Customer createCustomer(Customer.Builder customer) {
		logger.info("Creating Customer with: {}", customer);
		if (!customer.id().isPresent()) {
			customer.id(String.valueOf(db.nextId()));
			logger.info("Assigned next available id: {}", customer.id().get());
		}
		List<Customer> customers = getCustomers();
		customers.add(customer.build());
		putCustomers(customers);
		return customer.build();
	}
	
	public Customer readCustomer(String id) {
		logger.info("Reading Customer with id: {}", id);
		List<Customer> customers = getCustomers();
		for (Customer p : customers) {
			if (p.id().equals(id)) {
				return p;
			}
		}
		logger.warn("Cannot locate Customer with id: {}", id);
		return null;
	}
	
	public void updateCustomer(Customer customer) {
		logger.info("Updating Customer: {}", customer);
		List<Customer> customers = getCustomers();
		List<Customer> newCustomers = new ArrayList<>();
		boolean foundCustomer = false;
		for (Customer p : customers) {
			if (p.id().equals(customer.id())) {
				foundCustomer = true;
				newCustomers.add(customer);
			} else {
				newCustomers.add(p);
			}
		}
		if (foundCustomer) {
			putCustomers(newCustomers);
		} else {
			logger.warn("Cannot locate Customer with id: {}", customer.id());
			throw new IllegalArgumentException("Could not update passsenger "
					+ customer.id() + ". Not found in database.");
		}
	}
	
	public void deleteCustomer(String id) {
		logger.info("Deleting Customer with id: {}", id);
		List<Customer> customers = getCustomers();
		List<Customer> newCustomers = new ArrayList<>();
		boolean foundCustomer = false;
		for (Customer p : customers) {
			if (p.id().equals(id)) {
				foundCustomer = true;
			} else {
				newCustomers.add(p);
			}
		}
		if (foundCustomer) {
			putCustomers(newCustomers);
		} else {
			logger.warn("Cannot locate Customer with id: {}", id);
			throw new IllegalArgumentException("Could not delete passsenger "
					+ id + ". Not found in database.");
		}
	}
}
