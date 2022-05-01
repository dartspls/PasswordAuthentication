package com.daniel.auth;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Locale;


public class Controller {
    // db setup
    private final String framework = "embedded";
    private final String protocol = "jdbc:derby:";
    private final String dbName = "accountsDB";
    private final String tableInfo;
    // db connection
    private Connection conn = null;

    // list of statements for cleanup
    private ArrayList<Statement> statements = new ArrayList<>();

    public Controller() {
        tableInfo = "accounts(username varchar(" + App.USERNAME_MAX + "), password varchar(" + App.PASSWORD_MAX + "), salt varchar(" + App.SALT_SIZE + "))";
        try {
            conn = DriverManager.getConnection(protocol + dbName + ";create=true;");

            int tableStatus = checkTableExists();
            if(tableStatus == 0) {
                System.out.println("Table exists, sweet as");
            } else if(tableStatus == 1) {
                // TODO debug
                System.out.println("Table doesn't exist, creating...");
                createTable();
            } else if (tableStatus == 2) {
                System.out.println("Conn is null, uh oh");
            }
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }

    }
    public boolean insert(String username, String password, String salt) {
        try {
            PreparedStatement insert = conn.prepareStatement("insert into accounts values (?, ?, ?)");
            insert.setString(1, username);
            insert.setString(2, password);
            insert.setString(3, salt);
            insert.executeUpdate();
            insert.close();
            return true;
        } catch (SQLException sqle) {
            printSQLException(sqle);
        }
        return false;
    }

    public void shutdownDB() {
        try {
            DriverManager.getConnection(protocol + ";:shutdown=true");
        } catch (SQLException sqle) {
            if (( (sqle.getErrorCode() == 50000)
                    && ("XJ015".equals(sqle.getSQLState()) ))) {
                System.out.println("Shutdown derby normally");
            } else {
                System.out.println("Unexpected exception, shutdown failed");
                printSQLException(sqle);
            }
        } finally {
            // free up resources
            while(!statements.isEmpty()) {
                Statement st = (Statement)statements.remove(0);
                try {
                    if(st != null) {
                        st.close();
                        st = null;
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }

                try {
                    if (conn != null) {
                        conn.close();
                        conn = null;
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }
            }
        }
    }

    private void printSQLException(SQLException e) {
        while(e != null) {
            System.out.println("\nSQLException:");
            System.out.println(" SQL State:     " + e.getSQLState());
            System.out.println(" Error Code:    " + e.getErrorCode());
            System.out.println(" Message        " + e.getMessage());
            e = e.getNextException();
        }
    }

    private void createTable() throws SQLException {
        if(conn != null) {
            Statement s = conn.createStatement();
            statements.add(s);
            boolean result =s.execute("create table " + tableInfo);
            if(result) {
                // TODO remove debug
                System.out.println("Successfully created DB");
            } else {
                // TODO remove debug
                System.out.println("Failed to create DB");
            }
        }
    }

    /**
     * Checks if the account table exists
     * @return 0 if table exists, 1 if it does not, 2 if conn is null
     * @throws SQLException
     */
    private int checkTableExists() throws SQLException {
        if(conn != null) {
            ResultSet res = conn.getMetaData().getTables(null, null, dbName.toUpperCase(Locale.ROOT), null);
            if(res.next()) {
               return 0;
            }
            return 1;
        }
        return 2;
    }
}
