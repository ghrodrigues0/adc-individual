package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.LoginData;

public class LoginRequest {

  public LoginData input;

  public LoginRequest() {
  }

  public LoginRequest(LoginData input) {
    this.input = input;
  }
}