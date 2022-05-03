package com.daniel.auth;

import java.io.*;
import java.util.HashMap;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class App {
    private enum USERNAME_STATUS { UNAVAILABLE, INVALID, INVALID_LENGTH, INVALID_CHAR, INVALID_LANG, VALID }
    private enum PASSWORD_STATUS { INVALID, INVALID_COMMON, INVALID_LENGTH, VALID }
    public static final int USERNAME_MAX = 20;
    public static final int USERNAME_MIN = 3;
    public static final int PASSWORD_MAX = 64;
    public static final int PASSWORD_MIN = 8;
    public static final String STORAGE_FILE_NAME = "Credentials.csv";
    public static final String COMMON_PASSWORDS_LIST = "100k-most-used-passwords-NCSC-8char-plus.txt";


    /* ARGON2 CONFIG PARAMS */
    private static final int SALT_LEN = 128 / 8;
    private static final int HASH_LEN = 256 / 8;
    private static final int PARALLELISM = 4; // NOTE: BouncyCastle does not actually use multi-threading, so this will still run on one thread, and may not actually have an effect.
    private static final int MEM_KB = 256 * 1024; // 256MB
    private static final int ITERATIONS = 4;

    final String MAIN_MENU_DIALOGUE =   "\n==================\n" +
                                        "- Main Menu\n" +
                                        "- Commands:\n" +
                                        "- 'e' | 'E' : Exit application\n" +
                                        "- 'r' | 'R' : Register a new user account\n" +
                                        "- 'l' | 'L' : Login with an existing account";

    Argon2PasswordEncoder pwEncoder = new Argon2PasswordEncoder(SALT_LEN, HASH_LEN, PARALLELISM, MEM_KB, ITERATIONS);
    HashMap<String, Integer> loginAttempts = new HashMap<>();

    private void go(String[] args) {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        badWordsFilter.loadConfigs();
        Controller.init();
        boolean run = true;
        String userInput;
        while(run) {
            System.out.println(MAIN_MENU_DIALOGUE);
            try {
                // get input
                userInput = input.readLine();
                if(userInput == null) {
                    // invalid input
                    break;
                }

                char cmd = userInput.toLowerCase().charAt(0);
                switch(cmd) {
                    case 'e':
                        run = false;
                        break;
                    case 'r':
                        registerAccount(input);
                        break;
                    case 'l':
                        login(input);
                        break;
                    default:
                        invalidInput(cmd);
                        break;
                }

            } catch (IOException ioe) {
                if(ioe.getMessage().equals("Stream closed")) {
                    System.out.println("Input ended");
                }
                return;
            } catch (Exception e) {
                return;
            }
        }

        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a username is clean (contains no profanity)
     * @param username username to check
     * @return true if the username is clean, false if it contains filtered words
     */
    private boolean cleanUsername(String username) {
        return badWordsFilter.filterText(username);
    }

    /**
     * Validate a username
     * @param username username to validate
     * @return USERNAME_STATUS.VALID if valid, USERNAME_STATUS.INVALID if username contains profanity, or is too short, USERNAME_STATUS.UNAVAILABLE if already exists in db
     */
    private USERNAME_STATUS validateUsername (String username) {
        if(username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            return USERNAME_STATUS.INVALID_LENGTH;
        }

        if(!username.matches("[a-zA-Z0-9_]{3,}")) {
            return USERNAME_STATUS.INVALID_CHAR;
        }

        // check if username already exists
        if(Controller.findUsername(username)) {
            return USERNAME_STATUS.UNAVAILABLE;
        }

        if(!cleanUsername(username)) {
            return USERNAME_STATUS.INVALID_LANG;
        }

        return USERNAME_STATUS.VALID;
    }

    /**
     * Check the validity of a password
     * @param password password to validate
     * @return PASSWORD_STATUS.VALID if successfully validated, PASSWORD_STATUS.INVALID_COMMON if password is too common, PASSWORD_STATUS.INVALID_LENGTH if too short or long
     */
    private PASSWORD_STATUS validatePassword (String password) {
        if(password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            return PASSWORD_STATUS.INVALID_LENGTH;
        }

        if(findInCommon(password)) {
            return PASSWORD_STATUS.INVALID_COMMON;
        }
        return PASSWORD_STATUS.VALID;
    }

    /**
     * Checks through a list of the 100k most common passwords to see if the given password is contained
     *
     * @param password password to check against
     * @return true if the password is found in the list, false if not found
     */
    private boolean findInCommon(String password) {
        try {
            InputStream is = App.class.getResourceAsStream("/" + COMMON_PASSWORDS_LIST);
            if(is == null) {
                System.err.println("No resource found with name: " + COMMON_PASSWORDS_LIST);
                throw new FileNotFoundException("File not found: " + COMMON_PASSWORDS_LIST);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while((line = reader.readLine()) != null) {
                if(password.equals(line)) {
                    return true;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    /**
     * Prompt a user to login
     * @param reader input reader
     */
    private void login(BufferedReader reader) {
        try {
            String username = "";
            String password = "";
            boolean attemptingAuth = true;
            while(attemptingAuth) {
                System.out.println("Please enter your username. To return to the main menu, press 'enter' without entering any text");
                username = reader.readLine();

                if(username.equals("")) {
                    attemptingAuth = false;
                    continue;
                }

                System.out.println("Please enter your password:");
                password = reader.readLine();
                username = username.toLowerCase();

                if(loginAttempts.containsKey(username)) {
                    int failedAttempts = loginAttempts.get(username);
                    if (failedAttempts >= 3) {
                        System.out.println("You have failed too many login attempts and are locked out of this account");
                        return;
                    }
                }

                if(Controller.findUsername(username)) {
                    String storedPwHash = Controller.getPassword(username);
                    if(pwEncoder.matches(password, storedPwHash)) {
                        // authenticated
                        System.out.println("Successfully logged in: " + username);

                        // clear failed login attempts
                        if(loginAttempts.containsKey(username)) {
                            loginAttempts.remove(username);
                        }

                        attemptingAuth = false;
                    } else {
                        System.out.println("Incorrect username or password, please try again");
                        // increment failed login attempts
                        if(loginAttempts.containsKey(username)) {
                            int a = loginAttempts.get(username);
                            a ++;
                            loginAttempts.put(username, a);
                        } else {
                            loginAttempts.put(username, 1);
                        }
                    }
                } else {
                    // waste some time to avoid information leakage
                    pwEncoder.encode("12345678");
                    System.out.println("Incorrect username or password, please try again");
                    if(loginAttempts.containsKey(username)) {
                        int a = loginAttempts.get(username);
                        a ++;
                        loginAttempts.put(username, a);
                    } else {
                        loginAttempts.put(username, 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Empty input received");
        }
    }

    /**
     * Register an account and add to the database
     * @param reader inputstream reader
     */
    private void registerAccount(BufferedReader reader) {
        boolean invalidUsername = true;
        boolean invalidPassword = true;
        try{
            String username = "";
            String password = "";
            String passwordEnteredAgain = "";
            PASSWORD_STATUS passwordStatus = PASSWORD_STATUS.INVALID;
            USERNAME_STATUS usernameStatus = USERNAME_STATUS.INVALID;
            while(invalidUsername) {
                System.out.println("\n\nPlease enter a username (3 to 20 characters, no profanity, alphanumeric characters and _ only):");
                System.out.println("Enter nothing to exit to main menu");
                username = reader.readLine();

                if(username.equals("")) {
                    return;
                }

                username = username.toLowerCase();
                usernameStatus = validateUsername(username);
                System.out.println("\n");
                switch (usernameStatus) {
                    case UNAVAILABLE:
                        System.out.println("Username " + username + " is unavailable");
                        break;
                    case INVALID_LENGTH:
                        System.out.println("Username is too short or long, please choose a username between 3 and 20 characters");
                        break;
                    case INVALID_CHAR:
                        System.out.println("Username uses an invalid character, please choose a username containing only upper or lowercase characters, numbers 0 to 9, or _");
                        break;
                    case INVALID_LANG:
                        System.out.println("Username contains profanity, please choose a family friendly username");
                        break;
                    case VALID:
                        invalidUsername = false;
                        break;
                }
            }

            while(invalidPassword) {
                System.out.println("\n\nPlease enter a password, must be at least 8 characters long and less than 65 characters:");
                System.out.println("Enter nothing to exit to main menu");
                password = reader.readLine();
                if(password.equals("")) {
                    return;
                }
                System.out.println("Please verify your password by entering it again:");
                passwordEnteredAgain = reader.readLine();
                if(password.equals(passwordEnteredAgain)) {
                    passwordStatus = validatePassword(password);
                    switch (passwordStatus) {
                        case INVALID_COMMON:
                            System.out.println("Password is found in a list of common passwords, please choose another.");
                            break;
                        case INVALID_LENGTH:
                            System.out.println("Password is not within size constraints. Please choose a password at least 8 characters long and less than 65 characters.");
                            break;
                        case VALID:
                            invalidPassword = false;
                            break;
                    }
                } else {
                    System.out.println("Error: Passwords do not match");
                }
            }

            // sanity check
            if(passwordStatus != PASSWORD_STATUS.VALID || usernameStatus != USERNAME_STATUS.VALID) {
                System.err.println("Error: Password and username are not valid, yet we have passed verification stage!");
                return;
            }

            // generate hash of password
            String hashedPw = pwEncoder.encode(password);

            // insert into file
            Controller.insert(username, hashedPw);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Empty input received");
        }
    }

    private void invalidInput(char input) {
        System.out.println("Invalid input: '" + input + "'");
    }


    public static void main(String[] args) {
	    System.out.println("Starting...");
        new App().go(args);
        System.out.println("Exiting...");
    }
}
