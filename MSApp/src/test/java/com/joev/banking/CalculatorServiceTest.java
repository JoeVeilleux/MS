package com.joev.banking;

import static com.google.common.truth.Truth.assertWithMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.joev.util.SHClient;
import com.joev.util.SHClient.SHResp;

public class CalculatorServiceTest {
    private static final Logger logger = LogManager.getLogger(CalculatorServiceTest.class);
    
    @Rule public TestName testName = new TestName();

    private static final int TEST_PORT = 9000;
    private static final String URL_BASE = "http://localhost:" + TEST_PORT;
    Server server;
        
    @Before
    public void before() throws Exception {
        logger.info("================================================================================");
        logger.info("Starting test {}.{}", this.getClass().getSimpleName(), testName.getMethodName());
        // Start the service, using an alternate port for testing
        logger.info("before(): Starting server");
        server = CustomerService.createServer(TEST_PORT);
        server.start();
        logger.info("before(): Server is started");
    }
    
    @After
    public void after() throws Exception {
        logger.info("after(): Stopping server");
        server.stop();
        logger.info("after(): Server is stopped");
    }
    
    // Positive testcases
    @Test public void testCalc_PosPlusPos() throws Exception { doTestCalculator("12", "+", "4", Status.OK, "16"); }
    @Test public void testCalc_PosMinusPos() throws Exception { doTestCalculator("12", "-", "4", Status.OK, "8"); }
    @Test public void testCalc_PosTimesPos() throws Exception { doTestCalculator("12", "*", "4", Status.OK, "48"); }
    @Test public void testCalc_PosDivPos() throws Exception { doTestCalculator("12", "/", "4", Status.OK, "3"); }
    @Test public void testCalc_NegPlusPos() throws Exception { doTestCalculator("-12", "+", "4", Status.OK, "-8"); }
    @Test public void testCalc_NegMinusPos() throws Exception { doTestCalculator("-12", "-", "4", Status.OK, "-16"); }
    @Test public void testCalc_NegTimesPos() throws Exception { doTestCalculator("-12", "*", "4", Status.OK, "-48"); }
    @Test public void testCalc_NegDivPos() throws Exception { doTestCalculator("-12", "/", "4", Status.OK, "-3"); }

    // Negative testcases
    @Test public void testCalc_BadNumArg1() throws Exception { doTestCalculator("abc", "+", "0", Status.BAD_REQUEST, null); }
    @Test public void testCalc_BadOpArg() throws Exception { doTestCalculator("7", "times", "0", Status.BAD_REQUEST, null); }
    @Test public void testCalc_BadNumArg2() throws Exception { doTestCalculator("14", "+", "12.7", Status.BAD_REQUEST, null); }
    @Test public void testCalc_DivByZero() throws Exception { doTestCalculator("12", "/", "0", Status.INTERNAL_SERVER_ERROR, null); }

    /** Test the calculator service */
    public void doTestCalculator(String arg1, String op, String arg2, Status expectedStatus, String expected) throws Exception {
        logger.info("Test values: arg1={} op={} arg2={} expectedStatus={} expected={}", arg1, op, arg2, expectedStatus, expected);
        String args = urlEncode(arg1 + op + arg2);

        SHResp response = SHClient.doHttp(URL_BASE + "/calc/" + args, SHClient.RM.GET, null, SHClient.RP.ACC_TEXT);
        SHClient.logResponse(response);

        assertWithMessage("Response code").that(response.responseCode).isEqualTo(expectedStatus.getStatusCode());
        // Work around a bug (**NOT IN OUR CODE**) where the "reason phrase" for INTERNAL_SERVER_ERROR is supposed
        // to be "Internal Server Error" but in fact it's just "Server Error". Somebody's not complying with the
        // specs, but it's outside our code.
        if (expectedStatus.equals(Status.INTERNAL_SERVER_ERROR)) {
            assertWithMessage("Response message").that(response.responseMessage).isEqualTo("Server Error");
        } else {
            assertWithMessage("Response message").that(response.responseMessage).isEqualTo(expectedStatus.getReasonPhrase());
        }
        if (expected != null) {
            assertWithMessage("Response body").that(response.responseBody).isEqualTo(expected);
        }
    }
    
    public static String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
