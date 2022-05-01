package com.daniel.auth;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

public class Controller {
    // db setup
    private final String framework = "embedded";
    private final String protocol = "jdbc:derby";
    private final String dbName = "accountsDB";
    private final String tableInfo = "accounts(username varchar(20), password varchar(64), salt varchar(8))";
    // db connection
    private Connection conn = null;

    // list of statements for cleanup
    private ArrayList<Statement> statements = new ArrayList<>();

    public Controller() throws SQLException {
        conn = DriverManager.getConnection(protocol + dbName + ";create=true");
    }

    private void createTable() throws SQLException {
        if(conn != null) {
            Statement s = conn.createStatement();
            statements.add(s);
            s.execute("create table " + tableInfo);
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
