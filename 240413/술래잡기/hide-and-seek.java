import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    final static int SIGHT = 3;
    static int[] dx = {-1, 0, 1, 0};
    static int[] dy = {0, 1, 0, -1};

    static class Point {
        int x, y, dir;

        Point(int x, int y, int dir) {
            this.x = x;
            this.y = y;
            this.dir = dir;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Point{");
            sb.append("x=").append(x);
            sb.append(", y=").append(y);
            sb.append(", dir=").append(dir);
            sb.append('}');
            return sb.toString();
        }
    }

    static Point seeker; // 술래
    static List<Point> hiders; // 도망자 리스트
    static boolean[][] treeBoard; // 나무 위치 표시를 위한 배열

    static int[] seekerDir; // 술래 이동방향
    static int score, moveLimit, cycle;
    static int N, HiderNum, TreeNum, endTurn;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine().trim(), " ");

        N = Integer.parseInt(st.nextToken());
        HiderNum = Integer.parseInt(st.nextToken());
        TreeNum = Integer.parseInt(st.nextToken());
        endTurn = Integer.parseInt(st.nextToken());

        // 기본 값 설정, 입력 받기
        moveLimit = N * N - 1;
        cycle = 2 * moveLimit;
        seeker = new Point(N / 2 + 1, N / 2 + 1, 0);

        hiders = new ArrayList<>();
        treeBoard = new boolean[N + 1][N + 1];
        for (int i = 0; i < HiderNum; i++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int dir = Integer.parseInt(st.nextToken()); // 입력값 1: 좌우, 2: 상하

            hiders.add(new Point(x, y, dir));
        }

        for (int i = 0; i < TreeNum; i++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());

            treeBoard[x][y] = true;
        }

        getSeekerDirection();
        hideAndSeek();
    }

    private static void getSeekerDirection() {
        // 0. 술래 방향 설정
        seekerDir = new int[moveLimit * 2 + 1];

        int dir = 0, idx = 1, repeatNum = 1;
        makeDir:
        while (true) {
            for (int cycle = 0; cycle < 2; cycle++) {
                for (int repeat = 0; repeat < repeatNum; repeat++) {
                    seekerDir[idx++] = dir;
                    if (idx > moveLimit) {
                        break makeDir;
                    }
                }
                dir++;
                dir %= 4;
            }
            repeatNum++;
        }

        idx = 0;
        for (int rIdx = moveLimit + 1, size = seekerDir.length; rIdx < size; rIdx++) {
            seekerDir[rIdx] = (seekerDir[moveLimit - idx++] + 2) % 4;
        }
    }

    private static void hideAndSeek() {
        for (int turn = 1; turn <= endTurn; turn++) {
            // 1. 도망자 이동
            moveHiders();
            // 2. 술래 이동
            moveSeeker(turn);

            // 3. 모든 도망자를 잡았으면 더 진행할 필요가 없음
            if (hiders.isEmpty()) {
                break;
            }
        }
        System.out.println(score);
    }

    private static void moveSeeker(int curTurn) {
        // 2. 방향에 맞게 이동 한다.
        seeker.x += dx[seeker.dir];
        seeker.y += dy[seeker.dir];

        // 2-1. 다음 방향 정하기
        int term = (curTurn + 1) % cycle;
        if (term == 0) term = cycle;
        int dir = seekerDir[term];
        seeker.dir = dir;

        // 2-2. 도망자 확인
        int seekNum = 0;
        for (int r = 0; r < SIGHT; r++) {
            int nx = seeker.x + dx[seeker.dir] * r;
            int ny = seeker.y + dy[seeker.dir] * r;

            if (!inRange(nx, ny)) break; // 범위를 벗어난 경우 중단

            if (treeBoard[nx][ny]) continue; // 나무가 있는 경우 pass

            seekNum += findHiders(nx, ny);
        }
        // 2-3. 점수 계산
        score += seekNum * curTurn;
    }

    private static int findHiders(int targetX, int targetY) {
        int seekNum = 0;
        for (int size = hiders.size(), idx = size-1; idx >= 0; idx--) {
            Point curHider = hiders.get(idx);

            if (curHider.x == targetX && curHider.y == targetY) {
                hiders.remove(idx);
                seekNum++;
            }
        }
        return seekNum;
    }

    private static void moveHiders() {
        List<Point> nextMoves = new ArrayList<>();
        for (Point curHider : hiders) {

            // 술래와의 거리가 3 이하인 도망자들은 이동할 수 있다.
            if (getDistance(curHider, seeker) <= SIGHT) {
                int nx = curHider.x + dx[curHider.dir];
                int ny = curHider.y + dy[curHider.dir];

                if (inRange(nx, ny)) {  // 한칸 더 이동할 때 격자를 벗어나지 않으면
                    if (!correspondSeeker(nx, ny)) { // 술래 여부를 확인하고 이동한다.
                        curHider.x = nx;
                        curHider.y = ny;
                    }

                }  else {   // 격자를 벗어나면 방향을 바꾸고 한칸 더 이동가능한지 확인
                    curHider.dir = (curHider.dir + 2) % 4;
                    nx = curHider.x + dx[curHider.dir];
                    ny = curHider.y + dy[curHider.dir];

                    if (!correspondSeeker(nx, ny)) { // 술래 여부를 확인하고 이동한다.
                        curHider.x = nx;
                        curHider.y = ny;
                    }
                }
            }
            nextMoves.add(curHider);
        }
        hiders = nextMoves;
    }

    private static boolean correspondSeeker(int x, int y) {
        return x == seeker.x && y == seeker.y;
    }
    private static boolean inRange(int x, int y) {
        return x >= 1 && x <= N && y >= 1 && y <= N;
    }

    private static int getDistance(Point hider, Point seeker) {
        return Math.abs(hider.x - seeker.x) + Math.abs(hider.y - seeker.y);
    }

}