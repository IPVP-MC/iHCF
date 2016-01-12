package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that can handle schedules for game events.
 */
public class EventScheduler implements IEventScheduler {

    /*private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy,MM,dd,hh,mm,a", Locale.ENGLISH);
    private static final String FILE_NAME = "event-schedules.txt";
    private static final long QUERY_DELAY = TimeUnit.SECONDS.toMillis(60L);

    private long lastQuery;
    */

    private final ImmutableList<String> kothNames = ImmutableList.of("SG", "Nether", "City", "Ravel");
    private final Map<LocalDateTime, String> scheduleMap = new LinkedHashMap<>();
    private final HCF plugin;

    public EventScheduler(HCF plugin) {
        this.plugin = plugin;
        reloadSchedules();
    }

    private void reloadSchedules() {
        scheduleMap.clear();

        if (kothNames.size() < 2) {
            Bukkit.getLogger().warning("Less than 2 koths defined");
        } else {
            Bukkit.getLogger().info("Defining hardcoded schedules");
            LocalDateTime now = LocalDateTime.now(plugin.getConfiguration().getServerTimeZoneID());

            int assigned = 0;
            String lastPicked = null;
            while (scheduleMap.size() < 6) {
                String kothName = kothNames.get(plugin.getRandom().nextInt(kothNames.size()));
                if (lastPicked == null || !kothName.equals(lastPicked)) {
                    assigned++;
                    lastPicked = kothName;

                    int assignedHour;
                    if (assigned == 1) {
                        assignedHour = 9;
                    } else if (assigned == 2) {
                        assignedHour = 11;
                    } else if (assigned == 3) {
                        assignedHour = 15;
                    } else if (assigned == 4) {
                        assignedHour = 17;
                    } else if (assigned == 5) {
                        assignedHour = 21;
                    } else if (assigned == 6) {
                        assignedHour = 23;
                    } else {
                        // should be impossible
                        continue;
                    }

                    int assignedDay = now.getDayOfMonth();
                    if (now.getHour() > assignedHour) {
                        assignedDay++;
                    }

                    LocalDateTime time = LocalDateTime.of(
                            now.getYear(), now.getMonth(), assignedDay, assignedHour, 0);
                    scheduleMap.put(time, kothName);
                    System.out.println("Assigning " + kothName + " for " + time.toString());
                }
            }

            /*try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(plugin.getDataFolder(), FILE_NAME)), StandardCharsets.UTF_8))) {
                String currentLine;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (currentLine.startsWith("#")) {
                        continue;
                    }

                    String[] args = currentLine.split(":");
                    if (args.length == 2) {
                        try {
                            this.scheduleMap.put(LocalDateTime.parse(args[0], DATE_TIME_FORMATTER), args[1]);
                        } catch (DateTimeParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Bukkit.getConsoleSender().sendMessage("Could not find file " + FILE_NAME + '.');
            } catch (IOException ex) {
                ex.printStackTrace();
            }*/
        }
    }

    @Override
    public Map<LocalDateTime, String> getScheduleMap() {
        /*long millis = System.currentTimeMillis();
        if (millis - QUERY_DELAY > lastQuery) {
            this.reloadSchedules();
            this.lastQuery = millis;
        }*/

        return scheduleMap;
    }
}
