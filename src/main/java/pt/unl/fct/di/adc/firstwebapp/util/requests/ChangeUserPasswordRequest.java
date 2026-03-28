package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ChangeUserPasswordRequest {

  public ChangePasswordInput input;
  public AuthToken token;

  public ChangeUserPasswordRequest() {}

  public static class ChangePasswordInput {
    public String username;
    public String oldPassword;
    public String newPassword;
  }
}