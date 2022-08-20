import io.socket.emitter.Emitter;
import jsclub.codefest.bot.constant.GameConfig;
import jsclub.codefest.sdk.algorithm.AStarSearch;
import jsclub.codefest.sdk.algorithm.BaseAlgorithm;
import jsclub.codefest.sdk.model.Hero;
import jsclub.codefest.sdk.socket.data.*;
import jsclub.codefest.sdk.util.GameUtil;

import java.lang.reflect.Array;
import java.util.*;

public class Player2 {
    final static String SERVER_URL = "https://codefest.jsclub.me/";
    final static String PLAYER_ID = "player1-xxx";
    final static String COMPETITOR_ID = "player2-xxx";
    final static String GAME_ID = "44d7a70d-2406-46a8-a1ba-ec6423e97114";
    private static MapInfo map;
    private static int[][] roadMatrix; // ma tran ban do
    private static int[][] virusMatrix; // ma tran virus
    private static int[][] humanMatrix; // ma tran nguoi
    private static int[][] spoilsMatrix; // ma tran vat pham
    private static int[][] bombMatrix; // ma tran bom
    private static int[][] redMatrix;
    private static int[][] blackMatrix;
    private static int[][] pathMatrix;
    private static ArrayList<Position> blackList;
    private static Position heroPosition;


    public static double countOfWalk(Position start , Position des) {
        return sortPath(start , des).length();
    }
    public static double countOfWalk(String path) {
        return path.length();
    }
    public static String sortPath(Position start , Position des){
        return AStarSearch.aStarSearch(map.mapMatrix,blackList, start,des);
    }

    public static String getRandomPath(int length) {
        Random rand = new Random();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random_integer = rand.nextInt(5);
            sb.append("1234b".charAt(random_integer));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Hero randomPlayer = new Hero(PLAYER_ID, GAME_ID);
        Emitter.Listener onTickTackListener = objects -> {
            GameInfo gameInfo = GameUtil.getGameInfo(objects);
            map = gameInfo.getMapInfo();
            map.updateMapInfo();
            updateMatrixInfo();
            randomPlayer.move(getPath());
        };
        randomPlayer.setOnTickTackListener(onTickTackListener);
        randomPlayer.connectToServer(SERVER_URL);
    }

    public static String getPath(){
        String path = "";
        if(checkRedDanger(heroPosition)){
            System.out.println("Nguy hiem");
            path = runTo();
        }else{
            path = getTarget();
        }
        return  path;
    }

    //9 la vung cua hero
    //0 la vung di dcj xanh chua kham pha
    //1 la vung da kham pha
    //2 la vung den nguy hiem tinh ko di dcj
    //3 vung chua spoils
    //4 la vung chua nguy hiem dong
    //5 thung pha dcj
    public static String getTarget(){
        String path = "";
        pathMatrix = createZeroDiArray();
        pathMatrix[heroPosition.getRow()][heroPosition.getCol()] = 9;
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                if(spoilsMatrix[i][j] == 1 || humanMatrix[i][j] == 1){
                    pathMatrix[i][j] = 3;
                }
                if(blackMatrix[i][j] == 1 || roadMatrix[i][j] != 0){
                    pathMatrix[i][j] = 2;
                }if(roadMatrix[i][j] == 2 && bombMatrix[i][j] == 0){
                    pathMatrix[i][j] = 5;
                }
                if(map.getPlayerByKey(PLAYER_ID).pill >= 3){
                    if(virusMatrix[i][j] == 1 || humanMatrix[i][j] == 2){
                        pathMatrix[i][j] = 4;
                    }
                }
            }
        }
        Position p;
        Position t;
        Queue<Position> list = new ArrayDeque<Position>();
        list.add(heroPosition);
        Position box = null;
        while(!list.isEmpty()){
            p = list.poll();
            for( int i = 1; i <=4 ; i++){
                t = p.nextPosition(i,1);
                if(pathMatrix[t.getRow()][t.getCol()] == 0){
                    pathMatrix[t.getRow()][t.getCol()] = 1;
                    list.add(t);
                }else if(pathMatrix[t.getRow()][t.getCol()] == 3 || pathMatrix[t.getRow()][t.getCol()] == 4){
                    return sortPath(heroPosition,t);
                }else if(box == null && pathMatrix[t.getRow()][t.getCol()] == 5){
                    box = t;
                }
            }
        }
        if(path == "" && !isBomDrop()){
            path = sortPath(heroPosition,box);
            path = path.substring(0,path.length()-1) + 'b' + runTo();

        }
        return path;
    }

    public static boolean isBomDrop(){
        for(Bomb bomb:map.getBombs()){
            if(bomb.playerId.equals(PLAYER_ID)){
                return true;
            }
        }
        return false;
    }

    public static String runTo(){
        String path = "";
        pathMatrix = createZeroDiArray();
        pathMatrix[heroPosition.getRow()][heroPosition.getCol()] = 9;
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                if(redMatrix[i][j] == 1){
                    pathMatrix[i][j] = 1;
                }
                if(blackMatrix[i][j] == 1 || roadMatrix[i][j] != 0){
                    pathMatrix[i][j] = 2;
                }
            }
        }
        Position p;
        Position t;
        Queue<Position> list = new ArrayDeque<Position>();
        list.add(heroPosition);
        while(!list.isEmpty()){
            p = list.poll();
            for( int i = 1; i <=4 ; i++){
                t = p.nextPosition(i,1);
                if(pathMatrix[t.getRow()][t.getCol()] == 0){
                    return path = sortPath(heroPosition,t);
                }
            }
        }
        return path;
    }

    public static boolean checkRedDanger(Position p){
        if(redMatrix[p.getRow()][p.getCol()] == 1){
            return true;
        }
        return false;
    }

    public static boolean checkBlackDanger(Position p){
        if(blackMatrix[p.getRow()][p.getCol()] == 1){
            return true;
        }
        return false;
    }

    public static boolean checkRedAndBlackDanger(Position p){
        return checkRedDanger(p) && checkBlackDanger(p);
    }

    //tao 1 mang luu chu chuyen dong tim duong dao thoat
    //9 la vung cua hero
    //0 la vung di dcj xanh
    //1 la vung do nguy co
    //2 la vunf den nguy hiem


    public static void updateMatrixInfo(){
        roadMatrix = map.mapMatrix;
        spoilsMatrix = createSpoilsMatrix();
        humanMatrix = createHumanMatrix();
        virusMatrix = createVirusMatrix();
        bombMatrix = createBombMatrix();
        redMatrix = createRedMatrix();
        blackMatrix = createBlackMatrix();
        heroPosition = getHeroCurrentPosition();
        blackList = createBlackList();
    }

    //nhung noi nguy hiem co bom hoac vi rut chuan bi di qua
    // 1 la nguy hiem
    // 0 la an toan
    public static int[][] createRedMatrix(){
        int[][] redMatrix = createZeroDiArray();
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                if(bombMatrix[i][j] == 1 || virusMatrix[i][j] == 2 || humanMatrix[i][j] == 3){
                    redMatrix[i][j] = 1;
                }
            }
        }
        return redMatrix;
    }

    //nhung noi co nguoi nhiem virut hoac virut dang o can tranh khi di chuyen
    //1 la nguy hiem
    //0 la an toan
    public static int[][] createBlackMatrix(){
        int[][] blackMatrix = createZeroDiArray();
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                if(virusMatrix[i][j] == 1 || humanMatrix[i][j] == 2 || roadMatrix[i][j] != 0 ||
                        (map.getPlayerByKey(COMPETITOR_ID).currentPosition.getRow() == i && map.getPlayerByKey(COMPETITOR_ID).currentPosition.getCol() == j )){
                    blackMatrix[i][j] = 1;
                }
            }
        }
        return blackMatrix;
    }

    public static ArrayList<Position> createBlackList(){
        ArrayList<Position>  blackList = new ArrayList<Position>();
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                if(blackMatrix[i][j] == 1 || redMatrix[i][j] == 1){
                    blackList.add(new Position(j , i));
                }
            }
        }
        return  blackList;
    }


    // show test info
    //0 la duong di dcj
    //1 la tuong ko the pha
    //2 la tuong co the pha
    //6 la teleport gate
    //7 la khu cach li
    public static void showRoad(){
        int[][] mapMatrix = map.mapMatrix;
        System.out.println("\nRoad matrix : ");
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                System.out.print(mapMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void  showRoadMatrix(){
        int[][] mapMatrix = map.mapMatrix;
        System.out.println("\nRoad matrix : ");
        showDiMatrix(mapMatrix);
    }

    public static void showDiMatrix(int[][] matrix){
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static int[][] createZeroDiArray(){
        int[][] matrix = new int[map.size.rows][map.size.cols];
        for(int i = 0; i < map.size.rows;i++){
            for(int j = 0 ; j < map.size.cols;j++){
                matrix[i][j] = 0;
            }
        }
        return matrix;
    }

    public static Position getHeroCurrentPosition(){
        Position p = map.getPlayerByKey(PLAYER_ID).currentPosition;
        return p;
    }

    //0 la nhung loi ko co virus
    //1 la nhung noi co virus
    //2 la nhung noi virus chuan bi den
    public static int[][] createVirusMatrix(){
        int[][] virusMatrix = createZeroDiArray();
        for(Viruses virus: map.getVirus()){
            virusMatrix[virus.position.getRow()][virus.position.getCol()] = 1;
            if(virus.direction == 1){
                virusMatrix[virus.position.getRow()][virus.position.getCol() - 1] = 2;
            } else if (virus.direction == 2) {
                virusMatrix[virus.position.getRow()][virus.position.getCol() + 1] = 2;
            }else if (virus.direction == 3) {
                virusMatrix[virus.position.getRow() - 1][virus.position.getCol()] = 2;
            }else if (virus.direction == 4) {
                virusMatrix[virus.position.getRow() + 1][virus.position.getCol()] = 2;
            }
        }
        return virusMatrix;
    }

    //0 la nhung loi ko co human
    //1 la nhung noi co human khoe manh
    //2 la nhung noi co human bi nhiem
    //3 la nhung noi human nhiem co the se di
    public static int[][] createHumanMatrix(){
        int[][] humanMatrix = createZeroDiArray();
        for(Human human: map.getHuman()){
            if(human.infected){
                humanMatrix[human.position.getRow()][human.position.getCol()] = 2;
            }else {
                humanMatrix[human.position.getRow()][human.position.getCol()] = 1;
                if(human.direction == 1){
                    humanMatrix[human.position.getRow()][human.position.getCol() - 1] = 3;
                } else if (human.direction == 2) {
                    humanMatrix[human.position.getRow()][human.position.getCol() + 1] = 3;
                } else if (human.direction == 3) {
                    humanMatrix[human.position.getRow() - 1][human.position.getCol()] = 3;
                } else if (human.direction == 4) {
                    humanMatrix[human.position.getRow() + 1][human.position.getCol()] = 3;
                }
            }
        }
        return humanMatrix;
    }

    public static int[][] createSpoilsMatrix(){
        int[][] spoilsMatrix = createZeroDiArray();
        for(Spoil spoil:map.getSpoils()){
            spoilsMatrix[spoil.getRow()][spoil.getCol()] = 1;
        }
        return spoilsMatrix;
    }

    //nhung noi co bom la 1
    // ko co bom la 0
    public static int[][] createBombMatrix() {
        int[][] bombMatrix = createZeroDiArray();
        Iterator var3 = map.getBombs().iterator();
        while(var3.hasNext()) {
            Bomb bomb = (Bomb) var3.next();
            if(bomb.remainTime <= 500){
                bombMatrix[bomb.getRow()][bomb.getCol()] = 1;
                Player player = map.getPlayerByKey(bomb.playerId);
                for (int d = 1; d < 5; ++d) {
                    for (int p = 1; p <= player.power; ++p) {
                        Position effBomb = bomb.nextPosition(d, p);
                        bombMatrix[effBomb.getRow()][effBomb.getCol()] = 1;
                    }
                }
            }
        }
        return bombMatrix;
    }

}