package pt.unl.fct.di.adc.firstwebapp.util.responses;

public class ResponseRegister {

  public String status;
  public Data data;

  public ResponseRegister(String status, String username, String role) {
    this.status = status;
    this.data = new Data(username, role);
  }

  public static class Data {
    public String username;
    public String role;

    public Data(String username, String role) {
      this.username = username;
      this.role = role;
    }
  }
}