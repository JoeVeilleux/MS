package com.joev.ridesharing;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.NotFoundException;

@Path("/")
public class PassengerServlet {
	private static final Logger logger = LogManager.getLogger(PassengerServlet.class);
	// TODO: Need more logging in this module

	private PassengerDb passengerDb = new PassengerDb();

	@Context
	UriInfo uriInfo;

	@Context
	Request request;

	/**
	 * Create a new Passenger using the data provided in the payload
	 * 
	 * @param passengerBuilder the data for the new Passenger
	 * @return Response with: Status=201 CREATED; Location header containing the URL
	 *         to the newly-created item; Body containing a message acknowledging
	 *         successful creation (showing ID of the new item)
	 */
	@POST
	@Path("passengers")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPassenger(Passenger.Builder passengerBuilder) {
		Passenger passenger = passengerDb.createPassenger(passengerBuilder);
		URI uri = uriInfo.getRequestUri();
		URI newItemUri = UriBuilder.fromUri(uri).path("{id}").build(passenger.id());
		return Response.created(newItemUri).type(MediaType.TEXT_PLAIN)
				.entity("Created new Passenger with id=" + passenger.id()).build();
	}

	/**
	 * Get a list of all passengers, as text
	 * 
	 * @return list of Passengers in text form
	 */
	@GET
	@Path("/passengers")
	@Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	public String getPassengersListAsText() {
		return getPassengersList().toString();
	}

	/**
	 * Get a list of all passengers, as JSON
	 * 
	 * @return list of Passengers in JSON form
	 */
	@GET
	@Path("/passengers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Passenger> getPassengersList() {
		List<Passenger> data = passengerDb.getPassengers();
		return data;
	}

	/**
	 * Get details for a specific passenger, as text
	 * 
	 * @return data for the requested passenger (as text), or NOT_FOUND if there is
	 *         no such passenger
	 */
	@GET
	@Path("/passengers/{id}")
	@Produces({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
	public String getPassengerAsText(@PathParam("id") String id) {
		return getPassenger(id).toString();
	}

	/**
	 * Get details for a specific passenger, as JSON
	 * 
	 * @return data for the requested passenger (as JSON), or NOT_FOUND if there is
	 *         no such passenger
	 */
	@GET
	@Path("/passengers/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Passenger getPassenger(@PathParam("id") String id) {
		Passenger p = passengerDb.readPassenger(id);
		if (p == null) {
			throw new NotFoundException("Passenger '" + id + "' not found!");
		}
		return p;
	}

	/**
	 * Update an existing Passenger using the data provided in the payload
	 * 
	 * @param passengerBuilder the data for the new Passenger
	 * @return Response with: Status=200 OK; Body containing a message acknowledging
	 *         successful update of the item
	 */
	@PUT
	@Path("/passengers/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updatePassenger(@PathParam("id") String id, Passenger.Builder passengerBuilder) {
		if (!passengerBuilder.id().isPresent()) {
			// Passenger id wasn't specified in the payload; get it from the URL and plug it in
			passengerBuilder.id(id);
		} else if (!passengerBuilder.id().get().equals(id)) {
			// Passenger id was specified both on the URL and in the request body, with different
			// values!
			String errMsg = String.format(
				"ERROR: Unable to update Passenger. ID specified with conflicting values:"
				+ " ID from URL=%s, ID from request body=%s.",
				id, passengerBuilder.id().get());
			logger.error("updatePassenger(): {}", errMsg);
			return Response.ok().status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN)
					.entity(errMsg).build();
		}
		Passenger passenger = passengerBuilder.build();
		try {
			passengerDb.updatePassenger(passenger);
			return Response.ok().build();
		} catch (IllegalArgumentException e) {
			throw new NotFoundException("Passenger '" + id + "' not found!");
		}
	}

	/**
	 * Delete an existing Passenger
	 * 
	 * @param id of Passenger to be deleted
	 * @return Status=200 OK if successful; Status=404 NOT_FOUND if no Passenger
	 *         with that ID was found. Body will contain success/failure message as
	 *         text.
	 */
	@DELETE
	@Path("/passengers/{id}")
	public Response deletePassenger(@PathParam("id") String id) {
		try {
			passengerDb.deletePassenger(id);
			return Response.ok().type(MediaType.TEXT_PLAIN).entity("Passenger '" + id + "' deleted")
				.build();
		} catch (IllegalArgumentException e) {
			throw new NotFoundException("Passenger '" + id + "' not found!");
		}
	}

}
