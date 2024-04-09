import java.util.*;
import java.io.*;

public class Main {

    static int Size, TurnNum, SantaNum, RPower, SPower, failSantaNum;

    final static int RUDOLF_DIR = 8, SANTA_DIR=4;
    static int[] dx = {-1, 0, 1, 0, -1, -1, 1, 1};
    static int[] dy = {0, 1, 0, -1, -1, 1, -1, 1};
    
    static class Point {
        int x, y, dir;

        Point (int x, int y) {
            this.x = x;
            this.y = y;
            this.dir = 0;
        }
    }  

    static class Santa {
        int num;
        Point pos;
        int score;
        int sleepTime;
        boolean isAlive;

        Santa (int num, Point pos) {
            this.num = num;
            this.pos = pos;
            this.score = 0;
            this.sleepTime = 0;
            this.isAlive = true;
        }

        public String toString() {
            return "Santa " + num + " | " + pos.x + "," + pos.y + ", score:" + score + ", sleepTime:" + sleepTime + ", isAlive:" + isAlive;
        }
    }

    static int[][] santaBoard;
    static Santa[] santas;
    static Point rudolf;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
        StringTokenizer st = new StringTokenizer(br.readLine().trim());

        // Size, TurnNum, SantaNum, RPower, SPower 입력받기 
        Size = Integer.parseInt(st.nextToken());
        santaBoard = new int[Size+1][Size+1];
        TurnNum = Integer.parseInt(st.nextToken());
        SantaNum = Integer.parseInt(st.nextToken());
        RPower = Integer.parseInt(st.nextToken());
        SPower = Integer.parseInt(st.nextToken());   
        santas = new Santa[SantaNum]; 

        // rudolf 위치 입력받기
        st = new StringTokenizer(br.readLine().trim());
        int rx = Integer.parseInt(st.nextToken());
        int ry = Integer.parseInt(st.nextToken());
        rudolf = new Point(rx, ry);

        // Santa 정보 입력받기
        for (int sIdx = 0; sIdx < SantaNum; sIdx++) {
            st = new StringTokenizer(br.readLine().trim());
            int sNum = Integer.parseInt(st.nextToken());
            int sx = Integer.parseInt(st.nextToken());
            int sy = Integer.parseInt(st.nextToken());

            santas[sNum-1] = new Santa(sNum, new Point(sx, sy));
            santaBoard[sx][sy] = sNum; 
        }

        simulation();
    }

    static void simulation() {
        // 1. 턴 수만큼 진행한다.
        for (int turn = 1; turn <= TurnNum; turn++) {
            // 2. 루돌프가 이동한다. 
            moveRudolf();
            // 2-1. 산타와의 충돌 여부를 확인한다.
            int boardNum = santaBoard[rudolf.x][rudolf.y];
            if (boardNum > 0) { // 2-2. 산타가 존재하면, 산타는 루돌프의 힘만큼 점수를 얻고 밀려난다.
                Santa curSanta = santas[boardNum-1];

                // 점수 얻기
                curSanta.score += RPower;
                
                // 산타 밀기
                santaBoard[rudolf.x][rudolf.y] = 0;
                int nx = rudolf.x + RPower * dx[rudolf.dir];
                int ny = rudolf.y + RPower * dy[rudolf.dir];
                // System.out.println("루돌프 방향: "+ rudolf.dir + ", 이동할 방향:" + nx + "," + ny);

                
                curSanta.pos.x = nx;
                curSanta.pos.y = ny;
                curSanta.pos.dir = rudolf.dir;

                // 범위 체크
                if (outOfRange(nx, ny)) {
                    curSanta.isAlive = false;
                    failSantaNum++; 
                } else {
                    // 산타 기절
                    curSanta.sleepTime = turn+1;
                    // 상호 작용 체크
                    interaction(curSanta);
                }
            }
            // 3. 산타가 이동한다.
            for (Santa santa : santas) {
                // 탈락되었거나 기절한 산타는 제외
                if (!santa.isAlive || santa.sleepTime >= turn) {
                    continue;
                }
                // 산타가 이동하지 않았으면 continue;
                if(!moveSanta(santa)) {
                    continue;
                }
                // 3-1. 루돌프와의 충돌 여부를 체크한다. 
                if (santa.pos.x == rudolf.x && santa.pos.y == rudolf.y) {
                    // System.out.print("충돌!!!!!" + santa.toString());
                    // 3-1-1. 산타의 힘만큼 점수를 얻는다.
                    santa.score += SPower;
                    // 3-1-2. 이동방향의 반대로 밀려난다.
                    santaBoard[rudolf.x][rudolf.y] = 0;
                    // System.out.print("현재 방향: " + santa.pos.dir);
                    santa.pos.dir = (santa.pos.dir + 2) % SANTA_DIR;
                    // System.out.println(", 이동할 방향: " + santa.pos.dir);
                    int nx = santa.pos.x + SPower * dx[santa.pos.dir];
                    int ny = santa.pos.y + SPower * dy[santa.pos.dir];
                    
                    santa.pos.x = nx;
                    santa.pos.y = ny;

                    // 범위 체크
                    if (outOfRange(nx, ny)) {
                        santa.isAlive = false;
                        failSantaNum++; 
                        // System.out.println("범위 벗어남" + nx + "," + ny);
                    } else {
                        // 산타 기절
                        santa.sleepTime = turn+1;
                        // 상호 작용 체크
                        // System.out.println("상호작용 해야함");
                        interaction(santa);
                    }
                }
            }

            // 4. 탈락하지 않은 산타들은 1점씩을 얻는다.
            for(Santa santa : santas) {
                if (santa.isAlive) {
                    santa.score++;
                }
            }

            // System.out.println("After turn:" + turn +"--------");
            // System.out.println("cur Rudolf: " + rudolf.x + "," + rudolf.y);
            // for(int i = 1; i <= Size; i++) {
            //     for (int j = 1; j <= Size; j++) {
            //         System.out.print(santaBoard[i][j] + " ");
            //     }
            //     System.out.println();
            // }
            // System.out.println("-----------------");

            // 1-1. 모든 산타가 탈락하면 턴을 멈춘다. 
            if (failSantaNum == SantaNum) {
                break; 
            }
        }
        // 5. 산타별 최종 점수를 출력한다.
        printScore();
    }

    static void printScore() {
        StringBuilder answer = new StringBuilder();
        for(Santa santa : santas) {
            answer.append(santa.score).append(" ");
            // System.out.println(santa.toString());
        }
        System.out.println(answer);
    }

    static void interaction(Santa santa) {  // 산타가 가려는 칸에 다른 산타가 존재하는가
        int x = santa.pos.x;
        int y = santa.pos.y;
        int dir = santa.pos.dir;

        if (santaBoard[x][y]==0) {
            santaBoard[x][y] = santa.num;
            // System.out.println("비어있어용 " + x + "," + y);
            return;
        } else {
            int existSantaNum = santaBoard[x][y];
            santaBoard[x][y] = santa.num;
            // System.out.println("굴러온 돌:" + santa.num + ", 박힌 돌:" + existSantaNum);
            Santa existSanta = santas[existSantaNum-1];

            int nx = x + dx[dir];
            int ny = y + dy[dir];

            if (outOfRange(nx,ny)) {
                existSanta.isAlive = false;
                failSantaNum++;
                return;
            } 

            existSanta.pos.x = nx;
            existSanta.pos.y = ny;
            existSanta.pos.dir = dir;

            interaction(existSanta);
        }
    }

    static boolean moveSanta(Santa curSanta) {
        // 루돌프와 가장 가까워지는 방향으로 이동한다.
        int originDist = getDistance(curSanta.pos.x, rudolf.x, curSanta.pos.y, rudolf.y);
        int minDist= originDist;
        int nextX = -1;
        int nextY = -1; 
        int dir = -1;

        for (int i = 0; i < SANTA_DIR; i++) {
            int nx = curSanta.pos.x + dx[i];
            int ny = curSanta.pos.y + dy[i];

            if (outOfRange(nx, ny)) {
                continue;
            }

            if (santaBoard[nx][ny]>0) { // 산타가 있는 칸으로는 이동할 수 없음
                continue;
            }
            
            int dist = getDistance(nx, rudolf.x, ny, rudolf.y);

            if (minDist > dist) { 
                minDist = dist;
                nextX = nx;
                nextY = ny;
                dir = i;
            }
        }

        if (originDist > minDist) {  // 기존 거리보다 가까울 때만 이동한다.
            santaBoard[curSanta.pos.x][curSanta.pos.y] = 0;
            santaBoard[nextX][nextY] = curSanta.num;

            curSanta.pos.x = nextX;
            curSanta.pos.y = nextY;
            curSanta.pos.dir = dir;
            return true;
        }
        return false;
    }

    static void moveRudolf() {
        // 가장 가까운 산타를 찾는다. 거리가 같다면 r, c 가 큰 산타의 좌표를 찾는다.
        int minDist = Integer.MAX_VALUE;
        int nearestSantaX = -1;
        int nearestSantaY = -1; 

        for (Santa santa : santas) {
            if (!santa.isAlive) {
                continue;
            }

            int curDist = getDistance(santa.pos.x, rudolf.x, santa.pos.y, rudolf.y);
            if (minDist > curDist || minDist == curDist) {
                minDist = curDist;
                if (santa.pos.x > nearestSantaX) {
                    nearestSantaX = santa.pos.x;
                    nearestSantaY = santa.pos.y;
                } else if (santa.pos.x == nearestSantaX && santa.pos.y > nearestSantaY) {
                    nearestSantaY = santa.pos.y;
                }
            }
        }

        // 산타의 좌표와 가장 가까워지는 방향으로 이동한다.
        int nextX = -1; 
        int nextY = -1;
        int dir = -1;
        minDist = Integer.MAX_VALUE;
        for (int i = 0; i < RUDOLF_DIR; i++) {
            int nx = rudolf.x + dx[i];
            int ny = rudolf.y + dy[i];

            if (outOfRange(nx, ny)) {
                continue;
            }
            
            int dist = getDistance(nearestSantaX, nx, nearestSantaY, ny);
            if (minDist > dist) {
                minDist = dist;
                nextX = nx;
                nextY = ny;
                dir = i;
            }
        }
        rudolf.x = nextX;
        rudolf.y = nextY;
        rudolf.dir = dir;
    }

    static int getDistance(int x1, int x2, int y1, int y2) {
        int deltaX = Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2);

        return deltaX * deltaX + deltaY * deltaY;
    }

    static boolean outOfRange(int x, int y) {
        return x < 1 || x > Size || y < 1 || y > Size; 
    }
}