import kopilang.parser.*;
import kopilang.lexer.*;
import kopilang.node.*;

import java.io.*;

public class KopiLangs {

    public static void main(String[] args) {

        String languageName = (args.length > 0)
                ? args[0]
                : "Korean.kopilang";

        try {
            // File check exist or not
            File file = new File(languageName);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + languageName);
            }

            // Lexer + Parser
            Lexer lexer = new Lexer(
                    new PushbackReader(
                            new FileReader(file), 1024));

            Parser parser = new Parser(lexer);

            Start tree = parser.parse();

            // Compilation success message
            System.out.println("======================================");
            System.out.println("        KopiLangs Compiler");
            System.out.println("======================================");
            System.out.println(" Language : " + languageName);
            System.out.println(" Status    : SUCCESS");
            System.out.println("--------------------------------------");
            System.out.println(" No syntax error found.");
            System.out.println("======================================");

            // ----------------------------
            // Execute program
            // ----------------------------
            System.out.println();
            System.out.println(" Running program...");

            Interpreter interpreter = new Interpreter(true);
            interpreter.run(tree);

            System.out.println();
            System.out.println("TOKENS");
            System.out.println("-------------------------");

            Lexer tokenLexer = new Lexer(
                    new PushbackReader(new FileReader(file), 1024));

            Token token;

            while (!((token = tokenLexer.next()) instanceof EOF)) {

                System.out.printf("%-15s : %s%n",
                        token.getClass().getSimpleName(),
                        token.getText());
            }

            System.out.println("--------------------------------------");
            System.out.println(" Final variable values:");
            System.out.print(interpreter.finalStateReport());

            System.out.println("======================================");

        } catch (Exception e) {

            System.out.println("======================================");
            System.out.println("        KopiLangs Compiler");
            System.out.println("======================================");
            System.out.println(" Status : FAILED");
            System.out.println("--------------------------------------");
            System.out.println(" Error  : " + e.getMessage());
            System.out.println("======================================");
        }
    }
}