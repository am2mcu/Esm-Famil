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
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Server {
    private int port;
    private int numberOfPlayers, numberOfRounds;
    private char letter;
    private boolean[] chosenFields;
    private boolean endsByTime;
    private int time;
    private String[] fieldsValues = {"", "", "", "", "", "", "", "", "", "", ""};
    ServerSocket server;
    BufferedReader input;
    PrintWriter output;
    ObjectInputStream objectInput;
    ObjectOutputStream objectOutput;
    private Socket[] clients;
    private String[][] clientsFieldsValues;
    private int[][] scores;
    private Socket[] lettersQueue;

    public int[][] getScores() {
        return scores;
    }

    public void setFieldsValues(String[] fieldsValues) {
        this.fieldsValues = fieldsValues;
    }

    public Socket[] getLettersQueue() {
        return lettersQueue;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public Server(int numberOfPlayers, int numberOfRounds, char letter, boolean[] chosenFields, boolean endsByTime, int time) throws Exception {
        try {
            port = Integer.parseInt(new Scanner(new File("port.txt")).nextLine());
            new FileOutputStream("port.txt").write((port + 1 + "").getBytes());
        }
        catch (Exception e) {e.printStackTrace();}

        this.numberOfPlayers = numberOfPlayers - 1; // except server creator
        this.numberOfRounds = numberOfRounds;
        this.letter = letter;
        this.chosenFields = chosenFields;
        this.endsByTime = endsByTime;
        this.time = time;

        createServer();
        sendGameOptions();
    }

    private void createServer() throws IOException {
        server = new ServerSocket(port);
        // write to file
        String serverData = port +
                            "\n" + numberOfPlayers +
                            "\n" + numberOfRounds +
                            "\n" + letter +
                            "\n";
        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            serverData += chosenFields[i] + " ";
        }
        serverData += "\n" + endsByTime +
                            "\n" + time +
                            "\n";
        new FileOutputStream("servers.txt", true).write(serverData.getBytes());

        clients = new Socket[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            clients[i] = server.accept();
        }
    }

    public void closeServer() throws IOException {
        server.close();

        File inputFile = new File("servers.txt");
        Scanner scan = new Scanner(inputFile);

        File outputFile = new File("tmp.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true);

        int portLine;
        while (scan.hasNext()) {
            if ((portLine = scan.nextInt()) == port) {
                for (int i = 0; i < 7; i++) {
                    scan.nextLine();
                }
            } else {
                fileOutputStream.write((portLine + "").getBytes());
                for (int i = 0; i < 7; i++) {
                    fileOutputStream.write(scan.nextLine().getBytes());
                    fileOutputStream.write("\n".getBytes());
                }
            }
        }

        scan = new Scanner(outputFile);
        fileOutputStream = new FileOutputStream(inputFile);
        fileOutputStream.write("".getBytes());
        fileOutputStream = new FileOutputStream(inputFile, true);
        while (scan.hasNext()) {
            fileOutputStream.write(scan.nextLine().getBytes());
            fileOutputStream.write("\n".getBytes());
        }
    }

    private void sendGameOptions() throws IOException {
        lettersQueue();

        for (int i = 0; i < numberOfPlayers; i++) {
            output = new PrintWriter(clients[i].getOutputStream(), true);

            output.println(numberOfRounds);
            output.println(letter);
            for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
                output.println(chosenFields[j]);
            }
            output.println(endsByTime);
            output.println(time);

//            boolean wasIn = false;
//            for (int j = 0; j < numberOfRounds - 1; j++) {
//                if (clients[i].equals(lettersQueue[j])) {
//                    System.out.println("was in " + j);
//                    output.println(j);
//                    wasIn = true;
//                    break;
//                }
//            }
//            if (!wasIn) {
//                System.out.println("not in");
//                output.println(-1);
//            }
            for (int j = 0; j < numberOfRounds - 1; j++) {
                if (clients[i].equals(lettersQueue[j])) {
                    System.out.println("was in " + j);
                    output.println(j);
                }
                else {
                    output.println(-1);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Server{" +
                "port=" + port +
                ", numberOfPlayers=" + numberOfPlayers +
                ", numberOfRounds=" + numberOfRounds +
                ", letter=" + letter +
                ", chosenFields=" + Arrays.toString(chosenFields) +
                ", endsByTime=" + endsByTime +
                ", time=" + time +
                '}';
    }

    public void receiveData() throws IOException {
        String[][] tmp = new String[numberOfPlayers + 1][Menu.NUMBER_OF_FIELDS];
        tmp[0] = fieldsValues;

        clientsFieldsValues = new String[numberOfPlayers][Menu.NUMBER_OF_FIELDS];
        for (int i = 0; i < numberOfPlayers; i++) {
            input = new BufferedReader(new InputStreamReader(clients[i].getInputStream()));

            for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
                clientsFieldsValues[i][j] = input.readLine();
            }

            tmp[i + 1] = clientsFieldsValues[i];

            System.out.println(Arrays.toString(clientsFieldsValues[i]));
        }

        checkDictionary(tmp);
        // return tmp;
    }

    public void checkDictionary(String[][] tmp) throws FileNotFoundException {
        System.out.println(letter);

        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i]) {
                for (int j = 0; j < numberOfPlayers + 1; j++) {
                    System.out.println("khode kalame: " + tmp[j][i]);
                    if (!tmp[j][i].equals("")) {
                        System.out.println("khlai nist");
                        if (tmp[j][i].charAt(0) != letter) {
                            tmp[j][i] = "";
                            System.out.println("harfe aval dorost nabood");
                        }
                    }
                }
            }
        }

        for (int i = 0; i < numberOfPlayers + 1; i++) {
            System.out.println("check dictionary: " + Arrays.toString(tmp[i]));
        }

        for (Fields field: Fields.values()) {
            if (chosenFields[field.ordinal()]) {
                System.out.println("field name: " + field.name());

                for (int i = 0; i < numberOfPlayers + 1; i++) {
                    File file = new File("words/" + field.name() + ".txt");
                    Scanner scan = new Scanner(file);
                    boolean found = false;
                    while (scan.hasNext()) {
                        if (scan.nextLine().equals(tmp[i][field.ordinal()])) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        tmp[i][field.ordinal()] = "";
                }
            }
        }

        for (int i = 0; i < numberOfPlayers + 1; i++) {
            System.out.println(Arrays.toString(tmp[i]));
        }

        calcScore(tmp);
        // return tmp;
    }

    public void calcScore(String[][] tmp) {
        scores = new int[numberOfPlayers + 1][Menu.NUMBER_OF_FIELDS];

        for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
            if (chosenFields[i]) {
                for (int j = 0; j < numberOfPlayers + 1; j++) {
                    for (int k = j+1; k < numberOfPlayers + 1; k++) {
                        if (!tmp[j][i].equals("") & tmp[j][i].equals(tmp[k][i])) {
                            scores[j][i] = scores[k][i] = 5;
                        }
                    }
                    if (!tmp[j][i].equals("") & scores[j][i] != 5) {
                        scores[j][i] = 10;
                    }
                }
            }
        }

        for (int i = 0; i < numberOfPlayers + 1; i++) {
            System.out.println(Arrays.toString(tmp[i]));
        }
        for (int i = 0; i < numberOfPlayers + 1; i++) {
            System.out.println(Arrays.toString(scores[i]));
        }

        // return scores;
    }

    public int[] calcTotalScores(int[] totalScores, int[][] scores) {
        for (int i = 0; i < numberOfPlayers + 1; i++) {
            for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
                totalScores[i] += scores[i][j];
            }
        }
        return totalScores;
    }

    public void sendScores() throws IOException {
        for (int i = 0; i < numberOfPlayers; i++) {
            output = new PrintWriter(clients[i].getOutputStream(), true);

            for (int j = 0; j < Menu.NUMBER_OF_FIELDS; j++) {
                output.println(scores[i+1][j]);
            }
        }
    }

    public int sendWinnerScoreAndTotalScore(int[] totalScore) throws IOException {
        int maxScore = 0;
        for (int i = 0; i < numberOfPlayers + 1; i++) {
            if (totalScore[i] > maxScore)
                maxScore = totalScore[i];
        }

        for (int i = 0; i < numberOfPlayers; i++) {
            output = new PrintWriter(clients[i].getOutputStream(), true);
            output.println(maxScore);
            output.println(totalScore[i + 1]);
        }

        return maxScore;
    }

    private void lettersQueue() {
        lettersQueue = new Socket[numberOfRounds - 1];
        for (int i = 0; i < numberOfRounds - 1; i++) {
            lettersQueue[i] = clients[new Random().nextInt(numberOfPlayers)];
        }

        System.out.println(Arrays.toString(lettersQueue));
    }

    public char receiveNewLetter(Socket client) throws IOException {
        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        return input.readLine().charAt(0);
        // return letter
    }
}