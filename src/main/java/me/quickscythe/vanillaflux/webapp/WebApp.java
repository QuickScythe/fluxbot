package me.quickscythe.vanillaflux.webapp;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Feedback;
import me.quickscythe.vanillaflux.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.port;

public class WebApp {

    public WebApp() {
        port(Bot.WEB_PORT());
        get(Bot.API_ENTRY_POINT(), (req, res) -> {
            res.type("application/json");
            return Feedback.Errors.json("No UUID provided");
        });
        get(Bot.API_ENTRY_POINT() + "/polls/:action", (req, res) -> {
            String param = req.params(":action");
            res.type("application/json");
            if(param.endsWith(".png")){
                res.type("image/png");
                try (InputStream imageStream = new FileInputStream("polls/" + param)) {
                    byte[] imageBytes = imageStream.readAllBytes();
                    return imageBytes;
                } catch (IOException e) {
                    res.status(404);
                    return "Image not found";
                }
            }
            return Feedback.Errors.json("No UUID provided");
        });
        get(Bot.API_ENTRY_POINT() + "/:uuid", (req, res) -> {
            String param = req.params(":uuid");
            Utils.getLogger().log("Got a connection");
            Utils.getLogger().log("Param: " + param);
            res.type("application/json");
            try {
                try {
                    return Utils.getFluxApi().getPlayerData(UUID.fromString(param)).toString();
                } catch (IllegalArgumentException ex) {
                    Utils.getLogger().log("Couldn't find UUID based on " + param);
                    if (Utils.getFluxApi().searchUUID(param) == null) {
                        Utils.getLogger().log("User " + param + " not found");
                        return Feedback.Errors.json("User not found");
                    }
                    Utils.getLogger().log("User " + param + " found.");
                    return Utils.getFluxApi().getPlayerData(Utils.getFluxApi().searchUUID(req.params(":uuid"))).toString();
                }
            } catch (SQLException ex) {
                return Feedback.Errors.json("Internal Server Error: Couldn't connect to SQL Database");
            }
        });


        get(Bot.APP_ENTRY_POINT(), (req, res) -> {
            res.type("application/json");
            return Feedback.Errors.json("No path provided");
        });

        get(Bot.APP_ENTRY_POINT() + "/v1/:token/:action", (req, res) -> {
            res.type("application/json");
            String token = req.params(":token");
            String action = req.params(":action");
            String a = req.queryParams("a");
            String b = req.queryParams("b");
            String c = req.queryParams("c");
            if (TokenManager.validToken(TokenManager.getToken(token), req))
                return Feedback.Errors.json("Invalid token");
            if (action.equalsIgnoreCase("check_token")) {
                if (TokenManager.validToken(TokenManager.getToken(token), req))
                    return Feedback.Success.json("Valid Token");
                return Feedback.Errors.json("Invalid token");
            }
            if (action.equalsIgnoreCase("join") || action.equalsIgnoreCase("leave")) {
                //a = discord id, b = username, c = uuid
                if (a == null && b == null && c == null)
                    return Feedback.Errors.json("Must include at least one parameter");
                Member mem;
                //TODO Change to ONLINE_ROLE
                Role role = Utils.getGuild().getRoleById(Bot.ONLINE_ROLE());
                if (role == null) return Feedback.Errors.json("Role not found");
                if (a != null) {
                    mem = Utils.getGuild().getMemberById(Long.parseLong(a));
                    if (mem == null) return Feedback.Errors.json("User not found");
                } else {
                    UUID uid;
                    if (b != null) uid = Utils.getFluxApi().searchUUID(b);
                    else uid = UUID.fromString(c);
                    if (uid == null) return Feedback.Errors.json("User UUID found");
                    mem = Utils.getGuild().getMemberById(Utils.getFluxApi().getDiscordId(uid));
                }
                if (mem == null) return Feedback.Errors.json("Discord user not found");
                String message;
                if (action.equalsIgnoreCase("join")) {
                    message = "User " + mem.getAsMention() + " has been added to the online role";
                    Utils.getGuild().addRoleToMember(mem, role).queue();
                } else {
                    message = "User " + mem.getAsMention() + " has been removed from the online role";
                    Utils.getGuild().removeRoleFromMember(mem, role).queue();
                }
                Utils.getLogger().log(message, true);
                return Feedback.Success.json(message);


            }

            return Feedback.Success.json("Action " + action + " completed. Extra info: " + c);
        });

        get(Bot.APP_ENTRY_POINT() + "/token", (req, res) -> {
            res.type("application/json");
            String token = TokenManager.requestNewToken(req.ip());
            return token == null ? Feedback.Errors.json("Error generating token. IP Not allowed?") : Feedback.Success.json(token);
        });

        get(Bot.APP_ENTRY_POINT() + "/tokens", (req, res) -> {
            res.type("application/json");
            JSONObject feedback = new JSONObject();
            feedback.put("tokens", new JSONArray());
            for (String token : TokenManager.getTokens(req.ip())) {
                feedback.getJSONArray("tokens").put(token);
            }
            return feedback;
        });

        Utils.getLogger().log("WebApp started on port " + Bot.WEB_PORT(), !Bot.isDebug());
    }
}
