package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.doctordark.util.JavaUtils;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class that can handle schedules for game events.
 */
public class EventScheduler {

    private static final String FILE_NAME = "event-schedules.txt";
    private static final long QUERY_DELAY = TimeUnit.SECONDS.toMillis(60L);

    private long lastQuery;

    private final Map<LocalDateTime, String> scheduleMap = new LinkedHashMap<>();
    private final HCF plugin;

    public EventScheduler(HCF plugin) {
        this.plugin = plugin;
        this.reloadSchedules();
    }

    private void reloadSchedules() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), FILE_NAME)), StandardCharsets.UTF_8))) {

            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (currentLine.startsWith("#")) continue; // ignore comments;

                currentLine = currentLine.trim();
                String[] args = currentLine.split(":");
                if (args.length != 2) continue;

                LocalDateTime localDateTime = getFromString(args[0]);
                if (localDateTime == null) continue;

                scheduleMap.put(localDateTime, args[1]);
            }
        } catch (FileNotFoundException ex) {
            Bukkit.getConsoleSender().sendMessage("Could not find file " + FILE_NAME + '.');
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Map<LocalDateTime, String> getScheduleMap() {
        long millis = System.currentTimeMillis();
        if (millis - QUERY_DELAY > lastQuery) {
            this.reloadSchedules();
            this.lastQuery = millis;
        }

        return scheduleMap;
    }

    /**
     * Converts a {@link LocalDateTime} from a string input.
     *
     * @param input the string to convert from
     * @return the converted {@link LocalDateTime} or null if cannot parse
     */
    private static LocalDateTime getFromString(String input) {
        //TODO: Use DateTimeFormatterBuilder
        if (!input.contains(",")) return null;

        String[] args = input.split(",");
        if (args.length != 5) return null;

        Integer year = JavaUtils.tryParseInt(args[0]);
        if (year == null) return null;

        Integer month = JavaUtils.tryParseInt(args[1]);
        if (month == null) return null;

        Integer day = JavaUtils.tryParseInt(args[2]);
        if (day == null) return null;

        Integer hour = JavaUtils.tryParseInt(args[3]);
        if (hour == null) return null;

        Integer minute = JavaUtils.tryParseInt(args[4]);
        if (minute == null) return null;

        return LocalDateTime.of(year, month, day, hour, minute);
    }
}
