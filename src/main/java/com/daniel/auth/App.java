package com.daniel.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class App {
    public static final int USERNAME_MAX = 20;
    public static final int PASSWORD_MAX = 64;
    public static final int SALT_SIZE = 8; // 64 bit
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
    }

    private void login() {
        // TODO: Implement
        System.out.println("Not implemented");
    }

    private void registerAccount() {
        // TODO: Implement
        boolean res = false;
        System.out.println("Did insert succeed?: " + res);
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
//        new App().go(args);
        new App().testingArgon2();
        System.out.println("Exiting...");
    }
}
