package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseChangeUserRole {

  public String status;
  public Data data;

  public ResponseChangeUserRole() {
    this.status = "success";
    this.data = new Data();
  }

  public static class Data {
    public String message = "Role updated successfully";
  }
}
