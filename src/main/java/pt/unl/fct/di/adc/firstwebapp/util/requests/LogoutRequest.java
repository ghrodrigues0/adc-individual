package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class LogoutRequest {

  public LogoutInput input;
  public AuthToken token;

  public LogoutRequest() {
  }

  public static class LogoutInput {
    public String username;
  }
}