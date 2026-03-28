package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ShowUsersRequest {

  public RegisterData input;
  public AuthToken token;

  public ShowUsersRequest() {
  }

  public ShowUsersRequest(RegisterData input, AuthToken token) {
    this.input = input;
    this.token = token;
  }
}