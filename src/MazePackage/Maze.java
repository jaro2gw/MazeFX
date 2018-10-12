package MazePackage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Jaro on 2017-02-02.
 */
public class Maze {
    public static String[][] maze;
    public static int xSize;
    public static int ySize;
    private int[][] levelArray;
    private int sleepTime = 100;
    private boolean destinyFound = false;
    private static boolean[][] visited;
    private LinkedList<Thread> threads = new LinkedList<>();

    public int getSleepTime() {
        return sleepTime;
    }

    synchronized private void addToList(Point point, List<Point> list) {
        list.add(point);
    }

    synchronized private void addToQueue(Point point, Queue<Point> list) {
        list.add(point);
    }

    public LinkedList<MazePackage.Maze.Point> getPoints() {
        return points;
    }

    private LinkedList<Point> points;
    public static Point startPoint, destinyPoint;

    public void setStartPoint(int x, int y) {
        startPoint = new Point(x, y);
    }

    public void setDestinyPoint(int x, int y) {
        destinyPoint = new Point(x, y);
    }

    public Maze(int xMax, int yMax, int val) {
        maze = new String[xMax][yMax];
//        startPoint = new Point(X, Y);
//        destinyPoint = new Point(x2, y2);
        xSize = xMax;
        ySize = yMax;
        levelArray = new int[xSize][ySize];
        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < yMax; y++) {
                int value = x * x + 3 * x + 2 * x * y + y + y * y + val;
                String binary = Integer.toBinaryString(value);
                int ileJedynek = 0;
                for (char ch : binary.toCharArray()) if (ch == '1') ileJedynek++;
//                maze[x][y] = (ileJedynek % 2 == 0) ? " " : (ANSI_BLACK_BACKGROUND + "#" + ANSI_RESET);
                maze[x][y] = (ileJedynek % 2 == 0) ? " " : ("#");
            }
        }
    }

    public void kill() {
        for (int i = 0; i < threads.size(); i++) threads.get(i).interrupt();
    }

    private void fork(Point point) {
        Queue<Point> pointQueue = new LinkedList<>();
        pointQueue.add(point);
        while (!pointQueue.isEmpty()) {
            while (pointQueue.size() > 2) {
                Point p = pointQueue.remove();
                Thread th = new Thread(() -> fork(p));
                threads.add(th);
                th.start();
            }
            point = pointQueue.remove();
            makeTrue(point.x, point.y);
            if (point.equals(destinyPoint) && !destinyFound) {
                destinyFound = true;
                Thread th = new Thread(this::findShortestPath);
                threads.add(th);
                th.start();
            }
            point.findNeighbours();
            int level = levelArray[point.x][point.y];
            for (Point p : point.neighbours) {
                if (!visited[p.x][p.y] && !pointQueue.contains(p)) {
                    addToList(p, points);
                    addToQueue(p, pointQueue);
                    levelArray[p.x][p.y] = level + 1;
                    waitTime();
                }
            }
        }
    }

    synchronized private void makeTrue(int x, int y) {
        visited[x][y] = true;
    }

    private void waitTime() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

    public void BFS(Point point) {
        for (int x = 0; x < xSize; x++)
            for (int y = 0; y < ySize; y++) if (maze[x][y].equals(" ")) levelArray[x][y] = 0;

        levelArray[point.x][point.y] = 1;
        visited = new boolean[xSize][ySize];
        points = new LinkedList<>();
        addToList(point, points);
        fork(point);
    }

    public LinkedList<Point> shortestPath = new LinkedList<>();

    public boolean isDestinyFound() {
        return destinyFound;
    }

    void findShortestPath() {
        Point currentPoint = destinyPoint;
        shortestPath.add(currentPoint);
        int level, curLevel = -1000;
        while (!currentPoint.equals(startPoint)) {
            level = levelArray[currentPoint.x][currentPoint.y];
            currentPoint.findNeighbours();
            for (Point potentialMove : currentPoint.neighbours)
                if (levelArray[potentialMove.x][potentialMove.y] == level - 1 && curLevel != level) {
                    shortestPath.add(potentialMove);
                    curLevel = level;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                    currentPoint = potentialMove;
                }
        }
    }

    public static class Point {
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        int x, y;
        ArrayList<Point> neighbours;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
            neighbours = new ArrayList<>();
        }

        public void findNeighbours() {
            if (x == 0) {
                if (maze[x + 1][y].equals(" ")) neighbours.add(new Point(x + 1, y));
            } else if (x + 1 == xSize) {
                if (maze[x - 1][y].equals(" ")) neighbours.add(new Point(x - 1, y));
            } else {
                if (maze[x + 1][y].equals(" ")) neighbours.add(new Point(x + 1, y));
                if (maze[x - 1][y].equals(" ")) neighbours.add(new Point(x - 1, y));
            }

            if (y == 0) {
                if (maze[x][y + 1].equals(" ")) neighbours.add(new Point(x, y + 1));
            } else if (y + 1 == ySize) {
                if (maze[x][y - 1].equals(" ")) neighbours.add(new Point(x, y - 1));
            } else {
                if (maze[x][y + 1].equals(" ")) neighbours.add(new Point(x, y + 1));
                if (maze[x][y - 1].equals(" ")) neighbours.add(new Point(x, y - 1));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (x != point.x) return false;
            return y == point.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }
}
