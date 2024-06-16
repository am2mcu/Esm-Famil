import javafx.application.Application;
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

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private int port;
    private int numberOfRounds;
    private char letter;
    private boolean[] chosenFields = new boolean[Menu.NUMBER_OF_FIELDS];
    private boolean endsByTime;
    private int time;
    private String[] fieldsValues = {"", "", "", "", "", "", "", "", "", "", ""};
    private Socket socket;
    BufferedReader input;
    PrintWriter output;
    ObjectInputStream objectInput;
    ObjectOutputStream objectOutput;
    private int[] scores = new int[Menu.NUMBER_OF_FIELDS];
    private int winnerScore, totalScore;
    private int[] queueIndex;

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public char getLetter() {
        return letter;
    }

    public boolean[] getChosenFields() {
        return chosenFields;
    }

    public boolean isEndsByTime() {
        return endsByTime;
    }

    public int getTime() {
        return time;
    }

    public String[] getFieldsValues() {
        return fieldsValues;
    }

    public int[] getScores() {
        return scores;
    }

    public int getWinnerScore() {
        return winnerScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int[] getQueueIndex() {
        return queueIndex;
    }

    public Client(int port) throws Exception {
        this.port = port;

        try {
            socket = new Socket("127.0.0.1", port);
        } catch (Exception e) {e.printStackTrace();}

        receiveGameOptions();

        // new Game().start(new Stage());
    }

    public void closeClient() throws IOException {
        socket.close();
    }

    private void receiveGameOptions() throws IOException, ClassNotFoundException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        numberOfRounds = Integer.parseInt(input.readLine());
        letter = input.readLine().charAt(0);
        for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
            chosenFields[j] = input.readLine().equals("true");
        }
        endsByTime = input.readLine().equals("true");
        time = Integer.parseInt(input.readLine());

        queueIndex = new int[numberOfRounds - 1];
        for (int i = 0; i < numberOfRounds - 1; i++) {
            queueIndex[i] = Integer.parseInt(input.readLine());
        }

        System.out.println(queueIndex);
    }

    public void sendData() throws IOException {
        output = new PrintWriter(socket.getOutputStream(), true);

        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            output.println(fieldsValues[i]);
        }
    }

    public void receiveScores() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
            scores[j] = Integer.parseInt(input.readLine());
        }
    }

    public void receiveWinnerScoreAndTotalScore() throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        winnerScore = Integer.parseInt(input.readLine());
        totalScore = Integer.parseInt(input.readLine());
    }

    public void sendNextLetter(char letter) throws IOException {
        output = new PrintWriter(socket.getOutputStream(), true);

        output.println(letter);
    }
}
