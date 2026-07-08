import kopilang.parser.*;
import kopilang.lexer.*;
import kopilang.node.*;

import java.io.*;

public class KopiLangs {

    public static void main(String[] args) {

        String languageName;

        if (args.length > 0) {
            languageName = LanguageManager.getProgram(args[0]);
        } else {
            languageName = LanguageManager.getProgram("DoWhileTest");
        }

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
            CompilerLogger.showHeader();
            CompilerLogger.showSuccess(languageName);

            // ----------------------------
            // Execute program
            // ----------------------------
            CompilerLogger.showExecution();

            Interpreter interpreter = new Interpreter(true);
            interpreter.run(tree);

            CompilerLogger.showTokenTitle();

            Lexer tokenLexer = new Lexer(
                    new PushbackReader(new FileReader(file), 1024));
            int totalTokens = 0;
            Token token;

            while (!((token = tokenLexer.next()) instanceof EOF)) {
                totalTokens++;
                System.out.printf("%-15s : %s%n",
                        token.getClass().getSimpleName(),
                        token.getText());
            }

            // Final variable values
            CompilerLogger.showVariableTitle();
            System.out.print(interpreter.finalStateReport());

            System.out.println("======================================");
            CompilerLogger.showVariableTitle();

            System.out.print(interpreter.finalStateReport());

            CompilerLogger.showStatistics(
                languageName,
                totalTokens,
                interpreter.getVariableCount());

            CompilerLogger.showFooter();    
        } catch (Exception e) {

            CompilerLogger.showHeader();
            CompilerLogger.showFailed(e.getMessage());
        }
    }
    
}