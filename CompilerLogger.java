public class CompilerLogger {

    // HEADER & FOOTER
    // ==========================
    public static void showHeader() {
        System.out.println("======================================");
        System.out.println("        KopiLangs Compiler");
        System.out.println("======================================");
    }

    public static void showSuccess(String language) {
        System.out.println(" Language : " + language);
        System.out.println(" Status   : SUCCESS");
        System.out.println("--------------------------------------");
        System.out.println(" No syntax error found.");
        System.out.println("======================================");
        System.out.println();
    }

    public static void showFailed(String error) {
        System.out.println(" Status : FAILED");
        System.out.println("--------------------------------------");
        System.out.println(" Error  : " + error);
        System.out.println("======================================");
    }

    public static void showFooter() {
        System.out.println("======================================");
    }

    // EXECUTION SECTIONS
    // ==========================

    public static void showExecution() {
        System.out.println();
        System.out.println(" Running program...");
        System.out.println();
    }

    public static void showTokenTitle() {
        System.out.println();
        System.out.println("TOKENS");
        System.out.println("-------------------------");
    }

    public static void showVariableTitle() {
        System.out.println("--------------------------------------");
        System.out.println(" Final variable values:");
    }

    // COMPILER STATISTICS
    // ==========================
    public static void showStatistics(
            String program,
            int totalTokens,
            int totalVariables) {
        System.out.println();
        System.out.println("========== Statistics ================");
        System.out.println(" Program         : " + program);
        System.out.println(" Tokens Read     : " + totalTokens);
        System.out.println(" Variables       : " + totalVariables);
        System.out.println(" Status          : " + (totalTokens > 0 ? "SUCCESS" : "FAILED"));
        System.out.println("======================================");
    }

}