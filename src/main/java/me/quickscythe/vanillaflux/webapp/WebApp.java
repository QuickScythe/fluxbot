package me.quickscythe.vanillaflux.webapp;

import json2.JSONArray;
import json2.JSONObject;
import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Feedback;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.data.DataManager;
import me.quickscythe.vanillaflux.utils.polls.charts.ChartGenerator;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.port;

public class WebApp {

    public WebApp() {
        port(Bot.WEB_PORT());
        get(Bot.API_ENTRY_POINT() + "/graphs/:graph", (req, res) -> {
            String option1 = "";
            String option2 = "";
            boolean sort = (req.queryParams("sort") != null && Boolean.parseBoolean(req.queryParams("sort")));
            final long now = new Date().getTime();
            String graph = req.params(":graph");
            String users = req.queryParams("users");
            users = users == null ? "*" : users;
            List<String> userList = Arrays.asList(users.split(","));
            Map<UUID, JSONObject> data = new HashMap<>();
            ResultSet rs = SqlUtils.getDatabase("core").query("SELECT * FROM users");
            while (rs.next()) {
                if (!userList.contains("*") && !userList.contains(rs.getString("username")) && !userList.contains(rs.getString("uuid")))
                    continue;
                System.out.println(rs.getString("username"));
                Blob blob = rs.getBlob("json");
                InputStream inputStream = blob.getBinaryStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                JSONObject playerData = new JSONObject(outputStream.toString(StandardCharsets.UTF_8));
                playerData.put("username", rs.getString("username"));
                data.put(UUID.fromString(rs.getString("uuid")), playerData);
            }

            if (graph.equalsIgnoreCase("custom")) {
                option1 = req.queryParams("option1");
                option2 = req.queryParams("option2");
                if (option1 == null) {
                    res.status(400);
                    return "Missing parameters";
                }

                if (option2 == null) {
                    option2 = "playtime";
                }
            } else switch (graph) {
                case "jumps":
                    option1 = "jumps";
                    option2 = "playtime";
                    break;
                case "deaths":
                    option1 = "deaths";
                    option2 = "sessions";
                    break;
                case "kd":
                    option1 = "kills";
                    option2 = "deaths";
                    break;
                case "playtime":
                    option1 = "playtime";
                    option2 = "sessions";
                    break;
                case "kills":
                    option1 = "kills";
                    option2 = "sessions";
                    break;
                default:
                    res.status(400);
                    res.type("application/json");
                    JSONObject feedback = new JSONObject();
                    feedback.put("error", "Invalid graph");
                    feedback.put("valid_graphs", new JSONArray(Arrays.asList("jumps", "deaths", "kd", "playtime", "kills")));
                    return feedback.toString(2);

            }

            generateCustomChart(data, option1, option2, now, sort);

            try (InputStream imageStream = new FileInputStream(now + ".png")) {
                res.type("image/png");
                return imageStream.readAllBytes();
            } catch (IOException e) {
                res.status(404);
                return "Image not found";
            }

        });

        get(Bot.API_ENTRY_POINT(), (req, res) -> {
            res.type("application/json");
            return Feedback.Errors.json("No UUID provided");
        });
        get(Bot.API_ENTRY_POINT() + "/polls/:action", (req, res) -> {
            String param = req.params(":action");
            res.type("application/json");
            if (param.endsWith(".png")) {
                res.type("image/png");
                String id = param.split("\\.")[0];
                try (InputStream imageStream = new FileInputStream("polls/" + id + "/results.png")) {
                    byte[] imageBytes = imageStream.readAllBytes();
                    return imageBytes;
                } catch (IOException e) {
                    res.status(404);
                    return "Image not found";
                }
            }

            String content = DataManager.getFileContents(new File("polls/" + param + "/data.json"));
            if (content == null) return Feedback.Errors.json("Poll not found");
            return content;
//            if(PollUtils.getPoll(Long.parseLong(param)) == null) return Feedback.Errors.json("Poll not found");
//            return PollUtils.getPoll(Long.parseLong(param)).json();
//            return Feedback.Errors.json("No UUID provided");
        });
        get(Bot.API_ENTRY_POINT() + "/:uuid/:data", (req, res) -> {

            JSONObject data = new JSONObject(getDataFromUUID(req.params(":uuid")));
            JSONObject server_info = data.getJSONObject("server_info");
            int jumps = 0;
            int sessions = server_info.getJSONArray("sessions").length();
            for (int i = 0; i != sessions; i++) {
                JSONObject session = server_info.getJSONArray("sessions").getJSONObject(i);
                jumps = jumps + session.getInt("jumps");
            }
            return "Average Jumps/Session: " + jumps / sessions;
        });
        get(Bot.API_ENTRY_POINT() + "/:uuid", (req, res) -> {
            res.type("application/json");
            return getDataFromUUID(req.params(":uuid"));
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

    private void generateJumpChart(Map<UUID, JSONObject> data, long now) {
    }

    private void generateCustomChart(Map<UUID, JSONObject> data, String option1, String option2, long timeStamp, boolean sort) {
        Map<String, Float> option1Data = new HashMap<>();

        for (Map.Entry<UUID, JSONObject> entry : data.entrySet()) {
            String username = entry.getValue().getString("username");
            if (!entry.getValue().has("sessions")) continue;
            int sessions = entry.getValue().getJSONArray("sessions").length();
            int option1Value = 0;
            int option2Value = 0;
            for (int i = 0; i != sessions; i++) {
                JSONObject session = entry.getValue().getJSONArray("sessions").getJSONObject(i);
                if (option1.equalsIgnoreCase("kills") || option2.equalsIgnoreCase("kills")) {
                    if (session.has("kills")) {
                        for (String key : session.getJSONObject("kills").keySet()) {
                            if (key.toLowerCase().contains("enderman")) continue;
                            if (username.equals("WolfButtercup") && (key.toLowerCase().contains("zombie") || key.toLowerCase().contains("drowned")))
                                continue;
                            if (option1.equalsIgnoreCase("kills"))
                                option1Value = option1Value + session.getJSONObject("kills").getInt(key);
                            if (option2.equalsIgnoreCase("kills"))
                                option2Value = option2Value + session.getJSONObject("kills").getInt(key);
                        }
                    }
                }

                if (!option1.equalsIgnoreCase("kills") && session.has(option1))
                    option1Value = option1Value + session.getInt(option1);
                if (!option2.equalsIgnoreCase("kills") && !option2.equalsIgnoreCase("sessions") && session.has(option2))
                    option2Value = option2Value + session.getInt(option2);
            }
            if (option2.equalsIgnoreCase("playtime")) option2Value = option2Value / 1000 / 60;
            if (option1.equalsIgnoreCase("playtime")) option1Value = option1Value / 1000 / 60;
            if (option2.equalsIgnoreCase("deaths") && option2Value == 0) option2Value = 1;
            if (option2.equalsIgnoreCase("sessions")) option2Value = sessions;
            option1Data.put(username, (float) (((float) option1Value) / ((float) option2Value)));
        }


        if (option1.equalsIgnoreCase("playtime")) option1 = "Playtime (in Minutes)";
        else option1 = option1.substring(0, 1).toUpperCase() + option1.substring(1);
        if (option2.equalsIgnoreCase("playtime")) option2 = "Playtime (in Minutes)";
        else option2 = option2.substring(0, 1).toUpperCase() + option2.substring(1);
        ChartGenerator.generateBarChart(option1 + " vs " + option2, option1Data, "User", option1, timeStamp + ".png", sort);
    }

    private String getDataFromUUID(String uuid) {

        try {
            try {
                return Utils.getFluxApi().getPlayerData(UUID.fromString(uuid)).toString();
            } catch (IllegalArgumentException ex) {
                Utils.getLogger().log("Couldn't find UUID based on " + uuid);
                if (Utils.getFluxApi().searchUUID(uuid) == null) {
                    Utils.getLogger().log("User " + uuid + " not found");
                    return Feedback.Errors.json("User not found");
                }
                Utils.getLogger().log("User " + uuid + " found.");
                return Utils.getFluxApi().getPlayerData(Utils.getFluxApi().searchUUID(uuid)).toString();
            }
        } catch (SQLException ex) {
            return Feedback.Errors.json("Internal Server Error: Couldn't connect to SQL Database");
        }
    }
}
