package pt.unl.fct.di.adc.firstwebapp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes and verifies passwords with bcrypt.
 *
 * Accounts created before the switch to bcrypt still hold an unsalted SHA-512
 * hash. {@link #matches} accepts those legacy hashes so their owners can still
 * log in, and {@link #needsRehash} tells the caller to replace them with a
 * bcrypt hash right after a successful login.
 */
public final class PasswordHasher {

	/**
	 * bcrypt work factor: each +1 doubles the time to compute a hash. 10 keeps
	 * a login well under a second on a small App Engine instance; raise it if
	 * the hardware allows.
	 */
	private static final int COST = 10;

	/**
	 * A valid bcrypt hash of a random string, checked when the username does
	 * not exist so that the response takes about as long as a wrong password
	 * and timing does not reveal which usernames are registered.
	 */
	private static final String DUMMY_HASH = BCrypt.hashpw("dummy-password-for-timing", BCrypt.gensalt(COST));

	private PasswordHasher() {
	}

	public static String hash(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(COST));
	}

	public static boolean matches(String password, String storedHash) {
		if (password == null || storedHash == null)
			return false;
		if (isBcrypt(storedHash))
			return BCrypt.checkpw(password, storedHash);
		// Legacy unsalted SHA-512 hash: compare in constant time.
		byte[] candidate = DigestUtils.sha512Hex(password).getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(candidate, storedHash.getBytes(StandardCharsets.UTF_8));
	}

	public static boolean needsRehash(String storedHash) {
		return storedHash == null || !isBcrypt(storedHash);
	}

	/** Spends the same time as a real check, for logins with an unknown username. */
	public static void simulateCheck(String password) {
		BCrypt.checkpw(password == null ? "" : password, DUMMY_HASH);
	}

	/** jBCrypt 0.4 writes and reads the "$2a$" variant only. */
	private static boolean isBcrypt(String hash) {
		return hash.startsWith("$2a$");
	}
}
