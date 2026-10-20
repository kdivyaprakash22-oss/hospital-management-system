package com.hospital;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}
    public static Connection get() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String pass = System.getenv("DB_PASSWORD");
        if (url == null || url.isBlank()) throw new SQLException("DB_URL environment variable is not configured.");
        if (url.startsWith("postgresql://")) url = "jdbc:" + url;
        if (!url.startsWith("jdbc:postgresql://")) throw new SQLException("DB_URL must be a PostgreSQL JDBC URL.");
        return DriverManager.getConnection(url, user == null ? "" : user, pass == null ? "" : pass);
    }
}
