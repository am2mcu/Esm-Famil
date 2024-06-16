import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChooseServer extends Application {
    private ArrayList<Integer> portsList = new ArrayList<>();
    private ArrayList<Integer> numberOfPlayers = new ArrayList<>(), numberOfRounds = new ArrayList<>();
    private ArrayList<Character> letter = new ArrayList<>();
    private ArrayList<boolean[]> chosenFields = new ArrayList<>();
    private ArrayList<Boolean> endsByTime = new ArrayList<>();
    private ArrayList<Integer> time = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        File file = new File("servers.txt");
        Scanner scan = new Scanner(file);
        String tmpLine;

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        ArrayList<Button> buttonsList = new ArrayList<>();
        ArrayList<Button> infoList = new ArrayList<>();
        ArrayList<VBox> subVBoxes = new ArrayList<>();
        while (scan.hasNext()) {
            portsList.add(scan.nextInt());
            tmpLine = scan.nextLine();
            numberOfPlayers.add(scan.nextInt());
            tmpLine = scan.nextLine();
            numberOfRounds.add(scan.nextInt());
            tmpLine = scan.nextLine();
            letter.add(scan.nextLine().charAt(0));
            boolean[] tmp = new boolean[Menu.NUMBER_OF_FIELDS];
            for (int i = 0; i < Menu.NUMBER_OF_FIELDS; i++) {
                tmp[i] = scan.nextBoolean();
            }
            chosenFields.add(tmp);
            tmpLine = scan.nextLine();
            endsByTime.add(scan.nextBoolean());
            tmpLine = scan.nextLine();
            time.add(scan.nextInt());
            tmpLine = scan.nextLine();

            buttonsList.add(new Button(portsList.get(portsList.size() - 1) + ""));
            Button buttonInfo = new Button();
            buttonInfo.setGraphic(new ImageView("assets/downArrow.png"));
            infoList.add(buttonInfo);
            HBox hBox = new HBox();
            hBox.setSpacing(20);
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().addAll(buttonsList.get(buttonsList.size() - 1), infoList.get(infoList.size() - 1));
            VBox subVBox = new VBox();
            subVBox.setAlignment(Pos.CENTER);
            subVBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            subVBox.setSpacing(10);
            subVBox.getChildren().add(hBox);
            vBox.getChildren().add(subVBox);
            subVBoxes.add(subVBox);
        }

        for (int i = 0; i < infoList.size(); i++) {
            int finalI = i;
            AtomicBoolean isClosed = new AtomicBoolean(true);
            infoList.get(i).setOnAction(event -> {
                System.out.println(letter.get(finalI));
                TextArea moreInfo = new TextArea();
                moreInfo.setEditable(false);
                if (isClosed.get()) {
                    isClosed.set(false);
                    String info = "تعداد بازیکن: " + (numberOfPlayers.get(finalI) + 1) + "\n" +
                            "تعداد راند: " + numberOfRounds.get(finalI) + "\n" +
                            "حرف شروع: " + letter.get(finalI) + "\nفیلد های بازی: ";
                    for (Fields field: Fields.values()) {
                        if (chosenFields.get(finalI)[field.ordinal()]) {
                            info += field.name() + " ";
                        }
                    }
                    info += "\nنوع پایان بازی: " + (endsByTime.get(finalI) ? "پایان با زمان" : "اعلام پایان") +
                            "\n" + (time.get(finalI) == -1 ? "" : "زمان هر راند: " + time.get(finalI));
                    moreInfo.setText(info);
                    subVBoxes.get(finalI).getChildren().add(moreInfo);
                } else {
                    moreInfo.setVisible(false);
//                    try {
//                        primaryStage.close();
//                        start(primaryStage);
//                    } catch (Exception e) {e.printStackTrace();}
                }
            });
        }

        for (int i = 0; i < buttonsList.size(); i++) {
            int finalI = i;
            buttonsList.get(i).setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println(portsList.get(finalI));
                    primaryStage.close();
                    try {
                        // new Client(portsList.get(finalI));
                        new ClientGame(portsList.get(finalI)).start(new Stage());
                    } catch (Exception e) {e.printStackTrace();}
                }
            });
        }

        Button refresh = new Button("Refresh");
        vBox.getChildren().add(refresh);
        refresh.setOnAction(event -> {
            primaryStage.close();

            portsList.clear();
            numberOfPlayers.clear();
            numberOfRounds.clear();
            letter.clear();
            chosenFields.clear();
            endsByTime.clear();
            time.clear();
            try {
                start(primaryStage);
            } catch (Exception e) {e.printStackTrace();}
        });

        scrollPane.setContent(vBox);
        Scene scene = new Scene(scrollPane, 600, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("اسم فامیل");
        primaryStage.show();
    }
}
