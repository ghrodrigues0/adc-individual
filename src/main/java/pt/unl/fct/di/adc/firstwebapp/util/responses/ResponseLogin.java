package pt.unl.fct.di.adc.firstwebapp.util.responses;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;

public class ResponseLogin {

  public String status;
  public Data data;

  public ResponseLogin(AuthToken token) {
    this.status = "success";
    this.data = new Data(token);
  }

  public static class Data {
    public Token token;

    public Data(AuthToken token) {
      this.token = new Token(token);
    }
  }

  public static class Token {
    public String tokenId;
    public String username;
    public String role;
    public long issuedAt;
    public long expiresAt;

    public Token(AuthToken token) {
      this.tokenId = token.getTokenId();
      this.username = token.getUserId();
      this.role = token.getRole();
      this.issuedAt = token.getIssuedAt();
      this.expiresAt = token.getExpiresAt();
    }
  }
}