package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.logging.Logger;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;

import jakarta.servlet.http.HttpServletRequest;

import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.LoginData;
import pt.unl.fct.di.adc.firstwebapp.util.PasswordHasher;
import pt.unl.fct.di.adc.firstwebapp.util.requests.LoginRequest;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseLogin;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError.ErrorMsgs;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;

import com.google.gson.Gson;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";

	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";

	private static final String USER_PWD = "user_pwd";
	private static final String USER_LOGIN_TIME = "user_login_time";

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();

	public LoginResource() {
	} // Nothing to be done here

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginRequest req, @Context HttpServletRequest request, @Context HttpHeaders headers) {

		LoginData data = req.input;

		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.username);

		if (!data.isValidLogin()) {

			return Response.ok()
					.entity(new ResponseError("INVALID_INPUT", 9906, ResponseError.ErrorMsgs.INVALID_INPUT))
					.build();
		}

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);

		Key logKey = datastore.allocateId(
				datastore.newKeyFactory()
						.addAncestors(PathElement.of("User", data.username))
						.setKind("UserLog").newKey());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				// Unknown username: answer exactly as for a wrong password, and spend
				// the same time on a hash check, so callers can't tell which
				// usernames are registered.
				PasswordHasher.simulateCheck(data.password);
				LOG.warning(LOG_MESSAGE_UNKNOW_USER + data.username);

				return Response.ok()
						.entity(new ResponseError("INVALID_CREDENTIALS", 9900, ResponseError.ErrorMsgs.INVALID_CREDENTIALS))
						.build();
			}

			String hashedPWD = user.getString(USER_PWD);
			if (!PasswordHasher.matches(data.password, hashedPWD)) {
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.username);
				return Response.ok()
						.entity(new ResponseError("INVALID_CREDENTIALS", 9900, ResponseError.ErrorMsgs.INVALID_CREDENTIALS))
						.build();
			}

			// Accounts created before the switch to bcrypt still hold a SHA-512
			// hash. The password was just verified, so replace it with a bcrypt
			// hash in this same transaction.
			if (PasswordHasher.needsRehash(hashedPWD)) {
				user = Entity.newBuilder(user)
						.set(USER_PWD, PasswordHasher.hash(data.password))
						.build();
				txn.put(user);
			}

			AuthToken token = new AuthToken(data.username, user.getString("role"));
			String cityLatLong = headers.getHeaderString("X-AppEngine-CityLatLong");
			Entity log = Entity.newBuilder(logKey)
					.set("user_login_ip", request.getRemoteAddr())
					.set("user_login_host", request.getRemoteHost())
					.set("user_login_latlon", cityLatLong != null
							? StringValue.newBuilder(cityLatLong).setExcludeFromIndexes(true).build()
							: StringValue.newBuilder("").setExcludeFromIndexes(true).build())
					.set("user_login_city", headers.getHeaderString("X-AppEngine-City") != null
							? headers.getHeaderString("X-AppEngine-CIty")
							: "")
					.set("user_login_country", headers.getHeaderString("X-AppEngine-Country") != null
							? headers.getHeaderString("X-AppEngine-Country")
							: "")
					.set("user_login_time", Timestamp.now())
					.build();

			Key sessionKey = datastore.newKeyFactory().setKind("UserSession").newKey(token.getTokenId());
			Entity sessioEntity = Entity.newBuilder(sessionKey)
					.set("username", token.getUserId())
					.set("role", token.getRole())
					.set("issuedAt", Timestamp.ofTimeSecondsAndNanos(token.getIssuedAt(), 0))
					.set("expiresAt", Timestamp.ofTimeSecondsAndNanos(token.getExpiresAt(), 0))
					.build();

			txn.put(sessioEntity);
			txn.put(log);
			txn.commit();

			return Response.ok(new ResponseLogin(token)).build();
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getClass().getName() + ": " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

}