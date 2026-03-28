package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseLogout {

  public String status;
  public Data data;

  public ResponseLogout() {
    this.status = "success";
    this.data = new Data();
  }

  public static class Data {
    public String message = "Logout successful";
  }
}