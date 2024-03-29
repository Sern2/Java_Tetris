package my.tetris;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import my.tetris.figureFactory.Figure;
import my.tetris.figureFactory.FigureFactory;
import my.tetris.gui.Gui;
import my.tetris.utils.FigureStatus;
import my.tetris.utils.GridCoords;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Controller {
    private static final int NUM_OF_GRID_ROWS = 20;
    private static final int NUM_OF_GRID_COLUMNS = 10;

    private final Gui gui;

    private GridCoords pivot;


    public Controller(Gui gui) {
        this.gui = gui;

        initializeFigure();
        initializeTimeline();
        initializeMoving();
        initializeReset();
    }


    private void initializeFigure() {
        FigureFactory figureFactory = new FigureFactory();

        Figure figure = figureFactory.generateFigure();

        this.pivot = figure.getPivot();

        List<GridCoords> figureCoords = figure.getFigureCoords();
        List<Rectangle> figureRectangles = figure.getFigureRectangles();
        for (int i = 0; i < figureCoords.size(); i++) {
            int xCoord = figureCoords.get(i).getXCoord();
            int yCoord = figureCoords.get(i).getYCoord();
            Rectangle rectangle = figureRectangles.get(i);
            rectangle.setId(FigureStatus.FALLING.getId());
            gui.getGrid().add(rectangle, xCoord, yCoord);
        }
    }

    private void initializeTimeline() {
        KeyFrame keyFrame = new KeyFrame(Duration.millis(500), actionEvent -> {
            GridCoords increment = new GridCoords(0, 1);
            moveFigureDown(increment);
            updateScore();
        });
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void initializeMoving() {
        gui.initializeMoving();
    }

    private void initializeReset() {
        gui.getResetButton().setOnMouseClicked(mouseEvent -> {
            List<Rectangle> fallingFigure = getFallingFigure();
            List<Rectangle> bottomFigures = getBottomFigures();
            gui.getGrid().getChildren().removeAll(fallingFigure);
            gui.getGrid().getChildren().removeAll(bottomFigures);
            gui.getScore().setText("Score: " + 0);
            initializeFigure();
        });
    }


    private List<Rectangle> getFallingFigure() {
        return gui
                .getGrid()
                .getChildren()
                .stream()
                .filter(node -> Objects.equals(node.getId(), FigureStatus.FALLING.getId()))
                .map(node -> (Rectangle) node)
                .collect(Collectors.toList());
    }
    private List<Rectangle> getBottomFigures() {
        return gui
                .getGrid()
                .getChildren()
                .stream()
                .filter(node -> Objects.equals(node.getId(), FigureStatus.BOTTOM.getId()))
                .map(node -> (Rectangle) node)
                .collect(Collectors.toList());
    }
    private List<GridCoords> getFallingFigureCoords() {
        List<Rectangle> fallingFigure = getFallingFigure();
        List<GridCoords> fallingFigureCoords = new ArrayList<>();
        for (Rectangle rectangle : fallingFigure) {
            int xCoord = GridPane.getColumnIndex(rectangle);
            int yCoord = GridPane.getRowIndex(rectangle);
            fallingFigureCoords.add(new GridCoords(xCoord, yCoord));
        }
        return fallingFigureCoords;
    }
    private List<GridCoords> getBottomFiguresCoords() {
        List<Rectangle> bottomFigure = getBottomFigures();
        List<GridCoords> bottomFigureCoords = new ArrayList<>();
        for (Rectangle rectangle : bottomFigure) {
            int xCoord = GridPane.getColumnIndex(rectangle);
            int yCoord = GridPane.getRowIndex(rectangle);
            bottomFigureCoords.add(new GridCoords(xCoord, yCoord));
        }
        return bottomFigureCoords;

    }
    private List<GridCoords> getRotatedFigureCoords(double angle) {
        List<GridCoords> fallingFigureCoords = getFallingFigureCoords();

        List<GridCoords> rotatedFigureCoords = new ArrayList<>();
        for(GridCoords fallingRectCoords : fallingFigureCoords) {
            int xCoord = fallingRectCoords.getXCoord() - pivot.getXCoord();
            int yCoord = fallingRectCoords.getYCoord() - pivot.getYCoord();
            int rotatedXCoord = (int) Math.round(xCoord * Math.cos(angle) - yCoord * Math.sin(angle));
            int rotatedYCoord = (int) Math.round(xCoord * Math.sin(angle) + yCoord * Math.cos(angle));
            rotatedFigureCoords.add(new GridCoords(
                    rotatedXCoord + pivot.getXCoord(),
                    rotatedYCoord + pivot.getYCoord()));
        }
        return rotatedFigureCoords;
    }
    private List<GridCoords> getIncrementedFigureCoords(GridCoords increment) {
        List<GridCoords> fallingFigureCoords = getFallingFigureCoords();
        List<GridCoords> newFallingFigureCoords = new ArrayList<>();
        for (GridCoords fallingRectCoords : fallingFigureCoords) {
            int xCoord = fallingRectCoords.getXCoord() + increment.getXCoord();
            int yCoord = fallingRectCoords.getYCoord() + increment.getYCoord();
            newFallingFigureCoords.add(new GridCoords(xCoord, yCoord));
        }
        return newFallingFigureCoords;
    }
    private List<List<GridCoords>> getHorizontalLines() {
        List<List<GridCoords>> allHorizontalLines = new ArrayList<>();
        for (int yCoord = 0; yCoord < NUM_OF_GRID_ROWS; yCoord++) {
            List<GridCoords> horizontalLine = new ArrayList<>();
            for (int xCoord = 0; xCoord < NUM_OF_GRID_COLUMNS; xCoord++) {
                horizontalLine.add(new GridCoords(xCoord, yCoord));
            }
            allHorizontalLines.add(horizontalLine);
        }
        return allHorizontalLines;
    }
    private List<Rectangle> getRectanglesAtCoords(List<GridCoords> coords) {
        List<Rectangle> bottomFigures = getBottomFigures();
        List<Rectangle> rectanglesAtSpecificCoords = new ArrayList<>();
        for (GridCoords currentCoord : coords) {
            int xCoord = currentCoord.getXCoord();
            int yCoord = currentCoord.getYCoord();
            Predicate<Rectangle> rectangleDetection = rect ->
                    GridPane.getRowIndex(rect) == yCoord && GridPane.getColumnIndex(rect) == xCoord;
            bottomFigures
                    .stream()
                    .filter(rectangleDetection)
                    .findFirst()
                    .ifPresent(rectanglesAtSpecificCoords::add);
        }
        return rectanglesAtSpecificCoords;
    }
    private List<Rectangle> getRectanglesUpperBound(int yCoord) {
        List<Rectangle> bottomFigures = getBottomFigures();
        return bottomFigures
                .stream()
                .filter(rectangle -> GridPane.getRowIndex(rectangle) < yCoord)
                .collect(Collectors.toList());
    }

    private void moveFigureToTheBottom() {
        GridCoords increment = new GridCoords(0, 1);
        while (true) {
            List<GridCoords> newFigureCoords = getIncrementedFigureCoords(increment);
            boolean moveAndRotationAreAllowed = moveAndRotationAreAllowed(newFigureCoords);
            if (moveAndRotationAreAllowed) {
                drawFigure(newFigureCoords);
                int xCoord = pivot.getXCoord() + increment.getXCoord();
                int yCoord = pivot.getYCoord() + increment.getYCoord();
                pivot = new GridCoords(xCoord, yCoord);
            } else {
                setAllFallingIDToBottomID();
                initializeFigure();
                break;
            }

        }
    }
    private void moveFigureDown(GridCoords increment) {
        List<GridCoords> newFigureCoords = getIncrementedFigureCoords(increment);
        boolean moveAndRotationAreAllowed = moveAndRotationAreAllowed(newFigureCoords);
        if (moveAndRotationAreAllowed) {
            drawFigure(newFigureCoords);
            int xCoord = pivot.getXCoord() + increment.getXCoord();
            int yCoord = pivot.getYCoord() + increment.getYCoord();
            pivot = new GridCoords(xCoord, yCoord);
        } else {
            setAllFallingIDToBottomID();
            initializeFigure();
        }
    }
    private void moveFigureToSide(GridCoords increment) {
        List<GridCoords> newFigureCoords = getIncrementedFigureCoords(increment);
        boolean moveAndRotationAreAllowed = moveAndRotationAreAllowed(newFigureCoords);
        if (moveAndRotationAreAllowed) {
            drawFigure(newFigureCoords);
            int xCoord = pivot.getXCoord() + increment.getXCoord();
            int yCoord = pivot.getYCoord() + increment.getYCoord();
            pivot = new GridCoords(xCoord, yCoord);
        }
    }
    private void rotateFigure(List<GridCoords> rotatedFigureCoords) {
        boolean moveAndRotationAreAllowed = moveAndRotationAreAllowed(rotatedFigureCoords);
        if (moveAndRotationAreAllowed) {
            drawFigure(rotatedFigureCoords);
        }
    }
    private void drawFigure(List<GridCoords> newFigureCoords) {
        List<Rectangle> fallingFigure = getFallingFigure();
        gui.getGrid().getChildren().removeAll(fallingFigure);
        for (int i = 0; i < fallingFigure.size(); i++) {
            int xCoord = newFigureCoords.get(i).getXCoord();
            int yCoord = newFigureCoords.get(i).getYCoord();
            Rectangle currentRectangle = fallingFigure.get(i);
            gui.getGrid().add(currentRectangle, xCoord, yCoord);
        }
    }
    private void moveRectanglesDown(List<Rectangle> rectangles) {
        for (Rectangle currentRectangle : rectangles) {
            Rectangle copy = new Rectangle();
            copy.setWidth(currentRectangle.getWidth());
            copy.setHeight(currentRectangle.getHeight());
            copy.setArcHeight(currentRectangle.getArcHeight());
            copy.setArcWidth(currentRectangle.getArcWidth());
            copy.setStroke(Color.BLACK);
            copy.setFill(currentRectangle.getFill());
            copy.setStrokeWidth(currentRectangle.getStrokeWidth());
            copy.setId(FigureStatus.BOTTOM.getId());

            int xCoord = GridPane.getColumnIndex(currentRectangle);
            int yCoord = GridPane.getRowIndex(currentRectangle) + 1;
            gui.getGrid().getChildren().remove(currentRectangle);
            gui.getGrid().add(copy, xCoord, yCoord);
        }
    }
    private void setAllFallingIDToBottomID() {
        List<Rectangle> fallinFigures = getFallingFigure();
        fallinFigures.forEach(rectangle -> rectangle.setId(FigureStatus.BOTTOM.getId()));
    }
    private void updateScore() {
        List<List<GridCoords>> allHorizontalLines = getHorizontalLines();
        for (int yCoord = NUM_OF_GRID_ROWS - 1; yCoord >= 0; yCoord--) {
            List<GridCoords> lineCoord = allHorizontalLines.get(yCoord);
            if(rectanglesFormALine(lineCoord)) {
                List<Rectangle> rectanglesFormLine = getRectanglesAtCoords(lineCoord);
                List<Rectangle> rectanglesUpperLine = getRectanglesUpperBound(yCoord);
                gui.getGrid().getChildren().removeAll(rectanglesFormLine);
                moveRectanglesDown(rectanglesUpperLine);
                yCoord++;
                int currentScore = Integer.parseInt(gui.getScore().getText().split(" ")[1]) + 10;
                gui.getScore().setText("Score: " + currentScore);
            }
        }
    }

    private boolean figureInsideGrid(List<GridCoords> newFigureCoords) {
        Predicate<GridCoords> insideXDetection = coords ->
                0 <= coords.getXCoord() && coords.getXCoord() <= NUM_OF_GRID_COLUMNS - 1;
        Predicate<GridCoords> insideYDetection = coords ->
                0 <= coords.getYCoord() && coords.getYCoord() <= NUM_OF_GRID_ROWS - 1;

        boolean figureInsideXCoord = newFigureCoords
                .stream()
                .allMatch(insideXDetection);
        boolean figureInsideYCoord = newFigureCoords
                .stream()
                .allMatch(insideYDetection);

        return figureInsideXCoord && figureInsideYCoord;

    }
    private boolean intersectionDetected(List<GridCoords> newFigureCoords) {
        List<GridCoords> bottomFiguresCoords = getBottomFiguresCoords();

        for (GridCoords fallingRectCoords : newFigureCoords) {
            Predicate<GridCoords> coordsIdentityDetection = coords ->
                    coords.getXCoord() == fallingRectCoords.getXCoord()
                            && coords.getYCoord() == fallingRectCoords.getYCoord();

            boolean intersectionDetected = bottomFiguresCoords
                    .stream()
                    .anyMatch(coordsIdentityDetection);
            if (intersectionDetected) {
                return true;
            }
        }
        return false;
    }
    private boolean moveAndRotationAreAllowed(List<GridCoords> newFigureCoords) {
        boolean figureInsideGrid = figureInsideGrid(newFigureCoords);
        boolean intersectionDetected = intersectionDetected(newFigureCoords);
        return figureInsideGrid && !intersectionDetected;
    }
    private boolean rectanglesFormALine(List<GridCoords> lineCoords) {
        List<GridCoords> bottomFiguresCoords = getBottomFiguresCoords();
        int countIntersections = 0;
        for (GridCoords currentCoord : lineCoords) {
            int xCoord = currentCoord.getXCoord();
            int yCoord = currentCoord.getYCoord();
            Predicate<GridCoords> coordsCrossing = gridCoords ->
                    gridCoords.getXCoord() == xCoord && gridCoords.getYCoord() == yCoord;
            boolean crossingDetected = bottomFiguresCoords
                    .stream()
                    .anyMatch(coordsCrossing);
            if (crossingDetected) countIntersections++;
        }
        return countIntersections == 10;
    }



    public void processRotateLeft() {
        List<GridCoords> coordsRotatedToRight = getRotatedFigureCoords(Math.toRadians(90));
        rotateFigure(coordsRotatedToRight);
    }

}