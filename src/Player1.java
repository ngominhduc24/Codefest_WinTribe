import io.socket.emitter.Emitter;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player1 {

    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player1-xxx";
    final static String GAME_ID = "67615ab6-820e-4464-94bc-af63eec35a07";
    final static Hero Player1 = new Hero(PLAYER_ID, GAME_ID);

    public static void main(String[] args) {
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo map = gameInfo.getMapInfo();
            map.updateMapInfo();

            List<Position> restrictPosition = getRestrictPosition(map);
            String path = getPath(map, restrictPosition);
            Player1.move(path);
        };

        Player1.setOnTickTackListener(onTickTackListener);
        Player1.connectToServer(SERVER_URL);
    }

    public static List<Position> getRestrictPosition(MapInfo map) {
        List<Position> restrictPosition = new ArrayList<>();
        //restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        List<Position> virus = new ArrayList<>();
        for(Viruses vr:map.getVirus()) {
            virus.add(vr.position);
        }
        restrictPosition.addAll(virus);
        return restrictPosition;
    }

    public static String getPath(MapInfo map, List<Position> restrict) {
        if (map.spoils.size() != 0) {
            Position target = map.spoils.get(0);
            if (target != null) {
                return AStarSearch.aStarSearch(map.mapMatrix, restrict, map.getCurrentPosition(Player1), target);
            }
        }

        if (map.getHuman().size() != 0) {
            Position target = null;
            if (map.getPlayerByKey("player1-xxx").pill > 0) {
                target = map.getHuman().get(0).position;
            } else {
                for (Human human : map.getHuman()) {
                    if (!human.infected) {
                        target = human.position;
                        break;
                    }
                }
            }
            if (target != null) {
                return AStarSearch.aStarSearch(map.mapMatrix, restrict, map.getCurrentPosition(Player1), target);
            }
        }
            return "";
    }
}
