package loftily.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import loftily.gui.menu.mainmenu.MainMenu;
import loftily.utils.client.ClientUtils;
import loftily.utils.player.PlayerUtils;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServerUtils implements ClientUtils {
    public static void connectToLastServer() {
        if (mc.prevServerData != null) {
            mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new MainMenu()), mc, mc.prevServerData));
        }
    }
    
    public static String getServerIp() {
        String serverIp = null;
        
        if (mc.isIntegratedServerRunning()) {
            serverIp = "SinglePlayer";
        } else if (mc.world != null && mc.world.isRemote) {
            ServerData serverData = mc.getCurrentServerData();
            if (serverData != null) {
                serverIp = serverData.serverIP;
            }
        }
        return serverIp;
    }

    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        if (mc.world == null) {
            return lines;
        } else {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) {
                return lines;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return lines;
                } else {
                    Collection<Score> scores = scoreboard.getSortedScores(objective);
                    List<Score> list = new ArrayList<>();

                    for (Score input : scores) {
                        if (input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")) {
                            list.add(input);
                        }
                    }

                    if (list.size() > 15) {
                        scores = new ArrayList<>(Lists.newArrayList(Iterables.skip(list, list.size() - 15)));
                    } else {
                        scores = list;
                    }

                    int index = 0;

                    for (Score score : scores) {
                        index++;
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                        lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
                        if (index == scores.size()) {
                            lines.add(objective.getDisplayName());
                        }
                    }

                    Collections.reverse(lines);
                    return lines;
                }
            }
        }
    }

    public static String getServerPing() {
        if (mc.player == null || mc.getConnection().getPlayerInfo(mc.player.getUniqueID()) == null || mc.isIntegratedServerRunning())
            return "0 ms";
        
        if (mc.world != null && mc.world.isRemote) {
            return mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getResponseTime() + " ms";
        }
        return "";
    }

    public static String stripString(String s) {
        char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        StringBuilder validated = new StringBuilder();

        for (char c : nonValidatedString) {
            if (c < 127 && c > 20) {
                validated.append(c);
            }
        }

        return validated.toString();
    }

    public static int getLobbyStatus() {
        if (!PlayerUtils.nullCheck()) {
            return -1;
        } else {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) {
                return -1;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return -1;
                } else {
                    for (String line : getSidebarLines()) {
                        line = stripString(line);
                        String[] parts = line.split("  ");
                        if (parts.length > 1 && parts[1].startsWith("L")) {
                            return 1;
                        }
                    }

                    return -1;
                }
            }
        }
    }

    public static int hypixelStatus() {
        if (!PlayerUtils.nullCheck()) {
            return -1;
        } else {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) {
                return -2;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return -1;
                } else {
                    for (String line : getSidebarLines()) {
                        line = stripString(line);
                        if (line.startsWith("0") || line.startsWith("1")) {
                            return 1;
                        }
                    }

                    return -1;
                }
            }
        }
    }

    public static boolean isReplay() {
        if (isHypixel()) {
            if (!PlayerUtils.nullCheck()) {
                return false;
            } else {
                Scoreboard scoreboard = mc.world.getScoreboard();
                if (scoreboard == null) {
                    return false;
                } else {
                    ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                    return objective != null && stripString(objective.getDisplayName()).contains("REPLAY");
                }
            }
        } else {
            return false;
        }
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null && (mc.getCurrentServerData().serverIP.contains("hypixel.net") || mc.getCurrentServerData().serverIP.contains("nyap.buzz"));
    }

    public static boolean spectatorCheck() {
        return !mc.player.inventory.getStackInSlot(8).isEmptyStack() && mc.player.inventory.getStackInSlot(8).getDisplayName().contains("Return")
                || stripString(mc.ingameGUI.displayedTitle).contains("YOU DIED");
    }
}
