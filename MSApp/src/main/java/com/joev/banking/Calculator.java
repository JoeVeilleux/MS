package com.joev.banking;

public class Calculator {

    public String calc(String arg1, String op, String arg2) {
        long a1 = Integer.parseInt(arg1);
        long a2 = Integer.parseInt(arg2);
        long result;
        switch (op) {
        case "+":
            result = a1 + a2;
            break;
        case "-":
            result = a1 - a2;
            break;
        case "*":
            result = a1 * a2;
            break;
        case "/":
            result = a1 / a2;
            break;
        default:
            throw new IllegalArgumentException("Operator '" + op + "' not supported");
        }
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Operation results in value outside of Integer range");
        }
        return String.valueOf(result);
    }

    /** Simple demonstration of usage */
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        String arg1 = "12";
        String op = "+";
        String arg2 = "4";
        String expected = "16";
        String actual = calculator.calc(arg1, op, arg2);
        System.out.println(String.format("Calculator.calc(%s, %s, %s): expected=%s actual=%s",
            arg1, op, arg2, expected, actual));
    }
}
