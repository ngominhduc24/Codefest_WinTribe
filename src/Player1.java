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
    final static String PLAYER1_ID = "player1-xxx";
    final static String PLAYER2_ID = "player2-xxx";
    final static String GAME_ID = "678da3e4-4ea6-4574-808b-606b7f4610fa";
    final static Hero Player1 = new Hero(PLAYER1_ID, GAME_ID);




    public static void main(String[] args) {
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo map = gameInfo.getMapInfo();
            map.updateMapInfo();
            String path = getPath(map);
            Player1.move(path);
        };
        Player1.setOnTickTackListener(onTickTackListener);
        Player1.connectToServer(SERVER_URL);
    }

    public static List<Position> getRestrictPosition(MapInfo map) {
        List<Position> restrictPosition = new ArrayList<>();
        restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        restrictPosition.add(map.getPlayerByKey(PLAYER2_ID).currentPosition);
        restrictPosition.addAll(getBomb(map));
        List<Position> virus = new ArrayList<>();
        for(Viruses vr:map.getVirus()) {
            virus.add(vr.position);
        }
        restrictPosition.addAll(virus);
        return restrictPosition;
    }

    public  static Position getTargetSpoils(MapInfo map) {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        List<Position> restrictPosition = getRestrictPosition(map);
        if (map.spoils.size() != 0) {
            target = map.spoils.get(0);
            for(int i = 1; i <  map.spoils.size(); i++)
            {
                if((BaseAlgorithm.distanceBetweenTwoPoints(map.spoils.get(i), playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) )
                    || AStarSearch.aStarSearch(map.mapMatrix,restrictPosition, map.getCurrentPosition(Player1),target) == ""  )
                    target = map.spoils.get(i);
            }
        }
        if(target == null ) System.out.println("asdasdsa");
        return target;
    }

    public static List<Position> getBomb(MapInfo map) {
        List<Position> bomb = new ArrayList<>();
        List<Position> Bombs = new ArrayList<>();
        Bombs.addAll(map.bombs);
        for(Position item:Bombs) {
            bomb.add(item);
            for(int i = 0; i <= 4; i++) {
                for(int step = 1; step <= 2; step++){
                    bomb.add(item.nextPosition(i,step));
                }
            }
        }
        return bomb;
    }

    public  static Position getTargetHumans(MapInfo map) {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        // get default target (it is the first human are not infected)
        for (int i = 0; i < map.getHuman().size(); i++) {
            if(!map.getHuman().get(i).infected) {
                target = map.getHuman().get(i).position;
                break;
            }
        }
        if(target == null) return null;
        // get human shortest with player (if player have pill human can be enfected)
        if (map.getPlayerByKey(PLAYER1_ID).pill > 0) {

            for (int i = 0; i < map.getHuman().size(); i++) {
                if (map.getHuman().get(i).infected && BaseAlgorithm.distanceBetweenTwoPoints(map.getHuman().get(i).position, playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition))
                    target = map.getHuman().get(i).position;
            }
        } else {
            for (int i = 0; i < map.getHuman().size(); i++) {
                if (!map.getHuman().get(i).infected && BaseAlgorithm.distanceBetweenTwoPoints(map.getHuman().get(i).position, playerPosition) < BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition))
                    target = map.getHuman().get(i).position;
            }
        }
            return target;
    }

    public static String dodgeBomb(MapInfo map) {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        String path = "";
        List<Position> restrictPosition = getRestrictPosition(map);
        restrictPosition.addAll(getBomb(map));
        Position target = map.blank.get(0);
        double min = BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition);
        for(int i = 1; i <  map.blank.size(); i++)
        {
            if(BaseAlgorithm.distanceBetweenTwoPoints(map.blank.get(i), playerPosition) < 3 && AStarSearch.aStarSearch(map.mapMatrix, restrictPosition, playerPosition, map.blank.get(i)).length() != 0) {
            //if(BaseAlgorithm.distanceBetweenTwoPoints(map.blank.get(i), playerPosition) < min && AStarSearch.aStarSearch(map.mapMatrix, restrictPosition, playerPosition, map.blank.get(i)).length() != 0) {
                target = map.blank.get(i);
                min = BaseAlgorithm.distanceBetweenTwoPoints(map.blank.get(i), playerPosition);
            }
        }
        path = AStarSearch.aStarSearch(map.mapMatrix, restrictPosition, playerPosition, target);

        return path;
    }

    public static boolean checkBomb(MapInfo map) {
        List<Position> bomb = getBomb(map);
        Position position = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        for(Position item:bomb) {
            if(position.getRow() == item.getRow() && position.getCol() == item.getCol()) {
                return true;
            }
        }
        return false;
    }

    public static String getPath(MapInfo map) {
        String path = "";
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        List<Position> restrictPosition = getRestrictPosition(map);
        Position target = null;
        double min = -1;
        if(checkBomb(map)) {
            path = dodgeBomb(map);
        }
        else
        {
            target = getTargetSpoils(map);
            if(target != null) {
                path = AStarSearch.aStarSearch(map.mapMatrix,restrictPosition, map.getCurrentPosition(Player1),target);
                min = BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition);
            }
            target = getTargetHumans(map);
            if(target != null )
            {
                if( (BaseAlgorithm.distanceBetweenTwoPoints(target, playerPosition) < min )|| min == -1) {
                    path = AStarSearch.aStarSearch(map.mapMatrix,restrictPosition, map.getCurrentPosition(Player1),target);
                }
            }

        }
        return path;
    }
}
