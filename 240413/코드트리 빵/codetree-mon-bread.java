import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/*
 * 1. 편의점에서 가장 가까운 베이스캠프 찾기(우선순위 행,열 작은 순)
 * 2. 이동할 사람 있으면 편의점과 가장 가까운 방향으로 1칸 이동, 우선순위 상좌우하 (단순 맨해튼 X)
 * 3. t<=m, 현재 시간 = t이면 t번째 사람 베이스캠프에 넣기
 * 4. 모두 편의점 도착하면 멈추기
 * !! 출발한 사람이 있는 베이스캠프나 사람이 도착한 편의점(모두 이동이 끝난 뒤부터)으로는 이동 불가
 */
public class Main {
    static class PathPoint {    // 다음 방향 저장을 위한 객체
        int x, y;
        List<Integer> path;

        public PathPoint(int x, int y, List<Integer> path) {
            this.x = x;
            this.y = y;
            this.path = path;
        }
    }

    static class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Person {
        int num;
        Point pos;

        public Person(int num, Point pos) {
            this.num = num;
            this.pos = pos;
        }
    }

    static int[] dx = {-1, 0, 0, 1}, dy = {0, -1, 1, 0};

    static List<Person> movePeople; // 이동하는 사람 정보
    static List<Point> baseCamps; // 베이스캠프 위치 정보
    static Point[] cvStores; // 편의점 위치 정보
    static boolean[][] board; // 베이스캠프, 편의점 정보 저장을 위한 맵
    static int Size, PersonNum, finNum, curTime; // 맵 사이즈, 사람=편의점 수, 편의점 도착한 사람 수

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine().trim(), " ");
        // 0. 입력 받기
        Size = Integer.parseInt(st.nextToken());
        PersonNum = Integer.parseInt(st.nextToken());

        board = new boolean[Size + 1][Size + 1];
        baseCamps = new ArrayList<>();
        for (int row = 1; row <= Size; row++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            for (int col = 1; col <= Size; col++) {
                board[row][col] = st.nextToken().equals("1");

                if (board[row][col]) {
                    baseCamps.add(new Point(row, col));
                }
            }
        }

        cvStores = new Point[PersonNum + 1];
        for (int idx = 1; idx <= PersonNum; idx++) {
            st = new StringTokenizer(br.readLine().trim(), " ");
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());

            cvStores[idx] = new Point(x, y);
        }

        simulation();
    }

    private static void simulation() {
        // 모든 사람이 편의점에 도착할 때까지 반복
        curTime = 1;
        movePeople = new ArrayList<>();
        while (true) {
            move();
            if (curTime <= PersonNum) {
                spawnPeople(curTime);
            }
            
            if (finNum == PersonNum) {
                break;
            }
            curTime++;
        }
        System.out.println(curTime);
    }

    private static void spawnPeople(int curTime) {
        Point strPoint = getStartPoints(curTime);
        movePeople.add(new Person(curTime, strPoint));
        board[strPoint.x][strPoint.y] = true;   //  사람이 출발한 베이스캠프로 이동 불가
    }

    private static void move() {
        if (movePeople.isEmpty()) {
            return;
        }
        List<Person> nextMove = new ArrayList<>();   // 다음 이동 정보
        boolean[][] nextMap = new boolean[Size + 1][Size + 1];  // 다음 맵 정보

        for (Person curPerson : movePeople) {
            Point target = cvStores[curPerson.num]; // 목표 편의점
            // 최소가 되는 방향 정하기
            int dir = findMinDir(curPerson.pos, target);

            int nx = curPerson.pos.x + dx[dir];
            int ny = curPerson.pos.y + dy[dir];

            if (nx == target.x && ny == target.y) { // 편의점 도착
                finNum++;
                nextMap[nx][ny] = true; // 다음 텀 이동부터는 편의점을 지나갈 수 없다.
                continue;
            }
            // 편의점에 도착하지 않았으면 다음 이동 정보에 추가
            curPerson.pos.x = nx;
            curPerson.pos.y = ny;
            nextMove.add(curPerson);
        }
        board = nextMap;    //  이동 배열 갱신
        movePeople = nextMove;  // 이동 정보 갱신
    }

    private static int findMinDir(Point start, Point target) {
        boolean[][] visited = new boolean[Size + 1][Size + 1];
        Queue<PathPoint> q = new ArrayDeque<>();
        q.add(new PathPoint(start.x, start.y, new ArrayList<>()));
        visited[start.x][start.y] = true;

        while (!q.isEmpty()) {
            PathPoint cur = q.poll();

            if (cur.x == target.x && cur.y == target.y) {
                return cur.path.get(0);
            }

            for (int i = 0; i < 4; i++) {
                int nx = cur.x + dx[i];
                int ny = cur.y + dy[i];

                if (outOfRange(nx, ny) || visited[nx][ny]) { // 범위를 벗어났거나 이미 방문한 지역
                    continue;
                }

                if (board[nx][ny]) { // 이동 불가 구역
                    continue;
                }

                visited[nx][ny] = true;
                ArrayList<Integer> newPath = new ArrayList<>();
                newPath.addAll(cur.path);
                newPath.add(i);
                q.add(new PathPoint(nx, ny, newPath));
            }
        }
        return -1;
    }

    private static boolean outOfRange(int x, int y) {
        return x < 1 || x >= (Size + 1) || y < 1 || y >= (Size + 1);
    }

    private static Point getStartPoints(int No) {
        // 해당 편의점과 가장 가까운 베이스캠프 위치 찾기
        int minDist = Integer.MAX_VALUE;
        int row = Size + 1;
        int col = Size + 1;
        int idx = -1;

        for (int i = 0, size = baseCamps.size(); i < size; i++) {
            Point curBC = baseCamps.get(i);
            int dist = getDistance(curBC, cvStores[No]);

            if (minDist > dist) {   // 현재 베이스캠프가 가장 가까우면 값 갱신
                minDist = dist;
                row = curBC.x;
                col = curBC.y;
                idx = i;
            } else if (minDist == dist) { // 최솟값이 같으면
                if (row > curBC.x) {    // 행 최솟값 갱신
                    row = curBC.x;
                    col = curBC.y;
                    idx = i;
                } else if (row == curBC.x && col > curBC.y) {    // 열 최솟값 갱신
                    col = curBC.y;
                    idx = i;
                }
            }
        }
        baseCamps.remove(idx);
        return new Point(row, col);
    }

    private static int getDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}