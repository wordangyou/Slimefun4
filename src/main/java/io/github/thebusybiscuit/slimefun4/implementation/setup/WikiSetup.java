package io.github.thebusybiscuit.slimefun4.implementation.setup;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.Slimefun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

final class WikiSetup {

    private WikiSetup() {
    }

    public static void addWikiPages(SlimefunPlugin plugin) {
        JsonParser parser = new JsonParser();
        Slimefun.getLogger().log(Level.INFO, "Loading Wiki pages...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getClass().getResourceAsStream("/wiki.json")))) {
            JsonElement element = parser.parse(reader.lines().collect(Collectors.joining("")));
            JsonObject json = element.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                SlimefunItem item = SlimefunItem.getByID(entry.getKey());

                if (item != null) {
                    item.addOficialWikipage(entry.getValue().getAsString());
                }
            }
        } catch (IOException e) {
            Slimefun.getLogger().log(Level.SEVERE, "Failed to load wiki.json file", e);
        }
    }

}