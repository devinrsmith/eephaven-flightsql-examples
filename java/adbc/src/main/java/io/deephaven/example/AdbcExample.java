package io.deephaven.example;

import java.util.HashMap;
import java.util.Map;
import org.apache.arrow.adbc.core.AdbcConnection;
import org.apache.arrow.adbc.core.AdbcDatabase;
import org.apache.arrow.adbc.core.AdbcDriver;
import org.apache.arrow.adbc.core.AdbcStatement;
import org.apache.arrow.adbc.driver.flightsql.FlightSqlConnectionProperties;
import org.apache.arrow.adbc.driver.flightsql.FlightSqlDriverFactory;
import org.apache.arrow.flight.auth2.Auth2Constants;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdbcExample {
  private static final Logger log = LoggerFactory.getLogger(AdbcExample.class);

  public static void main(String[] args) throws Exception {
    final Map<String, Object> options = new HashMap<>();
    AdbcDriver.PARAM_URI.set(options, "grpc://localhost:10000");
    FlightSqlConnectionProperties.WITH_COOKIE_MIDDLEWARE.set(options, true);
    options.put(
        FlightSqlConnectionProperties.RPC_CALL_HEADER_PREFIX + Auth2Constants.AUTHORIZATION_HEADER,
        "Anonymous");
    options.put(
        FlightSqlConnectionProperties.RPC_CALL_HEADER_PREFIX + "x-deephaven-auth-cookie-request",
        "true");
    try (final BufferAllocator allocator = new RootAllocator();
        final AdbcDatabase database =
            new FlightSqlDriverFactory().getDriver(allocator).open(options);
        final AdbcConnection connection = database.connect()) {
      try (final AdbcStatement statement = connection.createStatement()) {
        statement.setSqlQuery("SELECT 42 as Foo");
        try (final AdbcStatement.QueryResult queryResult = statement.executeQuery()) {
          final ArrowReader reader = queryResult.getReader();
          final VectorSchemaRoot vectorRoot = reader.getVectorSchemaRoot();
          while (reader.loadNextBatch()) {
            log.info(vectorRoot.contentToTSVString());
          }
        }
      }
    }
  }
}
