import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/*
 * 1. 이동
 *   벽이 있으면 안됨
 *   경계 벗어나면 안됨
 *   상하좌우 순으로 출구와의 최단거리가 가까운 방향 찾기
 *   없으면 이동하지 않음
 * 2. 사각형 만들기
 *   출구의 왼쪽 윗칸 * (n-1) 을 기준으로 정사각형 만들기 (n=2부터)
 *   참가자를 포함할 때까지 n++ 반복
 *   기준점을 왼쪽위로 두고 n*n 크기의 사각형 만듦
 *   슬라이딩 윈도우, 경계를 넘어가면 continue;
 * 3. 사각형 돌리기
 * - 2차원 배열에는 벽 정보만 두고 사람은 사각형 찾을 때 돌릴까?
 * - 모든 참가자들의 이동거리 합 필요
 */
public class Main {

    final static int UL = 0; // 왼쪽 위 방향 탐색을 위한 변수
    static int[] dx = {-1, 1, -1, 0, 0}, dy = {-1, 0, 0, -1, 1};

    static class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static Point Exit;
    static List<Point> runners;
    static int[][] board;
    static int Size, RunnerNum, EndTime, moveCnt;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine().trim(), " ");
        // 0. 입력 받기
        Size = Integer.parseInt(st.nextToken());
        RunnerNum = Integer.parseInt(st.nextToken());
        EndTime = Integer.parseInt(st.nextToken());

        board = new int[Size + 1][Size + 1];
        for (int row = 1; row <= Size; row++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            for (int col = 1; col <= Size; col++) {
                board[row][col] = Integer.parseInt(st.nextToken());
            }
        }

        runners = new ArrayList<>();
        for (int i = 0; i <= RunnerNum; i++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());

            if (i == RunnerNum) {
                Exit = new Point(x, y);
                break;
            }
            runners.add(new Point(x, y));
        }

        simulation();
    }

    private static void simulation() {
        while (EndTime-- > 0) { // 종료조건 1. 시간이 끝난 경우
            // 1. 참가자 이동
            moveRunners();
            if (runners.isEmpty()) {    // 종료조건 2. 모든 참가자가 이동 완료한 경우
                break;
            }
            // 2. 사각형 돌리기
            moveMaze();
        }
        System.out.println(moveCnt);
        System.out.println(Exit.x + " " + Exit.y);
    }

    private static void moveMaze() {
        // 사각형 찾기
        int n = 2;      // 사각형 한 변의 길이
        int x, y;
        while (n <= Size) {
            //  출구 왼쪽 위 좌표를 기준으로 한다.
            x = Exit.x + dx[UL] * (n - 1);
            y = Exit.y + dy[UL] * (n - 1);

            for (int row = x; row < x + n; row++) {
                for (int col = y; col < y + n; col++) {
                    int ulX = row;
                    int ulY = col;
                    int drX = row + n - 1;
                    int drY = col + n - 1;

                    if (outOfRange(ulX, ulY) || outOfRange(drX, drY)) {
                        continue;
                    }

                    if (checkRunners(ulX, ulY, drX, drY)) { // 러너가 존재하면
                        // 미로 돌리기
                        rotateMaze(ulX, ulY, drX, drY);
                        // 출구도 돌리기
                        int rx = ulX + (Exit.y - ulY);  // 출구의 새로운 x 좌표 계산
                        int ry = ulY + (drX - Exit.x);  // 출구의 새로운 y 좌표 계산
                        Exit.x = rx;
                        Exit.y = ry;

                        return;
                    }
                }
            }
            n++;
        }
    }

    private static void rotateMaze(int upperX, int upperY, int lowerX, int lowerY) {
        int[][] temp = new int[lowerX - upperX + 1][lowerY - upperY + 1];

        for (int i = upperX; i <= lowerX; i++) {
            for (int j = upperY; j <= lowerY; j++) {
                temp[i - upperX][j - upperY] = board[i][j];
            }
        }

        for (int i = upperX; i <= lowerX; i++) {
            for (int j = upperY; j <= lowerY; j++) {
                board[i][j] = temp[lowerY - j][i - upperX];
                if (board[i][j] > 0) {
                    board[i][j]--; // 내구도 1 감소
                }
            }
        }
    }

    private static boolean checkRunners(int upperX, int upperY, int lowerX, int lowerY) {
        boolean isExist = false;
        for (Point curRunner : runners) {
            if (curRunner.x >= upperX && curRunner.x <= lowerX) {
                if (curRunner.y >= upperY && curRunner.y <= lowerY) {
                    isExist = true;

                    int nx = upperX + (curRunner.y - upperY);  // 러너의 새로운 x 좌표 계산
                    int ny = upperY + (lowerX - curRunner.x);  // 러너의 새로운 y 좌표 계산
                    curRunner.x = nx;
                    curRunner.y = ny;
                }
            }
        }
        return isExist;
    }

    private static void moveRunners() {
        //
        for (int size = runners.size(), idx = size - 1; idx >= 0; idx--) {   //  삭제를 고려해 뒤에서 탐색
            Point curRunner = runners.get(idx);
            int minDist = getDistance(curRunner, Exit);

            int x = curRunner.x;
            int y = curRunner.y;
            boolean isMove = false;

            // 이동하기
            for (int i = 1; i <= 4; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];
                // 범위 밖인 경우
                if (outOfRange(nx, ny)) {
                    continue;
                }
                // 벽인 경우
                if (board[nx][ny] > 0) {
                    continue;
                }

                // 출구에 도착한 경우
                if (nx == Exit.x && ny == Exit.y) {
                    isMove = true;
                    runners.remove(idx);
                    break;
                }

                int dist = getDistance(new Point(nx, ny), Exit);
                if (minDist > dist) { // 거리가 최소가 되는 경우만 러너 좌표 갱신. 이미 우선순위 순으로 이동하므로 거리가 같은 경우는 갱신 X
                    isMove = true;
                    minDist = dist;
                    curRunner.x = nx;
                    curRunner.y = ny;
                }
            }
            if (isMove) moveCnt++;
        }
    }

    private static int getDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private static boolean outOfRange(int x, int y) {
        return x < 1 || x >= (Size + 1) || y < 1 || y >= (Size + 1);
    }
}