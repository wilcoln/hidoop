package utils;

public class Log {

    //info
    public static void i(String className, String message) {
        System.out.println(ConsoleColors.WHITE + className + " : " + message + ConsoleColors.RESET);
    }

    //success

    public static void s(String className, String message) {
        System.out.println(ConsoleColors.GREEN_BOLD + className + " : " + message + ConsoleColors.RESET);
    }

    //error
    public static void e(String className, String message) {
        System.out.println(ConsoleColors.RED + className + " : " + message + ConsoleColors.RESET);
    }

    //debug
    public static void d(String className, String message) {
        System.out.println(ConsoleColors.BLUE + className + " : " + message + ConsoleColors.RESET);
    }

    //warning
    public static void w(String className, String message) {
        System.out.println(ConsoleColors.YELLOW + className + " : " + message + ConsoleColors.RESET);
    }

}