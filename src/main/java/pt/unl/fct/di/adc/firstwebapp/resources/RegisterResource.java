package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.DatastoreOptions;

import pt.unl.fct.di.adc.firstwebapp.util.PasswordHasher;
import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.adc.firstwebapp.util.requests.RegisterRequest;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseRegister;

@Path("/createaccount")
public class RegisterResource {

	/**
	 * Every self-registered account starts as USER. Only an ADMIN can change a
	 * role afterwards, through /changeuserrole.
	 */
	private static final String DEFAULT_ROLE = "USER";

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();

	public RegisterResource() {
	} // Default constructor, nothing to do

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerUser(RegisterRequest req) {

		RegisterData data = req.input;

		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration())
			return Response.ok()
					.entity(new ResponseError("INVALID_INPUT", 9906, ResponseError.ErrorMsgs.INVALID_INPUT))
					.build();

		Transaction txn = datastore.newTransaction();

		try {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);

			if (user != null) {
				txn.rollback();
				return Response.ok()
						.entity(new ResponseError("USER_ALREADY_EXISTS", 9901, ResponseError.ErrorMsgs.USER_ALREADY_EXISTS))
						.build();
			} else {
				user = Entity.newBuilder(userKey)
						.set("user_name", data.username)
						.set("user_pwd", PasswordHasher.hash(data.password))
						.set("phone", data.phone)
						.set("address", data.address)
						.set("role", DEFAULT_ROLE)
						.set("creation_time", Timestamp.now())
						.build();
				txn.put(user);
				txn.commit();
				LOG.info("User registered " + data.username);
				return Response.ok(new ResponseRegister("success", data.username, DEFAULT_ROLE)).build();
			}
		} catch (Exception e) {
			LOG.severe("Error registering user: " + e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error registering user.").build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}
}