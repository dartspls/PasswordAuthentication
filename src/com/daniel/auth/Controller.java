package com.daniel.auth;

import java.io.*;

public class Controller {
    // gave up on db
    public static void insert(String username, String password, String salt) {
        // check that username doesn't exist, faking "primary key"
        if(findUsername(username)) {
            // some error
            System.err.println("ERROR: Attempting to insert existing username");
        }
    }

    /**
     * Search for a username in the storage file
     *
     * @param username username to find
     * @return true if the username exists in storage
     */
    public static boolean findUsername(String username) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(App.STORAGE_FILE_NAME)));
            String line;
            String[] parts; // [0] username, [1] password, [2] salt

            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException fne) {
            System.err.println("No credential storage file found. Expected: " + App.STORAGE_FILE_NAME);
        } catch (IOException ioe) {
            System.out.println("IO Exception");
            ioe.printStackTrace();
        }

        return false;
    }

    /**
     * Find an account in the storage file
     *
     * @param username username of account
     * @param password salted password
     * @return true if an account with matching username and password + salt exists
     */
    public static boolean findAccount(String username, String password) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(App.STORAGE_FILE_NAME)));
            String line;
            String[] parts; // [0] username, [1] password, [2] salt

            while ((line = reader.readLine()) != null) {
                parts = line.split(",");
                String storedPass = parts[1] + parts[2]; // concatenate password hash and salt
                if (username.equals(parts[0]) && password.equals(storedPass)) {
                    return true;
                }
            }
        } catch (FileNotFoundException fne) {
            System.err.println("No credential storage file found. Expected: " + App.STORAGE_FILE_NAME);
        } catch (IOException ioe) {
            System.out.println("IO Exception");
            ioe.printStackTrace();
        }

        return false;
    }
}
