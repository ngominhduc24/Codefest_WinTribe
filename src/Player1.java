import io.socket.emitter.Emitter;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.algorithm.BaseAlgorithm;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Player1 {

    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player1-xxx";
    final static String GAME_ID = "7d764cf0-1c1a-48cd-85ce-4ecf4ed5475e";
    final static Hero Player1 = new Hero(PLAYER_ID, GAME_ID);

    public static void main(String[] args) {
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo map = gameInfo.getMapInfo();
            map.updateMapInfo();

            List<Position> restrictPosition = getRestrictPosition(map);
            String path = AStarSearch.aStarSearch(map.mapMatrix,restrictPosition, map.getCurrentPosition(Player1),getTarget(map));
            Player1.move(path);
        };
        Player1.setOnTickTackListener(onTickTackListener);
        Player1.connectToServer(SERVER_URL);
    }

    public static List<Position> getRestrictPosition(MapInfo map) {
        List<Position> restrictPosition = new ArrayList<>();
//        restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        restrictPosition.addAll(getBomb(map));
        List<Position> virus = new ArrayList<>();
        for(Viruses vr:map.getVirus()) {
            virus.add(vr.position);
        }
        restrictPosition.addAll(virus);
        return restrictPosition;
    }

    public static Position getTarget(MapInfo map) {
        Position playerPosition = map.getPlayerByKey("player1-xxx").currentPosition;
        Position target = playerPosition;
        if (map.spoils.size() != 0) {
            target = map.spoils.get(0);
            for(int i = 1; i <  map.spoils.size(); i++)
            {
                if(BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) < 4 && BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) != 0) {
                if(BaseAlgorithm.distanceBetweenTwoPoints(map.spoils.get(i), playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) )
                target = map.spoils.get(i);
                    return target;
                }
            }
        }

        if (map.getPlayerByKey("player1-xxx").pill > 0) {
            for(int i = 1; i <  map.getHuman().size(); i++)
            {
                if(BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) < 4 && BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) != 0) {
                if(map.getHuman().get(i).infected && BaseAlgorithm.distanceBetweenTwoPoints(map.getHuman().get(i).position, playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) )
                    target = map.getHuman().get(i).position;
                    return target;
                }
            }
        } else {
            for(int i = 1; i <  map.getHuman().size(); i++)
            {
                if(BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) < 4 && BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) != 0)
                if(!map.getHuman().get(i).infected && BaseAlgorithm.distanceBetweenTwoPoints(map.getHuman().get(i).position, playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) )
                    target = map.getHuman().get(i).position;
                    return target;
            }
        }
        System.out.println(BaseAlgorithm.distanceBetweenTwoPoints(target,playerPosition));
        return target;
    }

    public static List<Position> getBomb(MapInfo map) {
        List<Position> bomb = new ArrayList<>();
        for(Bomb item:map.bombs) {
            for(int i = 0; i <= 4; i++) {
                bomb.add(item.nextPosition(i,1));
            }
        }
        return bomb;
    }
}
