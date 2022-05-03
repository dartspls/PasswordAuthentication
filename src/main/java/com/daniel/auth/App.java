package com.daniel.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class App {
    private enum USERNAME_STATUS { UNAVAILABLE, INVALID, VALID }
    private enum PASSWORD_STATUS { INVALID_COMMON, INVALID_SHORT, VALID }
    public static final int USERNAME_MAX = 20;
    public static final int PASSWORD_MAX = 64;
    public static final String STORAGE_FILE_NAME = "Credentials.csv";


    /* ARGON2 CONFIG PARAMS */


    final String MAIN_MENU_DIALOGUE =   "\n==================\n" +
                                        "- Main Menu\n" +
                                        "- Commands:\n" +
                                        "- 'e' : Exit application\n" +
                                        "- 'h' : Display help information\n" +
                                        "- 'r' : Register a new user account\n" +
                                        "- 'l' : Login with an existing account";

    Controller ctrl; // "database"

    private void testingArgon2() {

        /*
        * This config takes 1~ second on a machine with:
        * CPU: Ryzen 9 5900X 12c24t
        * RAM: 32GB DDR4 3200 CL19
        */
        int saltLength = 128 / 8; // 128 bits for salt
        int hashLength = 256 / 8; // 256 bits for hashed pw
        int parallelism = 4; // NOTE: BouncyCastle Argon2 implementation does not take advantage of multiple threads, so even with threads=4 it only runs on one thread
        int memoryInKb = 256 * 1024; // 256mb
        int iterations = 4;

        Argon2PasswordEncoder pwEncoder = new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memoryInKb, iterations);
        long start = System.nanoTime();
        String hashed = pwEncoder.encode("somethingEasy22");
        long took = System.nanoTime() - start;

        System.out.println(hashed);
        System.out.println("Took " + (took / 1e9));

        start = System.nanoTime();
        boolean matches = pwEncoder.matches("somethingEasy22", hashed);
        took = System.nanoTime() - start;
        System.out.println("Matching...");
        System.out.println("Took " + (took / 1e9));

        if(matches) {
            System.out.println("Matches!");
        } else {
            System.out.println("Doesn't match!");
        }
    }

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

        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private USERNAME_STATUS validateUsername(String username) {
        // check if username is too short
        if(username.length() < 3) {
            return USERNAME_STATUS.INVALID;
        }

        // check if username already exists
        if(Controller.findUsername(username)) {
            return USERNAME_STATUS.UNAVAILABLE;
        }

        // TODO check for profanity
        if(false) {
            return USERNAME_STATUS.INVALID;
        }

        return USERNAME_STATUS.VALID;
    }

    private void login() {
        // TODO: Implement
        System.out.println("Not implemented");
    }

    private void registerAccount() {
        // TODO: Implement
        boolean invalidUsername = true;
        boolean invalidPassword = true;
        try{
            String username = "";
            String password= "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while(invalidUsername) {
                System.out.println("\n\nPlease enter a username (3 or more characters, no profanity):");
                username = reader.readLine();

                USERNAME_STATUS status = validateUsername(username);
                switch (status) {
                    case UNAVAILABLE:
                        System.out.println("Username " + username + " is unavailable.");
                        break;
                    case INVALID:
                        System.out.println("Username " + " is not valid.");
                        break;
                    case VALID:
                        invalidUsername = false;
                        break;
                }
            }

            while(invalidPassword) {
                System.out.println("\n\nPlease enter a password, must be at least 8 characters long:");
                password = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // TODO: Get rid of testing
        if(args.length == 0) {
            new App().go(args);
        } else {
            new App().testingArgon2();
        }
        System.out.println("Exiting...");
    }
}
