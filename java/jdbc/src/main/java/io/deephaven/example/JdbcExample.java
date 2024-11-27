package io.deephaven.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExample {
  private static final Logger log = LoggerFactory.getLogger(JdbcExample.class);

  public static void main(String[] args) throws Exception {
    final String url =
        "jdbc:arrow-flight-sql://localhost:10000?useEncryption=0&Authorization=Anonymous&x-deephaven-auth-cookie-request=true";
    try (final Connection connection = DriverManager.getConnection(url)) {
      try (final Statement statement = connection.createStatement();
          final ResultSet resultSet = statement.executeQuery("SELECT 42 As Foo")) {
        while (resultSet.next()) {
          log.info("{}", resultSet.getLong("Foo"));
        }
      }
    }
  }
}
