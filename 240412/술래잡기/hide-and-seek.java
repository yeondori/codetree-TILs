import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/*
 * 0. n 홀수. 술래는 정중앙에서 시작 (2/n, 2/n)
 * 1. 도망자 이동
 *   1-0. 오른쪽, 아래쪽이 default
 *   1-1. 술래와의 거리가 3 이하이면 이동
 *       1-1-1. 이동할 곳에 술래가 있지 않고 격자를 벗어나지 않으면 1칸 이동
 *       1-1-2. 격자를 벗어나면 방향을 반대로 바꾸고 술래 여부를 확인 후 1칸 이동
 * 2. 술래 이동
 *   2-0. 시야는 항상 3
 *   2-1. 달팽이 모양(위-오른쪽-아래-왼쪽 순)으로 1칸씩 이동. 끝에 도달하면 거꾸로 중심으로 돌아가는 것을 반복
 *   2-2. 나무에 있지 않고, 시야 내에 있는(술래의 방향) 도망자 잡기
 *   2-3. 획득 점수: 턴 수 * 턴에서 잡힌 도망자의 수
 */
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
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public boolean equals(Object obj) {
            Point o = (Point) obj;
            return this.x == o.x && this.y == o.y;
        }
    }

    static Point seeker; // 술래
    static Map<Point, List<Point>> hiderPos; // 도망자 위치 표시를 위한 맵
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

        hiderPos = new HashMap<>();
        treeBoard = new boolean[N + 1][N + 1];
        for (int i = 0; i < HiderNum; i++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int dir = Integer.parseInt(st.nextToken()); // 입력값 1: 좌우, 2: 상하

            Point hider = new Point(x, y, dir);
            ArrayList<Point> curHiders = new ArrayList<>(); // 초기에 위치가 겹치는 도망자는 없음
            curHiders.add(hider);
            hiderPos.put(new Point(x, y, dir), curHiders);
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
        seekerDir = new int[moveLimit + 1];

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
    }

    private static void hideAndSeek() {
        for (int turn = 1; turn <= endTurn; turn++) {
            // 1. 도망자 이동
            moveHiders();
//            System.out.println(turn + ": 이동 완");
            // 2. 술래 이동
            moveSeeker(turn);

            // 3. 모든 도망자를 잡았으면 더 진행할 필요가 없음
            if (hiderPos.isEmpty()) {
                break;
            }
        }
        System.out.println(score);
    }

    private static void moveSeeker(int curTurn) {
        // 2. 방향에 맞게 이동 한다.
//        System.out.println("현위치: " + seeker.x + "," + seeker.y + " || dir:" + seeker.dir);
        seeker.x += dx[seeker.dir];
        seeker.y += dy[seeker.dir];

        // 2-1. 다음 방향 정하기
        int term = (curTurn + 1) % cycle;
        int dir = seekerDir[term];

        if (term < moveLimit) { // 2-1-1. 현재 턴이 이동 배열 범위 내에 있으면 정방향 이동
//            seeker.x += dx[dir];
//            seeker.y += dy[dir];
            seeker.dir = dir;

        } else { // 2-1-2. 현재 턴이 이동 배열 범위보다 크면 역방향 이동
            int diff = term - moveLimit;
            dir = seekerDir[moveLimit - diff];
            int rDir = (dir + 2) % 4;
//            seeker.x += dx[rDir];
//            seeker.y += dy[rDir];
            seeker.dir = rDir;
        }

        // 2-2. 도망자 확인
        int nx = seeker.x;
        int ny = seeker.y;

//        System.out.println("여기서 찾을거임: " + nx + "," + ny + ":: dir:" + seeker.dir);

        int seekNum = 0;
        seek:
        while (inRange(nx, ny)) {
            for (int r = 0; r < SIGHT; r++) {
                if (!treeBoard[nx][ny]) {    // 나무에 있지 않은 도망자들 잡기
                    Point curPoint = new Point(nx, ny, 0);
                    List<Point> findHiders = hiderPos.get(curPoint);

                    if (findHiders != null) {
                        seekNum += findHiders.size();
                        findHiders.clear();
//                        System.out.println("이만큼 잡음: " + seekNum);
                    }
                }
//                else {
//                    System.out.println("나무 발견!");
//                }

                nx += dx[seeker.dir];
                ny += dy[seeker.dir];
//                System.out.println("다음 찾을 위치:" + nx + "," + ny);
                if (!inRange(nx, ny)) {
                    break seek;
                }
            }
        }

        // 2-3. 점수 계산
        score += seekNum * curTurn;
    }

    private static void moveHiders() {
        Map<Point, List<Point>> nextPos = new HashMap<>();

        for (List<Point> curHiders : hiderPos.values()) {
            for (int size = curHiders.size(), idx = size - 1; idx >= 0; idx--) {
                Point curHider = curHiders.get(idx);
                // 1-1. 술래와의 거리가 3 이하이면 이동한다.
                int dist = getDistance(curHider, seeker);
                if (dist > SIGHT) {
                    nextPos.compute(curHider, (k, v) -> {
                        if (v == null) {
                            v = new ArrayList<>();
                        }
                        v.add(curHider);
                        return v;
                    });
                    continue;
                }

                // 1-2. 다음 이동 위치 정하기
                int nx = curHider.x + dx[curHider.dir];
                int ny = curHider.y + dy[curHider.dir];

                // 1-2-1. 격자를 벗어나지 않는 경우
                if (inRange(nx, ny)) {
                    if (nx == seeker.x && ny == seeker.y) { // 술래가 있으면 이동하지 않는다.
                        continue;
                    }
                } else { // 1-2-2. 격자를 벗어나는 경우
                    curHider.dir += 2; // 방향을 바꾸고 다음 칸 확인
                    curHider.dir %= 4;

                    nx = curHider.x + dx[curHider.dir];
                    ny = curHider.y + dy[curHider.dir];

                    if (nx == seeker.x && ny == seeker.y) { // 술래가 있으면 이동하지 않는다.
                        continue;
                    }
                }
                // 이동
                curHiders.remove(curHider);

                curHider.x = nx;
                curHider.y = ny;
//                System.out.println(curHider.x + "," + curHider.y + "로 이동");

                nextPos.compute(curHider, (k, v) -> {
                    if (v == null) {
                        v = new ArrayList<>();
                    }
                    v.add(curHider);
                    return v;
                });
//                System.out.println("이동 완:" + nextPos.get(curHider).toString());
            }
        }
        hiderPos = nextPos;
    }

    private static boolean inRange(int x, int y) {
        return x >= 0 && x <= N && y >= 0 && y <= N;
    }

    private static int getDistance(Point hider, Point seeker) {
        return Math.abs(hider.x - seeker.x) + Math.abs(hider.y - seeker.y);
    }

}