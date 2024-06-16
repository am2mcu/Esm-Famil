import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientGame extends Application {
    Client client;
    private int port;
    private int numberOfRounds;
    private char letter;
    private boolean[] chosenFields = new boolean[Menu.NUMBER_OF_FIELDS];
    private boolean endsByTime;
    private int time;
    private int tempTime;
    private String[] fieldsValues = {"", "", "", "", "", "", "", "", "", "", ""};
    int[] scores = new int[Menu.NUMBER_OF_FIELDS];

    public ClientGame(int port) throws Exception {
        this.port = port;

        client = new Client(port);
        numberOfRounds = client.getNumberOfRounds();
        letter = client.getLetter();
        chosenFields = client.getChosenFields();
        endsByTime = client.isEndsByTime();
        time = client.getTime();
        tempTime = time;
        fieldsValues = client.getFieldsValues();
        scores = client.getScores();
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
                    scoresTextField[i].setText(scores[i] + "");
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
                    newRound(primaryStage, fieldsTextField, scoresTextField);
                }
            });

            finishButton.addEventHandler(ActionEvent.ACTION, event -> {
                try {
                    finishClicked.set(true);
                    new FileOutputStream("finished.txt").write("Finished".getBytes());
                } catch (Exception e) {e.printStackTrace();}

                newRound(primaryStage, fieldsTextField, scoresTextField);
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
                    newRound(primaryStage, fieldsTextField, scoresTextField);
                }
            });
        }

        Scene gameScene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("اسم فامیل");
        primaryStage.show();
    }

    private void newRound(Stage primaryStage, TextField[] fieldsTextField, TextField[] scoresTextField) {
        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i])
                fieldsValues[i] = fieldsTextField[i].getText();
        }
        System.out.println(Arrays.toString(fieldsValues));
        try {
            client.sendData();
        } catch (IOException e) {e.printStackTrace();}

        try {
            client.receiveScores();
        } catch (IOException e) {e.printStackTrace();}

        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i])
                scoresTextField[i].setText(scores[i] + "");
        }

        if (numberOfRounds != 1) { // 1 because first round will always start
            numberOfRounds--;

            for (int i = 0; i < client.getQueueIndex().length; i++) {
                if (numberOfRounds - 1 == client.getQueueIndex()[i]) {
                    System.out.println("creating new window");

                    primaryStage.close();
                    Stage stage = new Stage();
                    VBox vBox = new VBox();
                    vBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                    vBox.setAlignment(Pos.CENTER);
                    vBox.setSpacing(20);
                    TextField newLetterTextField = new TextField();
                    newLetterTextField.setPromptText("حرف بعدی را وارد کنید");
                    Button submit = new Button("Submit");
                    vBox.getChildren().addAll(newLetterTextField, submit);
                    submit.addEventHandler(ActionEvent.ACTION, event -> {
                        try {
                            tempTime = time;
                            client.sendNextLetter(newLetterTextField.getText().charAt(0));
                            stage.close();
                        } catch (IOException e) {e.printStackTrace();}
                    });
                    Scene scene = new Scene(vBox, 600, 400);
                    stage.setScene(scene);
                    stage.setTitle("اسم فامیل");
                    stage.show();
                }
            }

            primaryStage.close();
            try {
                tempTime = time;
                start(new Stage());
            } catch (Exception e) {e.printStackTrace();}
        } else if (numberOfRounds == 1) {
            try {
                client.receiveWinnerScoreAndTotalScore();

                VBox vBox = new VBox();
                vBox.setAlignment(Pos.CENTER);
                vBox.setSpacing(20);
                vBox.getChildren().addAll(new Label("Winner Score: " + client.getWinnerScore()),
                        new Label("Your Score: " + client.getTotalScore()));
                Scene scene = new Scene(vBox, 600, 400);
                primaryStage.setScene(scene);

                client.closeClient();
            } catch (IOException e) {e.printStackTrace();}
        } else
            primaryStage.close();
    }
}
