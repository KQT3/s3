package com.example.s3.util;

public interface ANSIColors {
    String ANSI_RESET = "\u001B[0m";
    String ANSI_BLACK = "\u001B[30m";
    String ANSI_RED = "\u001B[31m";
    String ANSI_GREEN = "\u001B[32m";
    String ANSI_YELLOW = "\u001B[33m";
    String ANSI_BLUE = "\u001B[34m";
    String ANSI_PURPLE = "\u001B[35m";
    String ANSI_CYAN = "\u001B[36m";
    String ANSI_WHITE = "\u001B[37m";

    static void printBlue(Object text){System.out.println(ANSI_BLUE + text + ANSI_RESET );}
    static void printPurple(Object text){System.out.println(ANSI_PURPLE + text + ANSI_RESET );}
    static void printGreen(Object text){System.out.println(ANSI_GREEN + text + ANSI_RESET );}
}
