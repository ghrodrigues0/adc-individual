package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseChangeUserPassword;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ChangeUserPasswordRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/changeuserpwd")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeUserPasswordResource {

    private static final Logger LOG = Logger.getLogger(ChangeUserPasswordResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();

    public ChangeUserPasswordResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangeUserPasswordRequest req) {

        ChangeUserPasswordRequest.ChangePasswordInput input = req.input;
        AuthToken token = req.token;

        if (input == null || input.username == null || input.username.isBlank()
                || input.oldPassword == null || input.oldPassword.isBlank()
                || input.newPassword == null || input.newPassword.isBlank())
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

        String callerUsername = sessionEntity.getString("username");
        if (!callerUsername.equals(input.username))
            return Response.ok()
                    .entity(new ResponseError("FORBIDDEN", 9907, ResponseError.ErrorMsgs.FORBIDDEN))
                    .build();

        Key userKey = datastore.newKeyFactory().setKind("User").newKey(input.username);

        Transaction txn = datastore.newTransaction();
        try {
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                return Response.ok()
                        .entity(new ResponseError("USER_NOT_FOUND", 9902, ResponseError.ErrorMsgs.USER_NOT_FOUND))
                        .build();
            }

            String storedHash = user.getString("user_pwd");
            if (!storedHash.equals(DigestUtils.sha512Hex(input.oldPassword))) {
                txn.rollback();
                return Response.ok()
                        .entity(new ResponseError("INVALID_CREDENTIALS", 9900, ResponseError.ErrorMsgs.INVALID_CREDENTIALS))
                        .build();
            }

            Entity updatedUser = Entity.newBuilder(user)
                    .set("user_pwd", DigestUtils.sha512Hex(input.newPassword))
                    .build();

            txn.put(updatedUser);
            txn.commit();

            Query<Entity> sessionsQuery = Query.newEntityQueryBuilder()
                    .setKind("UserSession")
                    .setFilter(StructuredQuery.PropertyFilter.eq("username", input.username))
                    .build();
            QueryResults<Entity> sessions = datastore.run(sessionsQuery);
            List<Key> sessionKeysToDelete = new ArrayList<>();
            sessions.forEachRemaining(s -> sessionKeysToDelete.add(s.getKey()));

            if (!sessionKeysToDelete.isEmpty())
                datastore.delete(sessionKeysToDelete.toArray(new Key[0]));

            LOG.info("Password changed for: " + input.username);
            return Response.ok(new ResponseChangeUserPassword()).build();

        } catch (Exception e) {
            if (txn.isActive())
                txn.rollback();
            LOG.severe("Error changing password: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}