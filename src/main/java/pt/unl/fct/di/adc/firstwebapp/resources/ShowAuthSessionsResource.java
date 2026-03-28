package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseShowAuthSessions;
import pt.unl.fct.di.adc.firstwebapp.util.SessionData;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ShowAuthSessionsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/showauthsessions")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowAuthSessionsResource {

  private static final Logger LOG = Logger.getLogger(ShowAuthSessionsResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private final Gson g = new Gson();

  public ShowAuthSessionsResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response showSessions(ShowAuthSessionsRequest req) {

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

    if (!sessionEntity.getString("role").equals("ADMIN"))
      return Response.ok()
          .entity(new ResponseError("UNAUTHORIZED", 9905, ResponseError.ErrorMsgs.UNAUTHORIZED))
          .build();

    Query<Entity> query = Query.newEntityQueryBuilder()
        .setKind("UserSession")
        .setFilter(StructuredQuery.PropertyFilter
            .gt("expiresAt", Timestamp.ofTimeSecondsAndNanos(System.currentTimeMillis() / 1000, 0)))
        .build();

    QueryResults<Entity> results = datastore.run(query);
    List<SessionData> sessions = new ArrayList<>();

    results.forEachRemaining(s -> {
      String tokenId = s.getKey().getName();
      String username = s.getString("username");
      String role = s.getString("role");
      long expiresAt = s.getTimestamp("expiresAt").getSeconds();
      sessions.add(new SessionData(tokenId, username, role, expiresAt));
    });

    return Response.ok(new ResponseShowAuthSessions(sessions)).build();
  }
}