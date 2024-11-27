package io.deephaven.example;

import org.apache.arrow.flight.CallHeaders;
import org.apache.arrow.flight.FlightCallHeaders;
import org.apache.arrow.flight.FlightClient;
import org.apache.arrow.flight.FlightInfo;
import org.apache.arrow.flight.FlightStream;
import org.apache.arrow.flight.HeaderCallOption;
import org.apache.arrow.flight.Location;
import org.apache.arrow.flight.auth2.Auth2Constants;
import org.apache.arrow.flight.auth2.ClientBearerHeaderHandler;
import org.apache.arrow.flight.auth2.ClientIncomingAuthHeaderMiddleware;
import org.apache.arrow.flight.grpc.CredentialCallOption;
import org.apache.arrow.flight.sql.FlightSqlClient;
import org.apache.arrow.memory.RootAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthExample {
  private static final Logger log = LoggerFactory.getLogger(AuthExample.class);

  public static void main(String[] args) throws Exception {
    final FlightClient client;
    final CredentialCallOption credentials;
    {
      final ClientIncomingAuthHeaderMiddleware.Factory authFactory =
          new ClientIncomingAuthHeaderMiddleware.Factory(new ClientBearerHeaderHandler());
      client =
          FlightClient.builder()
              .allocator(new RootAllocator())
              .location(Location.forGrpcInsecure("localhost", 10000))
              .intercept(authFactory)
              .build();
      {
        final CallHeaders headers = new FlightCallHeaders();
        headers.insert(Auth2Constants.AUTHORIZATION_HEADER, "Anonymous");
        client.handshake(new HeaderCallOption(headers));
      }
      credentials = authFactory.getCredentialCallOption();
    }
    try (final FlightSqlClient flightSqlClient = new FlightSqlClient(client)) {
      final FlightInfo info = flightSqlClient.execute("SELECT 42 as Foo", credentials);
      try (final FlightStream stream =
          flightSqlClient.getStream(info.getEndpoints().get(0).getTicket(), credentials)) {
        while (stream.next()) {
          log.info(stream.getRoot().contentToTSVString());
        }
      }
    }
  }
}
