package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseShowUserRole;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ShowUserRoleRequest;

import java.util.logging.Logger;

@Path("/showuserrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUserRoleResource {

  private static final Logger LOG = Logger.getLogger(ShowUserRoleResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private final Gson g = new Gson();

  public ShowUserRoleResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response showUserRole(ShowUserRoleRequest req) {

    AuthToken token = req.token;
    ShowUserRoleRequest.ShowUserRoleInput input = req.input;

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

    String callerRole = sessionEntity.getString("role");
    if (!callerRole.equals("ADMIN") && !callerRole.equals("BOFFICER"))
      return Response.ok()
          .entity(new ResponseError("UNAUTHORIZED", 9905, ResponseError.ErrorMsgs.UNAUTHORIZED))
          .build();

    Key userKey = datastore.newKeyFactory().setKind("User").newKey(input.username);
    Entity targetUser = datastore.get(userKey);

    if (targetUser == null)
      return Response.ok()
          .entity(new ResponseError("USER_NOT_FOUND", 9902, ResponseError.ErrorMsgs.USER_NOT_FOUND))
          .build();

    return Response.ok(new ResponseShowUserRole(input.username, targetUser.getString("role"))).build();
  }
}