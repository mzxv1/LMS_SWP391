package com.lms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized JDBC connection provider.
 * Reads connection details from /db.properties (src/main/resources/db.properties).
 *
 * NOTE: For a learning/demo project we use plain DriverManager connections
 * (one connection per request). For production use, swap this for a
 * connection pool (HikariCP, Tomcat JDBC, etc.) without changing the DAO layer.
 */
public class DBConnection {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream is = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialize DB configuration", e);
        }
        URL = props.getProperty("db.url");
        USERNAME = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
