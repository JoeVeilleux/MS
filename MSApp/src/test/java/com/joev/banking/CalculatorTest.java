package com.joev.banking;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CalculatorTest {
    private static final Logger logger = LogManager.getLogger(CustomerTest.class);

    @Rule
    public TestName testName = new TestName();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String NUMBER_MAX = String.valueOf(Integer.MAX_VALUE);
    private static final String NUMBER_TOO_BIG = String.valueOf(new Long(Integer.MAX_VALUE) + 1);
    private static final String NUMBER_MIN = String.valueOf(Integer.MIN_VALUE);
    private static final String NUMBER_TOO_SMALL = String.valueOf(new Long(Integer.MIN_VALUE) - 1);

    private String arg1;
    private String op;
    private String arg2;
    private String expected;
    private Class<? extends Throwable> expectedExceptionClass;

    private Calculator calculator;

    public CalculatorTest(String arg1, String op, String arg2, String expected,
            Class<? extends Throwable> expectedExceptionClass) {
        this.arg1 = arg1;
        this.op = op;
        this.arg2 = arg2;
        this.expected = expected;
        this.expectedExceptionClass = expectedExceptionClass;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> tests() {
        return Arrays.asList(new Object[][] {
                // Positive cases, all positive numeric inputs
                { "1", "+", "2", "3", null },
                { "12", "-", "2", "10", null },
                { "7", "*", "2", "14", null },
                { "8", "/", "2", "4", null },
                // Positive cases, negative numeric inputs
                { "-4", "+", "1", "-3", null },
                { "-5", "-", "-1", "-4", null },
                { "-5", "*", "7", "-35", null },
                { "-5", "*", "-7", "35", null },
                { "-12", "/", "2", "-6", null },
                // Positive cases: numbers near the edges of the Integer range
                { NUMBER_MAX, "+", "0", NUMBER_MAX, null },
                { NUMBER_MAX, "-", "1", String.valueOf(Integer.MAX_VALUE - 1), null },
                { NUMBER_MIN, "-", "0", NUMBER_MIN, null },
                { NUMBER_MIN, "+", "1", String.valueOf(Integer.MIN_VALUE + 1), null },
                // Negative cases: Illegal argument values
                { null, "+", "12", "N/A", NumberFormatException.class },
                { "abc", "+", "12", "N/A", NumberFormatException.class },
                { "1.5", "+", "12", "N/A", NumberFormatException.class },
                { "4", null, "12", "N/A", NullPointerException.class },
                { "4", "plus", "12", "N/A", IllegalArgumentException.class },
                { "4", "x", "12", "N/A", IllegalArgumentException.class },
                { "7", "+", null, "N/A", NumberFormatException.class },
                { "7", "+", "xx", "N/A", NumberFormatException.class },
                { "7", "+", "7.2", "N/A", NumberFormatException.class },
                // Negative cases: Valid numbers, but outside of valid Integer range
                { NUMBER_TOO_BIG, "+", "1", "N/A", NumberFormatException.class },
                { NUMBER_TOO_SMALL, "-", "1", "N/A", NumberFormatException.class },
                // Negative cases: Valid input numbers, resulting in values outside Integer range
                { NUMBER_MAX, "+", "1", "N/A", IllegalArgumentException.class },
                { NUMBER_MIN, "-", "1", "N/A", IllegalArgumentException.class },
                { NUMBER_MAX, "*", "2", "N/A", IllegalArgumentException.class },
                // Negative case: divide-by-zero
                { "2", "/", "0", "N/A", ArithmeticException.class },
            });
    }

    @Before
    public void before() throws IOException {
        logger.info("==========================================================================================");
        logger.info("Starting test {}.{} with: arg1={} op={} arg2={}", this.getClass().getSimpleName(),
                testName.getMethodName(), arg1, op, arg2);
        if (expectedExceptionClass == null) {
            logger.info("Expected result: {}", expected);
        } else {
            logger.info("Expected result: Exception: {}", expectedExceptionClass.getSimpleName());
        }
    }

    @Before
    public void beforeCreateCalculator() {
        calculator = new Calculator();
    }

    @Test
    public void testCalc() {
        // Arrange:
        // If this operation is expected to throw an Exception, set up so JUnit will
        // recognize that
        // as "success"
        if (expectedExceptionClass != null) {
            expectedException.expect(expectedExceptionClass);
        }

        // Act: Perform the operation
        String actual = calculator.calc(arg1, op, arg2);
        logger.atInfo().log("Actual result: {}", actual);

        // Assert:
        assertThat(actual).isEqualTo(expected);
    }
}
