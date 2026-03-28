package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseModifyAccount {

  public String status;
  public Data data;

  public ResponseModifyAccount() {
    this.status = "success";
    this.data = new Data();
  }

  public static class Data {
    public String message = "Updated successfully";
  }
}