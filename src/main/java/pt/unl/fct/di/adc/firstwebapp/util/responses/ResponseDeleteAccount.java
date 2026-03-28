package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseDeleteAccount {

  public String status;
  public Data data;

  public ResponseDeleteAccount() {
    this.status = "success";
    this.data = new Data();
  }

  public static class Data {
    public String message = "Account deleted successfully";
  }
}