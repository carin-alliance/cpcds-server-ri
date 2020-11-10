package ca.uhn.fhir.jpa.starter.authorization;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

/**
 * The Database is responsible for storing and retrieving authorized user login.
 * 
 * @see https://github.com/HL7-DaVinci/prior-auth/blob/dev/src/main/java/org/hl7/davinci/priorauth/Database.java
 */
public class Database {

    private String SQL_FILE;
    private static final String CREATE_SQL_FILE = "src/main/java/ca/uhn/fhir/jpa/starter/authorization/CreateDatabase.sql";

    private static final String styleFile = "src/main/resources/style.html";
    private static final String scriptFile = "src/main/resources/script.html";

    private static String style = "";
    private static String script = "";

    private static final String SET_CONCAT = ", ";
    private static final String WHERE_CONCAT = " AND ";

    // DB_CLOSE_DELAY=-1 maintains the DB in memory after all connections closed
    // (so that we don't lose everything between a connection closing and the next
    // being opened)
    private static final String JDBC_TYPE = "jdbc:h2:";
    private static final String JDBC_FILE = "database";
    private static final String JDBC_OPTIONS = ";DB_CLOSE_DELAY=-1";
    private String JDBC_STRING;

    public enum Table {
        USERS("Users"), CLIENTS("Clients");

        private final String value;

        Table(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_STRING);
        connection.setAutoCommit(true);
        return connection;
    }

    public Database() {
        this("./");
    }

    public Database(String relativePath) {
        JDBC_STRING = JDBC_TYPE + relativePath + JDBC_FILE + JDBC_OPTIONS;
        System.out.println("JDBC: " + JDBC_STRING);
        SQL_FILE = relativePath + CREATE_SQL_FILE;
        try (Connection connection = getConnection()) {
            String sql = new String(Files.readAllBytes(Paths.get(SQL_FILE).toAbsolutePath()));
            connection.prepareStatement(sql.replace("\"", "")).execute();
            System.out.println(sql);

            style = new String(Files.readAllBytes(Paths.get(relativePath + styleFile).toAbsolutePath()));
            script = new String(Files.readAllBytes(Paths.get(relativePath + scriptFile).toAbsolutePath()));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String generateAndRunQuery(Table table) {
        String sql = "SELECT * FROM " + table.value() + " ORDER BY TIMESTAMP DESC";
        return runQuery(sql, true, true);
    }

    public String runQuery(String sqlQuery, boolean printClobs, boolean outputHtml) {
        String ret = "";
        try (Connection connection = getConnection()) {
            // build and execute the query
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            ResultSet rs = stmt.executeQuery();

            // get the number of columns
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            if (outputHtml) {
                ret += "<table id='results'>\n<tr>";
            }
            // print the column names
            for (int i = 1; i <= columnCount; i++) {
                if (i != 1 && !outputHtml) {
                    ret += " / ";
                }
                if (outputHtml) {
                    String columnName = metaData.getColumnName(i);
                    if (columnName.contains("ID")) {
                        ret += "<th><div style='width: 300px;'>" + metaData.getColumnName(i) + "</div></th>";
                    } else {
                        ret += "<th>" + metaData.getColumnName(i) + "</th>";
                    }
                } else {
                    ret += metaData.getColumnName(i);
                }
            }
            if (outputHtml) {
                ret += "</tr>";
            }
            ret += "\n";

            // print all of the data
            while (rs.next()) {
                if (outputHtml) {
                    ret += "<tr>";
                }
                for (int i = 1; i <= columnCount; i++) {
                    if (outputHtml) {
                        ret += "<td>";
                    }
                    if (i != 1 && !outputHtml) {
                        ret += " / ";
                    }
                    Object object = rs.getObject(i);
                    if (object instanceof org.h2.jdbc.JdbcClob && printClobs) {
                        ret += "<button class=\"collapsible\">+</button>\n" + "<div class=\"content\"><xmp>";
                        ret += object == null ? "NULL" : rs.getString(i);
                        ret += "</xmp>\n</div>\n";
                    } else {
                        ret += object == null ? "NULL" : object.toString();
                    }
                    if (outputHtml) {
                        ret += "</td>\n";
                    }
                }
                if (outputHtml) {
                    ret += "</tr>";
                }
                ret += "\n";
            }

            if (outputHtml) {
                ret += "</table>\n";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (outputHtml) {
            ret = "<html><head>" + style + "</head><body>" + ret + script + "</body></html>";
        }

        return ret;
    }

    /**
     * Read a specific row from the database.
     * 
     * @param constraintParams - the search constraints for the SQL query.
     * @return User
     */
    private User readUser(Map<String, Object> constraintParams) {
        System.out.println("Database::read(Users, " + constraintParams.toString() + ")");
        User result = null;
        if (constraintParams != null) {
            try (Connection connection = getConnection()) {
                String sql = "SELECT TOP 1 patient_id, username, password, timestamp, refresh_token FROM Users WHERE "
                        + generateClause(constraintParams, WHERE_CONCAT) + " ORDER BY timestamp DESC;";
                PreparedStatement stmt = generateStatement(sql, Collections.singletonList(constraintParams),
                        connection);
                System.out.println("read query: " + stmt.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String id = rs.getString("patient_id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String createdDate = rs.getString("timestamp");
                    String refreshToken = rs.getString("refresh_token");
                    System.out.println("read: " + id + "/" + username);
                    result = new User(username, password, id, createdDate, refreshToken);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public User readUser(String username) {
        return this.readUser(Collections.singletonMap("username", username));
    }

    public String readRefreshToken(String patientId) {
        User user = this.readUser(Collections.singletonMap("patient_id", patientId));
        return user.getRefreshToken();
    }

    /**
     * Read a sepcific row from the Clients table
     * 
     * @param clientId - the client ID to search for
     * @return Client
     */
    public Client readClient(String clientId) {
        System.out.println("Database::read(Users " + clientId + ")");
        Client result = null;
        if (clientId != null) {
            try (Connection connection = getConnection()) {
                String sql = "SELECT TOP 1 id, secret, redirect, timestamp FROM Clients WHERE id = ? ORDER BY timestamp DESC;";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, clientId);
                System.out.println("read query: " + stmt.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String id = rs.getString("id");
                    String secret = rs.getString("secret");
                    String redirectUri = rs.getString("redirect");
                    String createdDate = rs.getString("timestamp");
                    System.out.println("read: " + id + "/" + secret);
                    result = new Client(id, secret, redirectUri, createdDate);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Insert a row into database.
     * 
     * @param table - the Table to write the data to
     * @param map   - key value pair of values to insert
     * @return boolean - whether or not the data was written.
     */
    private boolean write(Table table, Map<String, Object> map) {
        boolean result = false;
        if (table != null && map != null) {
            try (Connection connection = getConnection()) {
                String valueClause = "";
                for (int i = 0; i < map.values().size() - 1; i++)
                    valueClause += "?,";
                valueClause += "?";

                String sql = "INSERT INTO " + table.value() + " (" + setColumns(map.keySet()) + ") VALUES ("
                        + valueClause + ");";
                PreparedStatement stmt = generateStatement(sql, Collections.singletonList(map), connection);
                result = stmt.execute();
                System.out.println(stmt.toString());
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Insert a user into database.
     * 
     * @param user - the new user to insert into the database.
     * @return boolean - whether or not the user was written.
     */
    public boolean write(User user) {
        System.out.println("Database::write Users(" + user.toString() + ")");
        return write(Table.USERS, user.toMap());
    }

    /**
     * Insert a client into database.
     * 
     * @param client - the new client to insert into the database.
     * @return boolean - whether or not the client was written.
     */
    public boolean write(Client client) {
        System.out.println("Database::write Clients(" + client.hashCode() + ")");
        return write(Table.CLIENTS, client.toMap());
    }

    /**
     * Update a single column in a row to a new value
     * 
     * @param constraintParams - map of column to value for the SQL WHERE clause
     * @param data             - map of column to value for the SQL SET clause
     * @return boolean - whether or not the update was successful
     */
    private boolean update(Map<String, Object> constraintParams, Map<String, Object> data) {
        System.out.println("Database::update(Users WHERE " + constraintParams.toString() + ", SET" + data.toString() + ")");
        boolean result = false;
        if (constraintParams != null && data != null) {
            try (Connection connection = getConnection()) {
                String sql = "UPDATE Users SET " + generateClause(data, SET_CONCAT)
                        + ", timestamp = CURRENT_TIMESTAMP WHERE " + generateClause(constraintParams, WHERE_CONCAT)
                        + ";";
                Collection<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
                maps.add(data);
                maps.add(constraintParams);
                PreparedStatement stmt = generateStatement(sql, maps, connection);
                stmt.execute();
                result = stmt.getUpdateCount() > 0 ? true : false;
                System.out.println(stmt.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean setRefreshTokenId(String patientId, String jwtId) {
        return this.update(Collections.singletonMap("patient_id", patientId),
                Collections.singletonMap("refresh_token", jwtId));
    }

    /**
     * Create a SQL PreparedStatement from an SQL string and setting the strings
     * based on the maps provided.
     * 
     * @param sql        - query string with '?' denoting values to be set by the
     *                   maps.
     * @param maps       - Collection of Maps used to set the values.
     * @param connection - the connection to the database.
     * @return PreparedStatement with all values set or null if the number of values
     *         provided is incorrect.
     * @throws SQLException
     */
    private PreparedStatement generateStatement(String sql, Collection<Map<String, Object>> maps, Connection connection)
            throws SQLException {
        int numValuesNeeded = (int) sql.chars().filter(ch -> ch == '?').count();
        int numValues = maps.stream().reduce(0, (subtotal, element) -> subtotal + element.size(), Integer::sum);
        if (numValues != numValuesNeeded) {
            System.out.println("Database::generateStatement:Value mismatch. Need " + numValuesNeeded
                    + " values but received " + numValues);
            return null;
        }

        PreparedStatement stmt = connection.prepareStatement(sql);
        int valueIndex = 1;
        for (Map<String, Object> map : maps) {
            for (Object value : map.values()) {
                String valueStr;
                if (value instanceof String)
                    valueStr = (String) value;
                else if (value == null)
                    valueStr = "null";
                else
                    valueStr = value.toString();
                stmt.setString(valueIndex, valueStr);
                valueIndex++;
            }
        }

        return stmt;
    }

    /**
     * Reduce a Map to a single string in the form "{key} = '{value}'" +
     * concatonator
     * 
     * @param map          - key value pair of columns and values.
     * @param concatonator - the string to connect a set of key value with another
     *                     set.
     * @return string in the form "{key} = '{value}'" + concatonator...
     */
    private String generateClause(Map<String, Object> map, String concatonator) {
        String column;
        String sqlStr = "";
        for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
            column = iterator.next();
            sqlStr += column + " = ?";

            if (iterator.hasNext())
                sqlStr += concatonator;
        }

        return sqlStr;
    }

    /**
     * Internal function to map the keys to a string
     * 
     * @param keys - the set of keys to be reduced.
     * @return a string of each key concatenated by ", "
     */
    private String setColumns(Set<String> keys) {
        Optional<String> reducedArr = Arrays.stream(keys.toArray(new String[0]))
                .reduce((str1, str2) -> str1 + ", " + str2);
        return reducedArr.get();
    }

}
