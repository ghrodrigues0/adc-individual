package pt.unl.fct.di.adc.firstwebapp.util.responses;

import java.util.List;

import pt.unl.fct.di.adc.firstwebapp.util.User;

public class ResponseShowUsers {

  public String status;
  public Data data;

  public ResponseShowUsers(List<User> users) {
    this.status = "success";
    this.data = new Data(users);
  }

  public static class Data {
    public List<User> users;

    public Data(List<User> users) {
      this.users = users;
    }
  }
}