package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.requests.DeleteAccountRequest;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseDeleteAccount;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/deleteaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteAccountResource {

  private static final Logger LOG = Logger.getLogger(DeleteAccountResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private final Gson g = new Gson();

  public DeleteAccountResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteAccount(DeleteAccountRequest req) {

    AuthToken token = req.token;
    String target = req.input.username;

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

    Key userKey = datastore.newKeyFactory().setKind("User").newKey(target);

    Transaction txn = datastore.newTransaction();

    try {

      Entity userEntity = txn.get(userKey);
      if (userEntity == null) {
        txn.rollback();
        return Response.ok()
            .entity(new ResponseError("USER_NOT_FOUND", 9902, ResponseError.ErrorMsgs.USER_NOT_FOUND))
            .build();
      }

      Query<Entity> sessionsQuery = Query.newEntityQueryBuilder()
          .setKind("UserSession")
          .setFilter(StructuredQuery.PropertyFilter.eq("username", target))
          .build();
      QueryResults<Entity> sessions = datastore.run(sessionsQuery);
      List<Key> sessionKeysToDelete = new ArrayList<>();
      sessions.forEachRemaining(s -> sessionKeysToDelete.add(s.getKey()));

      txn.delete(userKey);
      txn.commit();

      if (!sessionKeysToDelete.isEmpty())
        datastore.delete(sessionKeysToDelete.toArray(new Key[0]));

      LOG.info("Account deleted: " + target);
      return Response.ok(new ResponseDeleteAccount()).build();

    } catch (Exception e) {
      if (txn.isActive())
        txn.rollback();
      LOG.severe("Error deleting account: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

  }

}
