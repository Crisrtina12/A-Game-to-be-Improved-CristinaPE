package com.ieselgrao.gametofork.controller;

import com.ieselgrao.gametofork.model.GameModel;
import com.ieselgrao.gametofork.MainApplication;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class GameController {

    @FXML
    private Pane gamePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label livesLabel;

    private GameModel model;
    private AnimationTimer gameLoop;
    private Random random = new Random();

    // Parámetros de los círculos
    private final double MIN_RADIUS = 10;
    private final double MAX_RADIUS = 30;
    private final double FALL_SPEED = 1;
    private final double LOST_LINE_Y = 550; // Línea cerca del pie de la ventana (600px)

    @FXML
    public void initialize() {
        model = MainApplication.getGameModel();

        // Bindeo de etiquetas a las propiedades del modelo
        scoreLabel.textProperty().bind(model.scoreProperty().asString("Puntuación: %d"));
        livesLabel.textProperty().bind(model.livesProperty().asString("Vidas: %d"));

        // Dibuja la línea roja de pérdida de vida
        Line lossLine = new Line(0, LOST_LINE_Y, gamePane.getWidth(), LOST_LINE_Y);
        lossLine.setStroke(Color.RED);
        lossLine.setStrokeWidth(2);
        gamePane.getChildren().add(lossLine);

        // Inicia el ciclo del juego
        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastSpawnTime = 0;
            private final long SPAWN_INTERVAL_NS = 1_000_000_000L; // Spawn cada 1 segundo

            @Override
            public void handle(long now) {
                // Generar nuevos círculos
                if (now - lastSpawnTime > SPAWN_INTERVAL_NS) {
                    createRandomCircle();
                    lastSpawnTime = now;
                }

                // Actualizar posición y revisar colisiones
                updateCircles();

                // Revisar fin del juego
                if (model.isGameOver()) {
                    stop();

                    // 🔊 Reproducir sonido de Game Over
                    String soundPath = getClass().getResource("/com/ieselgrao/gametofork/sounds/gameover.wav").toExternalForm();
                    Media sound = new Media(soundPath);
                    MediaPlayer gameOverPlayer = new MediaPlayer(sound);
                    gameOverPlayer.play();

                    try {
                        MainApplication.switchToGameOverView();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        gameLoop.start();
    }

    private void createRandomCircle() {
        double radius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * random.nextDouble();
        double x = radius + (random.nextDouble() * (gamePane.getWidth() - 2 * radius));

        Circle circle = new Circle(radius, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        circle.setLayoutX(x);
        circle.setLayoutY(-radius); // Inicia fuera de la parte superior

        // Asigna puntos según el tamaño (círculos más pequeños dan más puntos)
        int points = (int) (MAX_RADIUS - radius + 1);
        circle.setUserData(points);

        // Evento de click: Pop y sumar puntos
        circle.setOnMouseClicked(event -> {
            model.addScore((int) circle.getUserData());
            gamePane.getChildren().remove(circle);

            // 🔊Reproducir sonido al hacer clic
            String soundPath = getClass().getResource("/com/ieselgrao/gametofork/sounds/click.wav").toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer clickPlayer = new MediaPlayer(sound);
            clickPlayer.play();

            event.consume();
        });

        gamePane.getChildren().add(circle);
    }

    private void updateCircles() {
        // Usamos un Iterator seguro para evitar errores al modificar la lista mientras iteramos
        Iterator<javafx.scene.Node> iterator = gamePane.getChildren().iterator();
        while (iterator.hasNext()) {
            javafx.scene.Node node = iterator.next();
            if (node instanceof Circle circle) {
                // Mover el círculo
                circle.setLayoutY(circle.getLayoutY() + FALL_SPEED);

                // Comprobar si ha rebasado la línea de pérdida de vida
                if (circle.getLayoutY() > LOST_LINE_Y) {
                    model.loseLife();
                    iterator.remove(); // Eliminar el círculo
                }
            }
        }
    }
}