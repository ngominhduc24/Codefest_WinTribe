import io.socket.emitter.Emitter;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.algorithm.BaseAlgorithm;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Player1 {

    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER1_ID = "player1-xxx";
    final static String PLAYER2_ID = "player2-xxx";
    final static String GAME_ID = "914ddd01-7f81-424f-b134-8ba4f10a8741";
    final static Hero Player1 = new Hero(PLAYER1_ID, GAME_ID);
    private static MapInfo map;
    private static List<Position> restrictPosition;



    public static void main(String[] args) {
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            map = gameInfo.getMapInfo();
            map.updateMapInfo();
            restrictPosition = getRestrictPosition();
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
        if(map.getVirus().size() != 0)
        if(checkViruss()) {
            return dodgeViruss();
        }
        if(checkBomb() || !map.getBombs().isEmpty()) {
            return dodgeBomb();
        }
        else
        {
            target = getTargetBalk();
            if(target != null && map.getBombs().isEmpty()) {
                path = sortPath(playerPosition,target);
                min = countOfWalk(path);
                path = path.substring(0, path.length()-1) + "b" + dodgeBomb();
                System.out.println(path);
            }
            target = getTargetSpoils();
            if(target != null) {
                if( (countOfWalk(playerPosition, target) < min )|| min == -1) { // xoa
                    path = sortPath(playerPosition,target);
                    min = countOfWalk(playerPosition, target);
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
        restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        restrictPosition.add(map.getPlayerByKey(PLAYER2_ID).currentPosition);
        restrictPosition.addAll(map.getBombList());
        restrictPosition.addAll(getVirussList());
        return restrictPosition;
    }

    public static List<Position> getRestrictPosition4Blank() {
        List<Position> restrictPosition = new ArrayList<>();
        restrictPosition.addAll(map.balk);
        restrictPosition.addAll(map.walls);
        restrictPosition.addAll(map.teleportGate);
        restrictPosition.add(map.getPlayerByKey(PLAYER2_ID).currentPosition);
        restrictPosition.addAll(map.getBombList());
        restrictPosition.addAll(getVirussList());
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
        double min = 1000;
        if (!map.spoils.isEmpty()) {
            for(Position item:map.spoils)
            {
                if(countOfWalk(playerPosition, item) < min
                && countOfWalk(playerPosition, item) !=  0) {
                    target = item;
                    min = countOfWalk(playerPosition, target);
                }
            }
        }
        return target;
    }

    public  static Position getTargetHumans() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
         //get default target (it is the first human are not infected)
        for (Human human : map.getHuman()) {
            if(!human.infected && countOfWalk(playerPosition,human.position) != 0) {
                target = human.position;
                break;
            }
            if(human.infected
                    && map.getPlayerByKey(PLAYER1_ID).pill > 1
                    && countOfWalk(playerPosition, human.position) != 0) {
                target = human.position;
                break;
            }
        }

        if(target == null) return null;
        double min = 1000;
        // get human shortest with player (if player have pill human can be enfected)
        if (map.getPlayerByKey(PLAYER1_ID).pill > 1) {
            for (Human human : map.getHuman()) {
                if (human.infected
                        && countOfWalk(playerPosition, human.position) < min
                        && countOfWalk(playerPosition, human.position) !=  0)
                    target = human.position;
                min = countOfWalk(playerPosition, target);
            }
        }
        else  {
            for (Human human : map.getHuman()) {
                if (!human.infected
                        && countOfWalk(playerPosition, human.position) < min
                        && countOfWalk(playerPosition, human.position) !=  0 )
                    target = human.position;
                    min = countOfWalk(playerPosition, target);
            }
        }
        if(target != null) System.out.println(countOfWalk(playerPosition, target));
        else System.out.println("null");
        return target;
    }

    public static Position getTargetBalk() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        Position target = null;
        double min = 1000;
        if (map.balk.size() > 0) {
            for(Position item:map.balk )
            {
                if((AStarSearch.aStarSearch(map.mapMatrix, getRestrictPosition4Blank(), playerPosition, item).length() < min )
                        && countOfWalk(playerPosition, item) !=  0) {
                    target = item;
                    min = AStarSearch.aStarSearch(map.mapMatrix, getRestrictPosition4Blank(), playerPosition, item).length();
                }
            }
        }
        return target;
    }
    public static boolean checkBlank(Position p, List<Position> targetlist) {
        for(Position i:targetlist) {
            if(p.getRow() == i.getRow() && p.getCol() == i.getCol()) {
                return false;
            }
        }
        return true;
    }


    //**************************************** dodge bomb ************************************

    public static boolean checkBomb() {
        List<Position> bomb = map.getBombList();
        Position position = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        for(Position item:bomb) {
            if(position.getRow() == item.getRow() && position.getCol() == item.getCol()) {
                return true;
            }
        }
        return false;
    }

    public static String dodgeBomb() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        String path = "";
        restrictPosition.addAll(map.getBombs());
        Position target = null;
        double min = Double.MAX_VALUE;
        for(int i = 0; i <  map.blank.size(); i++)
        {
            if(checkBlank(map.blank.get(i),map.getBombList()))
                if( countOfWalk(playerPosition,map.blank.get(i)) < min && countOfWalk(playerPosition,map.blank.get(i)) != 0)
                {
                    target = map.blank.get(i);
                    min = countOfWalk(playerPosition, map.blank.get(i));
                }
        }
        if(target != null)
        path = sortPath(playerPosition, target);

        return path;
    }
    //**************************************** dodge viruss ************************************

    public static List<Position> getVirussList() {
        List<Position> output = new ArrayList();
        for(Viruses item:map.getVirus())
        {
            output.add(item.position);
            output.add(item.position.nextPosition(item.direction,1));
            Position temp = item.position;
            output.add(temp);
            output.add(temp.nextPosition(1,2));
            output.add(temp.nextPosition(2,2));
            output.add(temp.nextPosition(3,2));
            output.add(temp.nextPosition(4,2));
        }
        return output;
    }

    public static boolean checkViruss() {
        List<Position> viruss = getVirussList();
        Position position = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        for(Position item:viruss) {
            if(position.getRow() == item.getRow() && position.getCol() == item.getCol()) {
                return true;
            }
        }
        return false;
    }

    public static String dodgeViruss() {
        Position playerPosition = map.getPlayerByKey(PLAYER1_ID).currentPosition;
        String path = "";
        Position target = null;
        double min = Double.MAX_VALUE;
        for(Position item:map.blank)
        {
            if(checkBlank(item, getVirussList()))
                if( countOfWalk(playerPosition,item) < min && countOfWalk(playerPosition,item) != 0)
                {
                    target = item;
                    min = countOfWalk(playerPosition, item);
                }
        }
        if(target != null)
        path = sortPath(playerPosition, target);

        return path;
    }
}