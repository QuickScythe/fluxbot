package me.quickscythe.vanillaflux.webapp;

import me.quickscythe.vanillaflux.Bot;
import me.quickscythe.vanillaflux.utils.Errors;
import me.quickscythe.vanillaflux.utils.Utils;
import me.quickscythe.vanillaflux.utils.logs.BotLogger;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.port;

public class WebApp {

    private final int PORT = 8585;
    private final Logger logger = new BotLogger("WebApp").getLog();

    public WebApp() {
        port(PORT);
        get(Bot.API_ENTRY_POINT, (req, res) -> Errors.json("No UUID provided"));
        get(Bot.API_ENTRY_POINT + "/:uuid", (req, res) -> {
            String param = req.params(":uuid");
            Utils.getLogger().log("Got a connection");
            Utils.getLogger().log("Param: " + param);
            try {
                try {
                    return Utils.getFluxApi().getPlayerData(UUID.fromString(param)).toString();
                } catch (IllegalArgumentException ex) {
                    Utils.getLogger().log("Couldn't find UUID based on " + param);
                    if (Utils.getFluxApi().searchUUID(param) == null) {
                        Utils.getLogger().log("User " + param + " not found");
                        return Errors.json("User not found");
                    }
                    Utils.getLogger().log("User " + param + " found.");
                    return Utils.getFluxApi().getPlayerData(Utils.getFluxApi().searchUUID(req.params(":uuid"))).toString();
                }
            } catch (SQLException ex) {
                return Errors.json("Internal Server Error: Couldn't connect to SQL Database");
            }
        });


        get(Bot.APP_ENTRY_POINT, (request, response) -> {
            response.redirect("https://www.vanillaflux.com");
            return "test";
        });
    }
}
