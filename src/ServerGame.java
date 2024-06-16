import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerGame extends Application {
    Server server;
    private int port;
    private int numberOfPlayers, numberOfRounds;
    private char letter;
    private boolean[] chosenFields;
    private boolean endsByTime;
    private int time;
    private int tempTime;
    private String[] fieldsValues = {"", "", "", "", "", "", "", "", "", "", ""};
    private Socket[] clients;
    private String[][] clientsFieldsValues;
    private int[][] scores;
    private int[] totalScores;

    public ServerGame(int numberOfPlayers, int numberOfRounds, char letter, boolean[] chosenFields, boolean endsByTime, int time) throws Exception {
        server = new Server(numberOfPlayers, numberOfRounds, letter, chosenFields, endsByTime, time);

        this.numberOfPlayers = numberOfPlayers;
        this.numberOfRounds = numberOfRounds;
        this.letter = letter;
        this.chosenFields = chosenFields;
        this.endsByTime = endsByTime;
        this.time = time;
        tempTime = time;

        totalScores = new int[numberOfPlayers + 1];
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox hBox = new HBox();
        hBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setSpacing(20);
        TextField[] fieldsTextField = new TextField[Menu.NUMBER_OF_FIELDS];
        TextField[] scoresTextField = new TextField[Menu.NUMBER_OF_FIELDS];
        for (Fields field: Fields.values()) {
            if (chosenFields[field.ordinal()]) {
                VBox vBox = new VBox();
                vBox.setSpacing(20);
                Label label = new Label(field.name());
                vBox.getChildren().add(label);

                fieldsTextField[field.ordinal()] = new TextField();
                fieldsTextField[field.ordinal()].setPromptText(field.name());
                vBox.getChildren().add(fieldsTextField[field.ordinal()]);

                scoresTextField[field.ordinal()] = new TextField();
                scoresTextField[field.ordinal()].setPromptText("امتیاز");
                scoresTextField[field.ordinal()].setEditable(false);
                vBox.getChildren().add(scoresTextField[field.ordinal()]);

                hBox.getChildren().add(vBox);
            }
        }

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(hBox);

        Button lastRoundScores = new Button("Scores");
        lastRoundScores.setAlignment(Pos.CENTER);
        borderPane.setCenter(lastRoundScores);
        lastRoundScores.addEventHandler(ActionEvent.ACTION, event -> {
            for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
                if (chosenFields[i])
                    scoresTextField[i].setText(scores[0][i] + "");
            }
        });

        if (!endsByTime) {
            Button finishButton = new Button("Finish");
            borderPane.setBottom(finishButton);

            AtomicBoolean finishClicked = new AtomicBoolean(false);
            new FileOutputStream("finished.txt").write("".getBytes());
            new Thread(() -> {
                try {
                    while (!finishClicked.get()) {
                        Scanner scan = new Scanner(new File("finished.txt"));
                        if (scan.hasNext())
                            if (scan.nextLine().equals("Finished")) {
                                Platform.runLater(() -> finishButton.setText("Finished"));
                                break;
                            }
                    }
                } catch (FileNotFoundException e) {e.printStackTrace();}
            }).start();

            finishButton.textProperty().addListener((observable, oldValue, newValue) -> {
                if (finishButton.getText().equals("Finished")) {
                    try {
                        newRound(primaryStage, fieldsTextField, scoresTextField);
                    } catch (IOException e) {e.printStackTrace();}
                }
            });

            finishButton.addEventHandler(ActionEvent.ACTION, event -> {
                try {
                    finishClicked.set(true);
                    new FileOutputStream("finished.txt").write("Finished".getBytes());
                } catch (Exception e) {e.printStackTrace();}

                try {
                    newRound(primaryStage, fieldsTextField, scoresTextField);
                } catch (IOException e) {e.printStackTrace();}
            });
        } else {
            Label timer = new Label();
            borderPane.setBottom(timer);
            timer.setText(tempTime + " second");
            Timer tm = new Timer();
            tm.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> timer.setText(tempTime + " second"));
                    tempTime--;
                    if (tempTime == 0) {
                        tm.cancel();
                        return;
                    }
                }
            }, 1000, 1000);

            timer.textProperty().addListener((observable, oldValue, newValue) -> {
                if (timer.getText().equals("0 second")) {
                    try {
                        newRound(primaryStage, fieldsTextField, scoresTextField);
                    } catch (IOException e) {e.printStackTrace();}
                }
            });
        }

        Scene gameScene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("اسم فامیل");
        primaryStage.show();
    }

    private void newRound(Stage primaryStage, TextField[] fieldsTextField, TextField[] scoresTextField) throws IOException {
        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i])
                fieldsValues[i] = fieldsTextField[i].getText();
        }
        server.setFieldsValues(fieldsValues);
        System.out.println(Arrays.toString(fieldsValues));

        try {
            server.receiveData();
            scores = server.getScores();
            totalScores = server.calcTotalScores(totalScores, scores);
        } catch (IOException e) {e.printStackTrace();}

        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i])
                scoresTextField[i].setText(scores[0][i] + "");
        }

        try {
            server.sendScores();
        } catch (IOException e) {e.printStackTrace();}

        if (numberOfRounds != 1) { // not 0 because at least 1st round will execute
            numberOfRounds--;

            // get new letter and set it to letter
            letter = server.receiveNewLetter(server.getLettersQueue()[numberOfRounds - 1]);
            server.setLetter(letter);

            primaryStage.close();
            try {
                tempTime = time;
                start(new Stage());
            } catch (Exception e) {e.printStackTrace();}
        } else if (numberOfRounds == 1) {
            try {
                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setSpacing(20);
                vBox.getChildren().addAll(new Label("Winner Score: " + server.sendWinnerScoreAndTotalScore(totalScores)),
                        new Label("Your Score: " + totalScores[0]));
                Scene scene = new Scene(vBox, 600, 400);
                primaryStage.setScene(scene);

                server.closeServer();
            } catch (IOException e) {e.printStackTrace();}
        } else
            primaryStage.close();
    }
}
