import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map;
import java.util.Objects;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

public class Compiler {

    private static Map<String, Integer> operators = new HashMap<>();
    private static Map<String, Map<String, Integer>> tokens = new HashMap<>();

    private static int delimitersCont = 0;
    private static int operatorsCont = 0;
    private static int identifiersCont = 0;

    static {
        operators.put("(", 0);
        operators.put("^", 1);
        operators.put("|", 2);
        operators.put("&", 3);
        operators.put("~", 4);
        operators.put(")", 5);
    }

    private static boolean isOperator(String c) {
        return operators.containsKey(c);
    }

    private static int getPriority(String c) {
        return operators.get(c);
    }

    private static boolean isLeftBrackets(String c) {
        return Objects.equals("(", c);
    }

    private static boolean isRightBrackets(String c) {
        return Objects.equals(")", c);
    }

    private static boolean isNegation(String c) {
        return Objects.equals("~", c) || c.contains("~");
    }

    private static String format(String input) {
        return String.format("%s ", input);
    }

    private static List<String> parse(String input) {
        List<String> items = new ArrayList<>();
        StringBuilder identifier = new StringBuilder();
        input = input.replaceAll(" ", "")
                .replaceAll("\\{|\\[", "(")
                .replaceAll("\\}|\\]", ")")
                .replaceAll("\\+", "\\|")
                .replaceAll("\\*\\*", "\\^")
                .replaceAll("\\*", "\\&")
                .replaceAll("\\-", "\\~");

        System.out.println("PARSE IN: " + input);

        items = List.of(input.split(""));

        System.out.println("PARSE OUT: " + items);

        return items;
    }

    private static String convert(String input) {
        Deque<String> stack = new ArrayDeque<>();
        StringBuilder result = new StringBuilder();
        List<String> request = parse(input);
        boolean hasNegation = false;
        for (String item : request) {
            if (isLeftBrackets(item)) {
                tokens.get("delimiters").put(item);
                stack.push(item);
            } else if (isRightBrackets(item)) {
                while (!isLeftBrackets(stack.peek())) {
                    result.append(format(stack.pop()));
                }
                if(!tokens.get("delimiters").contains(item)){
                    delimitersCont++;
                }
                tokens.get("delimiters").add(item);
                stack.pop();
                
            } else if (isNegation(item)) {
                hasNegation = true;
                if(!tokens.get("operators").contains(item)){
                    operatorsCont++;
                }
                tokens.get("operators").add(item);
                
            } else if (isOperator(item)) {
                if(!tokens.get("operators").contains(item)){
                    operatorsCont++;
                }
                tokens.get("operators").add(item);
                while (!stack.isEmpty() && getPriority(item) < getPriority(stack.peek())) {
                    result.append(format(stack.pop()));
                }
                stack.push(item);
            } else {
                if(!tokens.get("identifiers").contains(item)){
                    identifiersCont++;
                }
                tokens.get("identifiers").add(item);
                if(hasNegation) {
                    result.append(format("~" + item));
                    hasNegation = false;
                } else {
                    result.append(format(item));
                }
            }
        }

        while (!stack.isEmpty()) {
            result.append(format(stack.pop()));
        }

        System.out.println("CONVERT OUT: " + result.toString());

        return result.toString();
    }

    private static Boolean calculate(String input) {
        String request = convert(input);
        Deque<Boolean> stack = new ArrayDeque<>();
        Boolean result = false;
        for(String item : request.split(" ")) {
            if(item.isEmpty()) {
                continue;
            }
            if(isOperator(item)) {
                Boolean b = stack.pop();
                Boolean a = stack.pop();
                switch (item) {
                    case "|":
                        result = a || b;
                        break;
                    case "&":
                        result = a && b;
                        break;
                    case "^":
                        result = a ^ b;
                        break;
                    default:
                        System.err.println("Operacion no soportada");
                }
                stack.push(result);
            } else {
                if(isNegation(item)) {
                    stack.push(false);
                } else {
                    stack.push(true);
                }
            }
        }
//(p&q)|(q&r)^t
        System.out.println("Tokens: " + tokens);

        return stack.pop();
    }

    public Compiler(String input) {
        operatorsCont = 0;
        identifiersCont = 0;
        delimitersCont = 0;
        tokens.put("delimiters", new HashMap<>());
        tokens.put("operators", new HashMap<>());
        tokens.put("identifiers", new HashMap<>());
        System.out.println(calculate(input));
        System.out.println(String.format("Operators count: %s", operatorsCont));
        System.out.println(String.format("Identifiers count: %s", identifiersCont));
        System.out.println(String.format("Delimiters count: %s", delimitersCont));
    }
}
