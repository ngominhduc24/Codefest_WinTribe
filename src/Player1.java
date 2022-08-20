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
    final static String GAME_ID = "e04cef22-dc59-4699-948b-f81def74adc5";
    final static Hero Player1 = new Hero(PLAYER1_ID, GAME_ID);
    private static MapInfo map;
    private static List<Position> restrictPosition;



    public static void main(String[] args) {
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            map = gameInfo.getMapInfo();
            map.updateMapInfo();
            restrictPosition = getRestrictPosition(); // lay
            String path = getPath();
            Player1.move(path);
        };
        Player1.setOnTickTackListener(onTickTackListener);
        Player1.connectToServer(SERVER_URL);
    }

    public static String getPath() {
        String path = "";
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition; // lay vi tri hien tai 

        Position target = null;
        double min = -1;
        if(checkBomb()) {
            path = dodgeBomb();
        }
        else
        {
            target = getTargetBalk();
            if(target != null) {
                path = sortPath(playerPosition,target);
                min = countOfWalk(path);
                if(path.length() > 1)
                    path = path.substring(0, path.length()-2) + "b";
            }
            target = getTargetSpoils();
            if(target != null) {
                if( (countOfWalk(playerPosition, target) < min )|| min == -1) {
                    path = sortPath(playerPosition,target);
                }
            }
            target = getTargetHumans();
            if(target != null )
            {
                if( (countOfWalk(playerPosition, target) < min )|| min == -1) {
                    path = sortPath(playerPosition,target);
                }
            }

        }
        return path;
    }


    // *****************************   new function ************************************

    public static double countOfWalk(Position start , Position des) {
        return sortPath(start , des).length();
    }
    public static double countOfWalk(String path) {
        return path.length();
    }
    public static String sortPath(Position start , Position des){
        return AStarSearch.aStarSearch(map.mapMatrix,restrictPosition, start,des);
    }

    //******************************************* take map info *******************************
    
    // nhung diem can tranh
    public static List<Position> getRestrictPosition() {
        List<Position> restrictPosition = new ArrayList<>();
//        restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        restrictPosition.add(map.getPlayerByKey(PLAYER2_ID).currentPosition);
//        restrictPosition.addAll(map.getBombList());
        List<Position> virus = new ArrayList<>();
        for(Viruses vr:map.getVirus()) {
            virus.add(vr.position);
        }
        restrictPosition.addAll(virus);
        return restrictPosition;
    }

    public static List<Position> getBomb() {
        List<Position> bomb = new ArrayList<>();
//        List<Position> Bombs = new ArrayList<>();
//        Bombs.addAll(map.bombs);
        for(Position item:map.bombs) {
            bomb.add(item);
            for(int i = 0; i <= 4; i++) {
                for(int step = 1; step <= 2; step++){
                    bomb.add(item.nextPosition(i,step));
                }
            }
        }
        return bomb;
    }

    //*********************************** get target *******************************************
    
    public  static Position getTargetSpoils() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        if (map.spoils.size() > 0) {
            target = map.spoils.get(0);
            for(int i = 1; i <  map.spoils.size(); i++)
            {
                if((countOfWalk(playerPosition, map.spoils.get(i)) < countOfWalk(playerPosition, target) )
                    && countOfWalk(playerPosition, map.spoils.get(i)) !=  0)
                    target = map.spoils.get(i);
            }
        }
        return target;
    }

    public  static Position getTargetHumans() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        // get default target (it is the first human are not infected)
        for (int i = 0; i < map.getHuman().size(); i++) {
            if(!map.getHuman().get(i).infected
                && countOfWalk(playerPosition, map.getHuman().get(i).position) != 0) {
                target = map.getHuman().get(i).position;
                break;
            }
            if(map.getHuman().get(i).infected
                && map.getPlayerByKey(PLAYER1_ID).pill > 1
                && countOfWalk(playerPosition, map.getHuman().get(i).position) != 0) {
                target = map.getHuman().get(i).position;
                break;
            }
        }

        if(target == null) return null;
        // get human shortest with player (if player have pill human can be enfected)
        if (map.getPlayerByKey(PLAYER1_ID).pill > 1) {

            for (int i = 0; i < map.getHuman().size(); i++) {
                if (map.getHuman().get(i).infected
                    && countOfWalk(playerPosition, map.getHuman().get(i).position) < countOfWalk(playerPosition, target)
                    && countOfWalk(playerPosition, map.getHuman().get(i).position) !=  0 )
                    target = map.getHuman().get(i).position;
            }
        } else  {
            for (int i = 0; i < map.getHuman().size(); i++) {
                if (!map.getHuman().get(i).infected
                    && countOfWalk(playerPosition, map.getHuman().get(i).position) < countOfWalk(playerPosition, target)
                    && countOfWalk(playerPosition, map.getHuman().get(i).position) !=  0 )
                    target = map.getHuman().get(i).position;
            }
        }
        return target;
    }

    public static Position getTargetBalk() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        if (map.balk.size() > 0) {
            target = map.balk.get(0);
            for(int i = 1; i <  map.balk.size(); i++)
            {
                if((countOfWalk(playerPosition, map.balk.get(i)) < countOfWalk(playerPosition, target) )
                        && countOfWalk(playerPosition, map.balk.get(i)) !=  0)
                    target = map.balk.get(i);
            }
        }
        return target;
    }

    //**************************************** dodge ************************************

    public static boolean checkBomb() {
        List<Position> bomb = map.getBombList();
        Position position = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        for(Position item:bomb) {
            if(position.getRow() == item.getRow() && position.getCol() == item.getCol()) {
                System.out.println("asd");
                return true;
            }
        }
        return false;
    }



    public static boolean checkBlank(Position p) {
        for(Position i:map.getBombList()) {
            if(p.getRow() == i.getRow() && p.getCol() == i.getCol()) {
                return false;
            }
        }
        return true;
    }

    public static String dodgeBomb() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        String path = "";
        restrictPosition.addAll(map.getBombs());
        Position target = null;
        double min = Double.MAX_VALUE;
        for(int i = 0; i <  map.blank.size(); i++)
        {
            if(checkBlank(map.blank.get(i)))
            if( countOfWalk(playerPosition,map.blank.get(i)) < min && countOfWalk(playerPosition,map.blank.get(i)) != 0)
            {
                target = map.blank.get(i);
                min = countOfWalk(playerPosition, map.blank.get(i));
            }
        }
        path = sortPath(playerPosition, target);

        return path;
    }

    

    
}
