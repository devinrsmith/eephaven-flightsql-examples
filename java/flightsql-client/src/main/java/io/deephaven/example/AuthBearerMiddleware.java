package io.deephaven.example;

import java.util.Objects;
import org.apache.arrow.flight.CallHeaders;
import org.apache.arrow.flight.CallInfo;
import org.apache.arrow.flight.CallStatus;
import org.apache.arrow.flight.FlightClientMiddleware;
import org.apache.arrow.flight.auth2.Auth2Constants;
import org.apache.arrow.flight.auth2.AuthUtilities;

public class AuthBearerMiddleware implements FlightClientMiddleware {

  private final Factory factory;

  private AuthBearerMiddleware(Factory factory) {
    this.factory = Objects.requireNonNull(factory);
  }

  @Override
  public void onHeadersReceived(CallHeaders incomingHeaders) {
    final String bearerValue =
        AuthUtilities.getValueFromAuthHeader(incomingHeaders, Auth2Constants.BEARER_PREFIX);
    if (bearerValue != null) {
      factory.setBearerValue(bearerValue);
    }
  }

  @Override
  public void onBeforeSendingHeaders(CallHeaders outgoingHeaders) {
    final String bearerValue = factory.getBearerValue();
    if (bearerValue != null) {
      outgoingHeaders.insert(
          Auth2Constants.AUTHORIZATION_HEADER, Auth2Constants.BEARER_PREFIX + bearerValue);
    }
  }

  @Override
  public void onCallCompleted(CallStatus status) {}

  public static class Factory implements FlightClientMiddleware.Factory {

    private volatile String bearerValue;

    public Factory() {}

    @Override
    public FlightClientMiddleware onCallStarted(CallInfo info) {
      return new AuthBearerMiddleware(this);
    }

    String getBearerValue() {
      return bearerValue;
    }

    void setBearerValue(String bearerValue) {
      // Avoid volatile write if the value hasn't changed
      if (!Objects.equals(this.bearerValue, bearerValue)) {
        this.bearerValue = bearerValue;
      }
    }
  }
}
