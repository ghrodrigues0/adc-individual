package pt.unl.fct.di.adc.firstwebapp.util.responses;

import pt.unl.fct.di.adc.firstwebapp.util.SessionData;

import java.util.List;

public class ResponseShowAuthSessions {

  public String status;
  public Data data;

  public ResponseShowAuthSessions(List<SessionData> sessions) {
    this.status = "success";
    this.data = new Data(sessions);
  }

  public static class Data {
    public List<SessionData> sessions;

    public Data(List<SessionData> sessions) {
      this.sessions = sessions;
    }
  }
}