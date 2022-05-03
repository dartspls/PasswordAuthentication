package com.daniel.auth;

import java.io.*;

public class Controller {

    public static void init() {
        try{
            File credFile = new File(App.STORAGE_FILE_NAME);
            if(!credFile.exists()) {
                credFile.createNewFile();
            }
        } catch (IOException ioe) {
            System.err.println("Error creating credential file");
        }
    }

    /**
     * insert an account into the database
     * @param username username of account
     * @param passwordHash hashed password in argon2 format
     */
    public static void insert(String username, String passwordHash) {
        // check that username doesn't exist, faking "primary key"
        if(findUsername(username)) {
            // some error
            System.err.println("ERROR: Attempting to insert existing username");
            return;
        }
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(App.STORAGE_FILE_NAME, true));
            writer.write(username + " " + passwordHash);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println("IOException in insert");
            e.printStackTrace();
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
            String[] parts; // [0] username, [1] password

            while ((line = reader.readLine()) != null) {
                parts = line.split(" ");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException fne) {
            System.err.println("No credential storage file found. Expected: " + App.STORAGE_FILE_NAME);
        } catch (IOException ioe) {
            System.err.println("IO Exception");
            ioe.printStackTrace();
        }

        return false;
    }

    /**
     * Find an account in the storage file and return the password (hash)
     *
     * @param username username of account
     * @return true if an account with matching username and password + salt exists
     */
    public static String getPassword(String username) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(App.STORAGE_FILE_NAME)));
            String line;
            String[] parts; // [0] username, [1] password

            while ((line = reader.readLine()) != null) {
                parts = line.split(" ");
                if (username.equals(parts[0])) {
                    return parts[1];
                }
            }
        } catch (FileNotFoundException fne) {
            System.err.println("No credential storage file found. Expected: " + App.STORAGE_FILE_NAME);
        } catch (IOException ioe) {
            System.out.println("IO Exception");
            ioe.printStackTrace();
        }
        return null;
    }
}
