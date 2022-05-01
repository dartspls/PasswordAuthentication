package com.daniel.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
    final String MAIN_MENU_DIALOGUE =   "\n==================\n" +
                                        "- Main Menu\n" +
                                        "- Commands:\n" +
                                        "- 'e' : Exit application\n" +
                                        "- 'h' : Display help information\n" +
                                        "- 'r' : Register a new user account\n" +
                                        "- 'l' : Login with an existing account";

    private void go(String[] args) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        // temp
        boolean loggedIn = false;
        boolean run = true;

        String userInput;
        while(run) {
            // TODO: placeholder auth stuff
            if(loggedIn) {
                System.out.println("Welcome admin");
            } else {
                System.out.println(MAIN_MENU_DIALOGUE);
            }

            try {
                // get input
                userInput = input.readLine();
                if(userInput.length() < 1) {
                    // got nothing
                    continue;
                }

                char cmd = userInput.charAt(0);
                switch(cmd) {
                    case 'e':
                        run = false;
                        break;
                    case 'h':
                        help();
                        break;
                    case 'r':
                        registerAccount();
                        break;
                    case 'l':
                        login();
                        break;
                    default:
                        invalidInput();
                        break;
                }

            } catch (Exception e) {
                // TODO: Proper error messages
                e.printStackTrace();
            }

        }
    }

    private void login() {
        // TODO: Implement
        System.out.println("Not implemented");
    }

    private void registerAccount() {
        // TODO: Implement
        System.out.println("Not implemented");
    }

    private void invalidInput() {
        // TODO: Implement
        System.out.println("Not implemented");
    }

    private void help() {
        // TODO: Implement
        System.out.println("Not implemented");
    }


    public static void main(String[] args) {
	    System.out.println("Starting...");
        new App().go(args);
        System.out.println("Exiting...");
    }
}
