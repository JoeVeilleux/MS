package com.joev.banking;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/calc")
public class CalculatorServlet {
    private static final Logger logger = LogManager.getLogger(CalculatorServlet.class);

    @GET
    @Path("/{args}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response doGet(@PathParam("args") String args) {
        Pattern argsP = Pattern.compile("([\\-\\+]?\\d+)([\\+\\-\\*\\/])([\\-\\+]?\\d+)");
        Matcher m = argsP.matcher(args);
        String result;
        if (m.matches()) {
            String arg1 = m.group(1);
            String op = m.group(2);
            String arg2 = m.group(3);
            Calculator calculator = new Calculator();
            result = calculator.calc(arg1, op, arg2);
            logger.atInfo().log("doGet(): args={} arg1={} op={} arg2={} result={}", args, arg1, op, arg2, result);
            return Response.ok().type(MediaType.TEXT_PLAIN).entity(result).build();
        } else {
            String errMsg = String.format(
                "ERROR: Illegal argument '%s'. Specify: <numbers><op><numbers> Valid 'op' values: + - * /", args);
            logger.atError().log("doGet(): {}", errMsg);
            return Response.ok().status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(errMsg).build();
        }
    }

}
