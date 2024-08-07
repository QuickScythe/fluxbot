package me.quickscythe.vanillaflux.webapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.quickscythe.vanillaflux.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerApiHandler implements HttpHandler {

    UUID uid;

    public PlayerApiHandler(String uid) {
        this.uid = UUID.fromString(uid);
    }

    @Override
    public void handle(HttpExchange e) throws IOException {
        String response = "This is a response";
        try {
            response = Utils.getFluxApi().getPlayerData(uid).toString();
        } catch (SQLException ex) {
            Utils.getLogger().error("There was an error parsing user data", ex);
        }
        e.sendResponseHeaders(200, response.length());
        OutputStream os = e.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
