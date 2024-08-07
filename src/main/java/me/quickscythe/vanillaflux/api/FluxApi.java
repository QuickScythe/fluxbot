package me.quickscythe.vanillaflux.api;

import json2.JSONObject;
import me.quickscythe.vanillaflux.utils.sql.SqlUtils;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FluxApi implements Api {

    @Override
    public JSONObject getPlayerData(UUID uid) throws SQLException {

        ResultSet rs = SqlUtils.getDatabase("core").query("SELECT * FROM users WHERE uuid='" + uid.toString() + "';");

        if (rs.next()) {
            Blob blob = rs.getBlob("json");
            JSONObject play_stats = new JSONObject(new String(blob.getBytes(1, (int) blob.length())));
            JSONObject json = new JSONObject();
            json.put("server_info", play_stats);
            json.put("last_seen", Long.parseLong(rs.getString("last_seen")));
            json.put("discord_linked", !rs.getString("discord_key").equals("null"));
            json.put("uuid", UUID.fromString(rs.getString("uuid")));
            json.put("username", rs.getString("username"));
            return json;
        }
        return null;

    }
}
