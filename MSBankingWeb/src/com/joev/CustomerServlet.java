package com.joev;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.joev.banking.Customer;
import com.joev.util.SHClient;
import com.joev.util.SHClient.SHResp;

/**
 * Servlet implementation class CustomerServlet
 */
@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CustomerServlet.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	// Base URL for calling the REST service
	// TODO: Get from somewhere like a config file
	String URL_BASE = "http://localhost:8000";

	String baseUri;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CustomerServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	// HTML to appear at the top of every page.
	// TODO: find a way to define this globally (use in JSP and also here in Java code)
	private static final String htmlPageTop = "<html>\n" + "<head>\n" + "<meta charset=\"UTF-8\">\n"
			+ "<title>Banking Web UI</title>\n"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"Banking.css\">\n" + "</head>\n" + "<body>\n"
			+ "<div class=\"appTitle\">\n"
			+ "<a href=\"%s\"><button class=\"appTitleButton\" type=\"button\" id=\"nav_home\">\n"
			+ "<img src=\"bank.png\" alt=\"Banking Web UI\" style=\"width:280px;height:40px;\">\n"
			+ "</button></a>\n" + "</div>\n" + "";

	private String getHtmlPageTop() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(htmlPageTop, baseUri));
		return sb.toString();
	}

	private String getHtmlPageBottom() {
		StringBuilder sb = new StringBuilder();
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	private String getHtmlCustomerDetail(Customer p) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");
		sb.append("<tr><td>Id</td><td>" + p.id() + "</td></tr>");
		sb.append("<tr><td>Name</td><td>" + p.name() + "</td></tr>");
		sb.append("<tr><td>Address</td><td>" + p.address() + "</td></tr>");
		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("doGet(): request={}{}", request.getRequestURL(),
				(request.getQueryString() == null ? "" : "?" + request.getQueryString()));
		baseUri = request.getContextPath();

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		StringBuilder body = new StringBuilder();

		String reqId = request.getParameter("id");
		if (reqId == null) {
			// No id specified; show the list of all Customers
			body.append(getHtmlPageTop());
			body.append("<h1>Manage Customers</h1>");
			body.append("<p>Click the Id of a customer to see their details and perform actions."
					+ " Click the 'New Customer' button to create a new customer.</p>");
			SHResp srvResponse = SHClient.doHttp(URL_BASE + "/customers", SHClient.RM.GET, null, SHClient.RP.ACC_JSON);
			SHClient.logResponse(srvResponse);
			String data = srvResponse.responseBody;
			Customer.Builder customers[] = mapper.readValue(data, Customer.Builder[].class);
			body.append("<table border='1'>");
			body.append("<tr><th>Id</th><th>Name</th><th>Address</th></tr>");
			for (Customer.Builder pb : customers) {
			    Customer p = pb.build();
				body.append("<tr>").append("<td>")
						.append("<a href=" + request.getRequestURL() + "?id=" + p.id() + ">" + p.id() + "</a>")
						.append("</td>").append("<td>").append(p.name()).append("</td>").append("<td>").append(p.address())
						.append("</td>").append("</tr>");
			}
			body.append("</table>");
			// Add "New" button to create a new Customer...
			body.append("<div class=\"actions\">");
			body.append(
					"<a href=\"newcustomer.jsp\">"
					+ "<button type=\"button\" id=\"new_customer\">New Customer</button>"
					+ "</a>");
			body.append("</div>");
		} else {
			// A customer-id was specified
			// Get the Customer's current details
			SHResp srvResponse = SHClient.doHttp(URL_BASE + "/customers/" + reqId, SHClient.RM.GET, null,
					SHClient.RP.ACC_JSON);
			SHClient.logResponse(srvResponse);
			String data = srvResponse.responseBody;
			Customer.Builder pb = mapper.readValue(data, Customer.Builder.class);
			Customer p = pb.build();
			// See what action was requested
			String action = request.getParameter("action");
			if (action == null) {
				// No 'action' specified; show customer details, with buttons to Edit / Delete
				body.append(getHtmlPageTop());
				body.append("<h1>Customer " + reqId + " Details</h1>");
				body.append(getHtmlCustomerDetail(p));
				body.append("<div class=\"actions\">");
				body.append("<a href=" + request.getRequestURL() + ">"
						+ "<button type=\"button\" id=\"cancel\">Cancel</button></a>");
				body.append("<a href=" + request.getRequestURL() + "?id=" + p.id() + "&action=edit>"
						+ "<button type=\"button\" id=\"edit_customer\">Edit Customer</button></a>");
				body.append("<a href=" + request.getRequestURL() + "?id=" + p.id() + "&action=confirmdelete>"
						+ "<button type=\"button\" id=\"del_customer\">Delete Customer</button></a>");
				body.append("</div>");
			} else if (action.equals("edit")) {
				// action=edit: show the customer's details in editable mode
				body.append(getHtmlPageTop());
				String header = "Edit Customer";
				String instructions = "Make any desired changes and click 'Save' to commit, or click"
						+ " 'Cancel' to quit without modifying the customer.";
				body.append(
					String.format(
						"<h1>%s</h1>\n" + 
						"<p>%s</p>\n" + 
						"<form action=\"customers\" method=\"post\">\n" + 
						"  <table border=\"1\">\n" + 
						"    <tr><th>Id</th><td><input type=\"text\" name=\"id\" size=\"5\" value=\"%s\" readonly></td></tr>\n" + 
						"    <tr><th>Name</th><td><input type=\"text\" name=\"name\" size=\"20\" value=\"%s\"></td></tr>\n" + 
						"    <tr><th>Address</th><td><input type=\"text\" name=\"address\" size=\"40\" value=\"%s\"></td></tr>\n" + 
						"  </table>\n" + 
						"  <button type=\"submit\" name=\"button\" value=\"Cancel\">Cancel</button>\n" + 
						"  <button type=\"submit\" name=\"button\" value=\"Submit\">Submit</button>\n" + 
						"</form>\n",
						header, instructions, p.id(), p.name(), p.address()
					)
				);
			} else if (action.equals("confirmdelete")) {
				// action=confirmdelete: show the customer's details, with a 'Confirm' button
				body.append(getHtmlPageTop());
				body.append("<h1>Confirm Customer " + reqId + " Delete</h1>");
				body.append(getHtmlCustomerDetail(p));
				body.append("<p>Click the 'Confirm' button to confirm that you would like to delete"
						+ " this customer, or click 'Cancel' to quit without deleting the customer.</p>");
				body.append("<div class=\"actions\">");
				body.append("<a href=" + request.getRequestURL() + ">"
						+ "<button type=\"button\" id=\"cancel\">Cancel</button></a>");
				body.append("<a href=" + request.getRequestURL() + "?id=" + p.id() + "&action=delete>"
						+ "<button type=\"button\" id=\"confirm\">Confirm</button></a>");
				body.append("</div>");
			} else if (action.equals("delete")) {
				// action=delete: delete the Customer
				srvResponse = SHClient.doHttp(URL_BASE + "/customers/" + reqId, SHClient.RM.DELETE, null,
						SHClient.RP.ACC_TEXT);
				SHClient.logResponse(srvResponse);
				body.append(getHtmlPageTop());
				if (srvResponse.responseCode == 200) {
					body.append("<h2>Customer " + reqId + " Deleted Successfully</h2>");
				} else {
					body.append("<p>RESULT: responseCode=" + srvResponse.responseCode + "</p>");
				}
				// TODO: Error checking all up in here
			} else {
				// TODO: UNRECOGNIZED ACTION!!! Throw an Exception or CLIENT_ERROR or something...
			}
		}
		body.append(getHtmlPageBottom());

		PrintWriter writer = response.getWriter();
		writer.append(body.toString());
		writer.close();
		response.setContentLength(body.length());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("doPost(): request={}{}", request.getRequestURL(),
				(request.getQueryString() == null ? "" : "?" + request.getQueryString()));
		// Don't commit this transaction if the user clicked 'Cancel'
		String button = request.getParameter("button");
		logger.info("doPost: User clicked the '{}' button", button);
		if (button.equalsIgnoreCase("Submit")) {
			// User clicked 'Submit': Commit the transaction
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String address = request.getParameter("address");
			String url;
			SHClient.RM requestMethod;
			String requestBody;
			if (id == null) {
				// No id, so this is a request to create a new Customer
				url = URL_BASE + "/customers";
				requestMethod = SHClient.RM.POST;
				requestBody = "{\"name\":\"" + name + "\",\"address\":\"" + address + "\"}";
			} else {
				// id specified, so this is a request to update an existing Customer
				url = URL_BASE + "/customers/" + id;
				requestMethod = SHClient.RM.PUT;
				// TODO: Create JSON by creating new Customer.Builder using these values, then JSON-izing
				requestBody = "{\"id\":\"" + id + "\", \"name\":\"" + name + "\",\"address\":\"" + address + "\"}";
			}
			SHResp srvResponse = SHClient.doHttp(url, requestMethod, requestBody,
					SHClient.RP.CON_JSON, SHClient.RP.ACC_JSON);
			SHClient.logResponse(srvResponse);
			// If successful, just redirect to the GET handler
			if (srvResponse.responseCode == 200 || srvResponse.responseCode == 201) {
				doGet(request, response);
			} else {
				StringBuilder body = new StringBuilder();
				body.append(getHtmlPageTop());
				body.append("<h1>ERROR: Unable to " +
					(id == null ? "create new customer!" : "modify customer " + id + "!") +
					"</h1>");
				body.append("<h2>Request Details</h2>");
				body.append("<p>URL: " + url);
				body.append("<br>Request method: " + requestMethod);
				body.append("<br>Request body: " + requestBody);
				body.append("<h2>Response Details</h2>");
				body.append("<p>Response code: " + srvResponse.responseCode + "/" + srvResponse.responseMessage);
				body.append("<br>Response body: " + srvResponse.responseBody);
				body.append("<br>Response headers: " + srvResponse.responseHeaders + "</p>");
				// TODO: Action buttons?
				body.append(getHtmlPageBottom());
				PrintWriter writer = response.getWriter();
				writer.append(body.toString());
				writer.close();
			}
		} else {
			// User clicked 'Cancel': Don't commit this transaction; instead, redirect to the GET handler...
			doGet(request, response);
		}
	}

//	public static class Customer {
//		public String id;
//		public String name;
//		public String address;
//
//		public Customer() {
//			this(null, null, null);
//		}
//
//		public Customer(String id, String name, String address) {
//			this.id = id;
//			this.name = name;
//			this.address = address;
//		}
//
//		@Override
//		public String toString() {
//			return this.getClass().getSimpleName() + "{" + "id=" + id + " " + "name=" + name + " " + "address="
//					+ address + "}";
//		}
//	}

}
