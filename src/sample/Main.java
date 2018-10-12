package sample;

import MazePackage.Maze;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.LinkedList;

public class Main extends Application {
    private GraphicsContext gc;
    private Maze lab;
    private int sleepTime;
    private ColorPicker pointy, path, fill;
    private Color pointColor, pathColor, fillColor;
    private Button res;
    private int seed;
    private Thread refresh, maze;

    static public boolean isClosing() {
        return closing;
    }

    private static boolean closing = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        seed = 1364;
        lab = new Maze(75, 75, seed);
        Group root = new Group();
        Canvas canvas = new Canvas(Maze.xSize * 10 + 10, Maze.ySize * 10 + 10);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        res = new Button("RESTART");
        res.setOnAction(event -> restart());

        ustawKolory();

        HBox hbox = new HBox(new Label("Kolor punktów:"), pointy, new Label("Kolor ścieżki:"), path, new Label("Kolor" +
                " wypełnienia:"), fill, res);
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);

        VBox vBox = new VBox(hbox, root);
        vBox.setAlignment(Pos.CENTER_RIGHT);
        vBox.setSpacing(10);

        primaryStage.setScene(new Scene(vBox));
        primaryStage.show();
        primaryStage.setOnCloseRequest(this::close);
        primaryStage.setResizable(false);
        primaryStage.setTitle("MAZE");

        runRefresh();
    }

    private void runRefresh() {
        refresh = new Thread(this::refresh);
        refresh.start();
    }

    private void restart() {
        maze.interrupt();
        refresh.interrupt();
        lab.kill();
//        lab = null;
        lab = new Maze(75, 75, seed);
        runRefresh();
    }

    private void ustawKolory() {
        pointColor = Color.LIGHTGREEN;
        pathColor = Color.CORNFLOWERBLUE;
        fillColor = Color.RED;
        pointy = new ColorPicker(pointColor);
        path = new ColorPicker(pathColor);
        fill = new ColorPicker(fillColor);
        pointy.setOnAction(event -> pointColor = pointy.getValue());
        path.setOnAction(event -> pathColor = path.getValue());
        fill.setOnAction(event -> fillColor = fill.getValue());
    }

    private void close(WindowEvent windowEvent) {
        closing = true;
        System.exit(0);
    }

    private void refresh() {
        sleepTime = lab.getSleepTime();
        maze = new Thread(() -> {
            lab.setStartPoint(1, 1);
            lab.setDestinyPoint(30, 38);
            lab.BFS(Maze.startPoint);
        });
        maze.start();
        while (!closing) {
            Platform.runLater(() -> {
                drawMap();
                LinkedList<Maze.Point> points = lab.getPoints();
                if (points != null) {
                    for (Maze.Point point : points) rysujPunkt(point, fillColor, 10);
                    if (lab.isDestinyFound()) {
                        LinkedList<Maze.Point> path = lab.shortestPath;
                        for (Maze.Point point : path) {
                            rysujPunkt(point, pathColor, 8);
                        }
                    }
                    rysujPunkt(Maze.startPoint, pointColor, 10);
                    rysujPunkt(Maze.destinyPoint, pointColor, 10);
                }
            });
            try {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e) {

            }
        }
    }

    private void rysujPunkt(Maze.Point point, Color c, int grubosc) {
        if (point == null) return;
        int x = point.getX();
        int y = point.getY();
        gc.setFill(c);
        gc.fillRect(x * 10 + (10 - grubosc) / 2, y * 10 + (10 - grubosc) / 2, grubosc, grubosc);

    }

    private void drawMap() {
        int w = 10, h = 10;
        String[][] strings = Maze.maze;
        for (int i = 0; i < strings.length; i++) {
            for (int j = 0; j < strings[i].length; j++) {
                int finalJ = j;
                int finalI = i;
                gc.setFill(strings[finalI][finalJ].equals(" ") ? Color.WHITE : Color.BLACK);
                gc.fillRect(finalI * w, finalJ * h, w, h);
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
