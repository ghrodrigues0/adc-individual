package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ShowUserRoleRequest {

  public ShowUserRoleInput input;
  public AuthToken token;

  public ShowUserRoleRequest() {
  }

  public static class ShowUserRoleInput {
    public String username;
  }
}