package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.api.ErrorReason;
import com.google.cloud.datastore.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.adc.firstwebapp.util.User;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ShowUsersRequest;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseShowUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/showusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

  private static final Logger LOG = Logger.getLogger(ShowUsersResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

  public ShowUsersResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)

  public Response showUsers(ShowUsersRequest req, @Context HttpServletRequest request, @Context HttpHeaders headers) {

    AuthToken token = req.token;

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

    String role = sessionEntity.getString("role");
    if (!role.equals("ADMIN") && !role.equals("BOFFICER"))
      return Response.ok()
          .entity(new ResponseError("UNAUTHORIZED", 9905, ResponseError.ErrorMsgs.UNAUTHORIZED))
          .build();

    Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();

    QueryResults<Entity> results = datastore.run(query);
    List<User> users = new ArrayList<>();

    results.forEachRemaining(u -> {
      users.add(new User(u.getKey().getName(), u.getString("role")));
    });

    return Response.ok(new ResponseShowUsers(users)).build();

  }

}
