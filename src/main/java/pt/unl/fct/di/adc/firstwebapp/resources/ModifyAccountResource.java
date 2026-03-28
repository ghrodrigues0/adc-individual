package pt.unl.fct.di.adc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseError;
import pt.unl.fct.di.adc.firstwebapp.util.responses.ResponseModifyAccount;
import pt.unl.fct.di.adc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.util.requests.ModifyAccountRequest;

import java.util.Objects;
import java.util.logging.Logger;

@Path("/modaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAccountResource {

  private static final Logger LOG = Logger.getLogger(ModifyAccountResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private final Gson g = new Gson();

  public ModifyAccountResource() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response modifyAccount(ModifyAccountRequest req) {

    AuthToken token = req.token;
    ModifyAccountRequest.ModifyAccountInput input = req.input;

    if (input == null || input.username == null || input.username.isBlank()
        || input.attributes == null
        || (input.attributes.phone == null && input.attributes.address == null))
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

    String target = input.username;
    String role = sessionEntity.getString("role");
    String username = sessionEntity.getString("username");

    if (role.equals("USER")) {
      if (!username.equals(target))
        return Response.ok()
            .entity(new ResponseError("UNAUTHORIZED", 9905, ResponseError.ErrorMsgs.UNAUTHORIZED))
            .build();
    } else if (role.equals("BOFFICER")) {
      if (!username.equals(target)) {
        Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(target);
        Entity targetEntity = datastore.get(targetUserKey);

        if (targetEntity == null)
          return Response.ok()
              .entity(new ResponseError("USER_NOT_FOUND", 9902, ResponseError.ErrorMsgs.USER_NOT_FOUND))
              .build();

        if (!targetEntity.getString("role").equals("USER"))
          return Response.ok()
              .entity(new ResponseError("FORBIDDEN", 9907, ResponseError.ErrorMsgs.FORBIDDEN))
              .build();
      }
    }

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

      Entity.Builder updatedUser = Entity.newBuilder(userEntity);

      if (input.attributes.phone != null && !input.attributes.phone.isBlank())
        updatedUser.set("phone", input.attributes.phone);

      if (input.attributes.address != null && !input.attributes.address.isBlank())
        updatedUser.set("address", input.attributes.address);

      txn.put(updatedUser.build());
      txn.commit();

      LOG.info("Account modified: " + target);
      return Response.ok(new ResponseModifyAccount()).build();

    } catch (Exception e) {
      if (txn.isActive())
        txn.rollback();
      LOG.severe("Error modifying account: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

  }

}
