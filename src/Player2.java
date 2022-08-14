import io.socket.emitter.Emitter;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.GameInfo;
import jsclub.codefest.sdk.socket.data.MapInfo;
import jsclub.codefest.sdk.socket.data.Position;
import jsclub.codefest.sdk.util.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player2 {

    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player2-xxx";
    final static String GAME_ID = "0af82a5d-47e2-49e2-9056-6797866f212f";

    public static void main(String[] args) {
        Hero randomPlayer = new Hero(PLAYER_ID, GAME_ID);

        // player move
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            MapInfo map = gameInfo.getMapInfo();
            map.updateMapInfo();
            List<Position> restrictPosition = new ArrayList<Position>();
            restrictPosition.addAll(map.balk);
            restrictPosition.addAll(map.walls);

            String path = "";
            if(map.spoils.size() != 0) {
                Position target = map.spoils.get(0);
                if(target != null) {
                    path = AStarSearch.aStarSearch(map.mapMatrix, restrictPosition, map.getCurrentPosition(randomPlayer),target);
                }
                randomPlayer.move(path);
            }
            //randomPlayer.move(getRandomPath(20));
        };

        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(SERVER_URL);
    }
}
