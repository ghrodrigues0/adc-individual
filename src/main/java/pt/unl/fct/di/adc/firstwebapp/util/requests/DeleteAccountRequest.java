package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class DeleteAccountRequest {

  public DeleteInput input;
  public AuthToken token;

  public DeleteAccountRequest() {
  }

  public static class DeleteInput {
    public String username;
  }
}