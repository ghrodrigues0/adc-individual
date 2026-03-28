package pt.unl.fct.di.adc.firstwebapp.util.requests;

import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;

public class RegisterRequest {

  public RegisterData input;

  public RegisterRequest() {
  }

  public RegisterRequest(RegisterData input) {
    this.input = input;
  }
}