package pt.unl.fct.di.adc.firstwebapp.util;

public class SessionData {

  public String tokenId;
  public String username;
  public String role;
  public long expiresAt;

  public SessionData(String tokenId, String username, String role, long expiresAt) {
    this.tokenId = tokenId;
    this.username = username;
    this.role = role;
    this.expiresAt = expiresAt;
  }
}