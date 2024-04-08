import java.util.*;
import java.io.*;

public class Main {

    static int boardSize, friendsNum; 
    static int board[][];
    static int orders[];
    static String[] favFriends;

    static int[] dx = {-1, 0, 0, 1};
    static int[] dy = {0, -1, 1, 0};

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // 초기화
        boardSize = Integer.parseInt(br.readLine());
        board = new int[boardSize][boardSize];
        friendsNum = boardSize * boardSize;
        orders = new int[friendsNum];
        favFriends = new String[friendsNum + 1];

        // 입력 받기
        for(int i = 0; i < friendsNum; i++) {
            String input = br.readLine().trim();
            int meIdx = input.charAt(0) - '0';
            orders[i] = meIdx;
            favFriends[meIdx] = input.substring(1);
        } 
        
        simulation();
    }
    
    static void simulation() {
        // 자리 정하기
        for(int idx : orders) {
            // 앉을 자리 찾기
            int[] seat = findSeat(idx);
            board[seat[0]][seat[1]] = idx;
        }

        // 점수 계산
        int score = calculateScore();
        System.out.println(score);
    }

    static int[] findSeat(int me) {
        int favFrNum = 0;
        int blankCnt = 0;
        int row = boardSize - 1;
        int col = boardSize - 1;

        for(int i = 0; i < boardSize; i++) {
            for(int j =0; j < boardSize; j++) {

                if (board[i][j]!=0) {
                    continue;
                }

                int num = 0;
                int cnt = 0;

                for(int dir = 0; dir < 4; dir++) {
                    int nx = i + dx[dir];
                    int ny = j + dy[dir];

                    if (nx < 0 || nx >= boardSize || ny <0 || ny>=boardSize) {
                        continue;
                    }
                    // 비어있는 칸 체크
                    if (board[nx][ny]==0) {
                        cnt++;
                    }
                    // 좋아하는 친구 체크
                    if (favFriends[me].contains(board[nx][ny]+"")) {
                        num++;
                    }
                }
                
                if (num > favFrNum) {
                    favFrNum = num;
                    row = i;
                    col = j;
                } 
                if (num == favFrNum && cnt > blankCnt) {
                    blankCnt = cnt;
                    row = i;
                    col = j;
                }
            }
        }
        return new int[]{row, col};
    }

    static int calculateScore() {
        int score = 0;
        for(int i = 0; i < boardSize; i++) {
            for(int j =0; j < boardSize; j++) {
                int me = board[i][j];
                int favCnt = 0;

                for(int dir = 0; dir < 4; dir++) {
                    int nx = i + dx[dir];
                    int ny = j + dy[dir];

                    if (nx < 0 || nx >= boardSize || ny <0 || ny>=boardSize) {
                        continue;
                    }
                    
                    // 좋아하는 친구 체크
                    if (favFriends[me].contains(board[nx][ny]+"")) {
                        favCnt++;
                    }
                }
                if (favCnt == 0) {
                    continue;
                }
                score += Math.pow(10, favCnt - 1);
            }
        }
        return score;
    }
}