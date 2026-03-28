package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ModifyAccountRequest {

  public ModifyAccountInput input;
  public AuthToken token;

  public ModifyAccountRequest() {
  }

  public static class ModifyAccountInput {
    public String username;
    public Attributes attributes;

    public static class Attributes {
      public String phone;
      public String address;
    }
  }
}