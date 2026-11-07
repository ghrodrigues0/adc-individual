package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseLogout;
import pt.unl.fct.di.adc.firstwebapp.util.requests.LogoutRequest;

import java.util.logging.Logger;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

  private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

  public LogoutResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response logout(LogoutRequest req) {

    AuthToken token = req.token;
    LogoutRequest.LogoutInput input = req.input;

    if (input == null || input.username == null || input.username.isBlank())
      return Response.ok()
          .entity(new ResponseError("INVALID_INPUT", 9906, ResponseError.ErrorMsgs.INVALID_INPUT))
          .build();

    if (token == null || token.getTokenId() == null || token.getTokenId().isBlank())
      return Response.ok()
          .entity(new ResponseError("INVALID_TOKEN", 9903, ResponseError.ErrorMsgs.INVALID_TOKEN))
          .build();

    Key sessionKey = datastore.newKeyFactory().setKind("UserSession").newKey(token.getTokenId());
    Entity sessionEntity = datastore.get(sessionKey);

    if (sessionEntity == null)
      return Response.ok()
          .entity(new ResponseError("INVALID_TOKEN", 9903, ResponseError.ErrorMsgs.INVALID_TOKEN))
          .build();

    if (sessionEntity.getTimestamp("expiresAt").toDate().getTime() < System.currentTimeMillis())
      return Response.ok()
          .entity(new ResponseError("TOKEN_EXPIRED", 9904, ResponseError.ErrorMsgs.TOKEN_EXPIRED))
          .build();

    if (!sessionEntity.getString("role").equals("ADMIN") && !sessionEntity.getString("username").equals(input.username))
      return Response.ok()
          .entity(new ResponseError("FORBIDDEN", 9907, ResponseError.ErrorMsgs.FORBIDDEN))
          .build();

    try {
      datastore.delete(sessionKey);
      LOG.info("Logout successful for: " + input.username);
      return Response.ok(new ResponseLogout()).build();

    } catch (Exception e) {
      LOG.severe("Error during logout: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}