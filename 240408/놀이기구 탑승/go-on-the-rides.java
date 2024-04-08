import java.util.*;
import java.io.*;

public class Main {

    static int boardSize, friendsNum;
    static int[][] board;
    static int[] orders;
    static HashSet<Integer>[] favFriends;

    static int[] dx = {-1, 0, 0, 1};
    static int[] dy = {0, -1, 1, 0};

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        boardSize = Integer.parseInt(br.readLine());
        board = new int[boardSize][boardSize];
        friendsNum = boardSize * boardSize;
        orders = new int[friendsNum];
        favFriends = new HashSet[friendsNum + 1];

        for(int i = 0; i < favFriends.length; i++) {
            favFriends[i] = new HashSet<>();
        }

        // 입력 받기
        for(int i = 0; i < friendsNum; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            int meIdx = Integer.parseInt(st.nextToken());
            orders[i] = meIdx;
            while(st.hasMoreTokens()) {
                favFriends[meIdx].add(Integer.parseInt(st.nextToken()));
            }
        } 

        simulation();
    }

    static void simulation() {
        for(int idx : orders) {
            int[] seat = findSeat(idx);
            board[seat[0]][seat[1]] = idx;
        }

        int score = calculateScore();
        System.out.println(score);
    }

    static int[] findSeat(int me) {
        int maxFavFrNum = -1;
        int maxBlankCnt = -1;
        int bestRow = -1;
        int bestCol = -1;

        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                if (board[i][j] != 0) continue;

                int favFrNum = 0;
                int blankCnt = 0;

                for(int dir = 0; dir < 4; dir++) {
                    int nx = i + dx[dir];
                    int ny = j + dy[dir];

                    if (nx < 0 || nx >= boardSize || ny < 0 || ny >= boardSize) continue;

                    if (board[nx][ny] == 0) {
                        blankCnt++;
                    } else if (favFriends[me].contains(board[nx][ny])) {
                        favFrNum++;
                    }
                }

                if (favFrNum > maxFavFrNum || (favFrNum == maxFavFrNum && blankCnt > maxBlankCnt)) {
                    maxFavFrNum = favFrNum;
                    maxBlankCnt = blankCnt;
                    bestRow = i;
                    bestCol = j;
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }

    static int calculateScore() {
        int score = 0;
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                int me = board[i][j];
                if (me == 0) continue;

                int favCnt = 0;

                for(int dir = 0; dir < 4; dir++) {
                    int nx = i + dx[dir];
                    int ny = j + dy[dir];

                    if (nx < 0 || nx >= boardSize || ny < 0 || ny >= boardSize) continue;

                    if (favFriends[me].contains(board[nx][ny])) {
                        favCnt++;
                    }
                }

                if (favCnt > 0) {
                    score += Math.pow(10, favCnt - 1);
                }
            }
        }
        return score;
    }
}