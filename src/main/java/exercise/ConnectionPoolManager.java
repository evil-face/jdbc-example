package exercise;
import org.apache.commons.dbcp2.BasicDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolManager {
    private static final String JDBC_URL =  System.getenv("JDBC_URL");
    private static final String JDBC_USER = System.getenv("JDBC_USER");
    private static final String JDBC_PASSWORD = System.getenv("JDBC_PASSWORD");
    private static final DataSource dataSource;
    private ConnectionPoolManager() {
        throw new IllegalStateException("Utility class");
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        dataSource = setupDataSource();
    }

    private static DataSource setupDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(JDBC_URL);
        ds.setUsername(JDBC_USER);
        ds.setPassword(JDBC_PASSWORD);

        ds.setMaxTotal(10);
        ds.setValidationQuery("SELECT 1");

        return ds;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
