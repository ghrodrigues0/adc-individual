package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 900; // 15m

	public String username;
	public String tokenId;
	public long issuedAt;
	public long expiresAt;
	public String role;

	public AuthToken() {
	}

	public AuthToken(String username, String userRole) {
		this.username = username;
		this.tokenId = UUID.randomUUID().toString();
		this.issuedAt = System.currentTimeMillis() / 1000;
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;
		this.role = userRole;
	}

	public String getTokenId() {
		return tokenId;
	}

	public String getUserId() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public long getIssuedAt() {
		return issuedAt;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

}
