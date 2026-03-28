package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ChangeUserRoleRequest {

  public ChangeUserRoleInput input;
  public AuthToken token;

  public ChangeUserRoleRequest() {
  }

  public static class ChangeUserRoleInput {
    public String username;
    public String newRole;
  }
}