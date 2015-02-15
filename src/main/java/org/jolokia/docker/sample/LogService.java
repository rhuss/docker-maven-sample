package org.jolokia.docker.sample;

import java.io.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.flywaydb.core.Flyway;

/**
 * Popup Tomcat, migrate DB and start LogService
 *
 * @author roland
 * @since 08.08.14
 */
public class LogService extends HttpServlet {

    private final String connectionUrl;

    public LogService() throws SQLException {
        // Prepare JDBC Url from environment variable
        connectionUrl = getConnectionUrl(System.getenv("DB_PORT"));

        // Create DB schema (as defined in resources/db/migration/)
        Flyway flyway = new Flyway();
        flyway.setDataSource(connectionUrl, "postgres", null);
        flyway.migrate();
    }

    // Log into DB and print out all logs.
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try (Connection connection = DriverManager.getConnection(connectionUrl,"postgres",null)) {
            // Insert current request in DB ...
            insertLog(req, connection);

            // ... and then return all logs stored so far
            resp.setContentType("text/plain");
            printOutLogs(connection, resp.getWriter());
        } catch (SQLException e) {
            throw new ServletException("Cannot update DB: " + e,e);
        }
    }

    public static void main(String[] args) throws LifecycleException, SQLException {
        // Start embedded tomcat with a LogService servlet and wait forever
        setupAndStartTomcat(new LogService());
    }

    // Extract connection URL from environment variable as setup by Docker
    private String getConnectionUrl(String dockerEnvVar) {
        Pattern pattern = Pattern.compile("^[^/]*//(.*)");
        Matcher matcher = pattern.matcher(dockerEnvVar);
        matcher.matches();
        String hostAndPort = matcher.group(1);
        return "jdbc:postgresql://" + hostAndPort + "/postgres";
    }

    // ===================================================================================


    private static void setupAndStartTomcat(HttpServlet servlet) throws SQLException, LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        Context rootCtx = tomcat.addContext("/", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(rootCtx, "log", servlet);
        rootCtx.addServletMapping("/*", "log");
        tomcat.start();
        tomcat.getServer().await();
    }

    private void printOutLogs(Connection connection, PrintWriter out) throws SQLException {
        Statement select = connection.createStatement();
        ResultSet result = select.executeQuery("SELECT * FROM LOGGING ORDER BY DATE ASC");
        while (result.next()) {
            Timestamp date = result.getTimestamp("DATE");
            String ip = result.getString("IP");
            String url = result.getString("URL");
            out.println(date + "\t\t" + ip + "\t\t" + url);
        }
    }

    private void insertLog(HttpServletRequest req, Connection connection) throws SQLException {
        try (PreparedStatement stmt =
                     connection.prepareStatement("INSERT INTO LOGGING (date,ip,url) VALUES (?,?,?)")) {
            stmt.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
            stmt.setString(2, req.getRemoteAddr());
            stmt.setString(3, req.getRequestURI());
            stmt.executeUpdate();
        }
    }
}
