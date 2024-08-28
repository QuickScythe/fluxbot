package me.quickscythe.vanillaflux.api;

import json2.JSONObject;

import java.sql.SQLException;
import java.util.UUID;

public interface Api {

    public JSONObject getPlayerData(UUID uid) throws SQLException;

    UUID searchUUID(String params) throws SQLException;
}
