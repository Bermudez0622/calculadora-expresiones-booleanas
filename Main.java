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

public class Main {

    private static Map<String, Integer> operators = new HashMap<>();
    private static Map<String, Set<String>> tokens = new HashMap<>();

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
                stack.push(item);
            } else if (isRightBrackets(item)) {
                while (!isLeftBrackets(stack.peek())) {
                    result.append(format(stack.pop()));
                }
                tokens.get("delimiters").add("()");
                stack.pop();
            } else if (isNegation(item)) {
                hasNegation = true;
                tokens.get("operators").add(item);
            } else if (isOperator(item)) {
                tokens.get("operators").add(item);
                while (!stack.isEmpty() && getPriority(item) < getPriority(stack.peek())) {
                    result.append(format(stack.pop()));
                }
                stack.push(item);
            } else {
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

    private static Boolean calculate(String input) throws IOException {
        String request = convert(input);
        Deque<Boolean> stack = new ArrayDeque<>();
        Boolean result = false;
        System.out.println(request);
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
                        throw new IOException("Operacion no soportada");
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

        System.out.println("Tokens: " + tokens);

        return stack.pop();
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        while (scan.hasNextLine()) {
            tokens.put("delimiters", new TreeSet<>());
            tokens.put("operators", new TreeSet<>());
            tokens.put("identifiers", new TreeSet<>());
            System.out.println(calculate(scan.nextLine()));
        }

        scan.close();
    }
}
