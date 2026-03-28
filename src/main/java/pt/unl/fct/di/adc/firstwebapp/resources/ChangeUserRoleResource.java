package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseChangeUserRole;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ChangeUserRoleRequest;

import java.util.logging.Logger;

@Path("/changeuserrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeUserRoleResource {

  private static final Logger LOG = Logger.getLogger(ChangeUserRoleResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private final Gson g = new Gson();

  public ChangeUserRoleResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response changeUserRole(ChangeUserRoleRequest req) {

    ChangeUserRoleRequest.ChangeUserRoleInput input = req.input;
    AuthToken token = req.token;

    if (input == null || input.username == null || input.username.isBlank() || input.newRole == null || input.newRole.isBlank()
        || (!input.newRole.equals("USER") && !input.newRole.equals("BOFFICER") && !input.newRole.equals("ADMIN")))
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

    if (!sessionEntity.getString("role").equals("ADMIN"))
      return Response.ok()
          .entity(new ResponseError("UNAUTHORIZED", 9905, ResponseError.ErrorMsgs.UNAUTHORIZED))
          .build();

    Key userKey = datastore.newKeyFactory().setKind("User").newKey(input.username);

    Transaction txn = datastore.newTransaction();
    try {
      Entity targetUser = txn.get(userKey);

      if (targetUser == null) {
        txn.rollback();
        return Response.ok()
            .entity(new ResponseError("USER_NOT_FOUND", 9902, ResponseError.ErrorMsgs.USER_NOT_FOUND))
            .build();
      }

      Entity updatedUser = Entity.newBuilder(targetUser).set("role", input.newRole).build();

      txn.put(updatedUser);
      txn.commit();

      LOG.info("Role updated for " + input.username + " to " + input.newRole);
      return Response.ok(new ResponseChangeUserRole()).build();

    } catch (Exception e) {
      if (txn.isActive())
        txn.rollback();
      LOG.severe("Error changing role: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
}