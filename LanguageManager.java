public class LanguageManager {

    public static String getProgram(String language) {

        if (language == null || language.isEmpty()) {
            return "DoWhileTest.kopilang";
        }

        switch (language.toLowerCase()) {

            case "malay":
                return "Malay.kopilang";

            case "spanish":
                return "Spanish.kopilang";

            case "tagalog":
                return "Tagalog.kopilang";

            case "korean":
                return "Korean.kopilang";

            case "dowhile":
            case "dowhiletest":
                return "DoWhileTest.kopilang";

            default:
                throw new IllegalArgumentException(
                    "Unknown program: " + language);
        }
    }
}