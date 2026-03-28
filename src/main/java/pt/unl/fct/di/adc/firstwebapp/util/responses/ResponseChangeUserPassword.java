package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseChangeUserPassword {

  public String status;
  public Data data;

  public ResponseChangeUserPassword() {
    this.status = "success";
    this.data = new Data();
  }

  public static class Data {
    public String message = "Password changed successfully";
  }
}