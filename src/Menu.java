import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;

public class Menu extends Application {
    public static final int NUMBER_OF_FIELDS = 11;

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        Label gameTitle = new Label("اسم فامیل");
        gameTitle.setStyle("-fx-font-family: 'B Nazanin'; -fx-font-size: 33; font-weight: bold;");
        GridPane.setHalignment(gameTitle, HPos.CENTER);
        gridPane.setVgap(60);
        gridPane.getChildren().add(gameTitle);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(20);

        Button createServerButton = new Button("Create Server");
        Button joinServerButton = new Button("Join Server");
        createServerButton.setContentDisplay(ContentDisplay.TOP);
        createServerButton.setGraphic(new ImageView("assets\\createServer.png"));
        createServerButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-pref-width: 100px; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-border-color: rgba(0,0,0,0.54);");
        createServerButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FlowPane flowPane = new FlowPane(Orientation.VERTICAL, 10, 10);
                flowPane.setAlignment(Pos.CENTER);
                flowPane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                flowPane.setPrefWrapLength(100);
                Label gameOptionsLabel = new Label("تنظیمات بازی");
                flowPane.getChildren().add(gameOptionsLabel);
                TextField numberOfPlayersTextField = new TextField();
                numberOfPlayersTextField.setPromptText("تعداد بازیکن");
                TextField numberOfRoundsTextField = new TextField();
                numberOfRoundsTextField.setPromptText("تعداد دور");
                TextField letterTextField = new TextField();
                letterTextField.setPromptText("حرف اول");
                flowPane.getChildren().addAll(numberOfPlayersTextField, numberOfRoundsTextField, letterTextField);
                CheckBox[] fieldsCheckbox = new CheckBox[11];
                for (int i = 0; i < NUMBER_OF_FIELDS; i += 2) {
                    if (i == NUMBER_OF_FIELDS - 1)
                        flowPane.getChildren().add(fieldsCheckbox[i] = new CheckBox(Fields.values()[i].name()));
                    else {
                        HBox hBox1 = new HBox();
                        hBox1.setSpacing(100);
                        fieldsCheckbox[i] = new CheckBox(Fields.values()[i].name());
                        fieldsCheckbox[i+1] = new CheckBox(Fields.values()[i+1].name());
                        hBox1.getChildren().addAll(fieldsCheckbox[i], fieldsCheckbox[i+1]);
                        flowPane.getChildren().add(hBox1);
                    }
                }

                RadioButton finishEnd = new RadioButton("اتمام بازی وقتی کسی تمام کرد");
                RadioButton timerEnd = new RadioButton("اتمام بازی با زمان");
                ToggleGroup toggleGroup = new ToggleGroup();
                finishEnd.setToggleGroup(toggleGroup);
                timerEnd.setToggleGroup(toggleGroup);
                flowPane.getChildren().add(finishEnd);
                HBox hBox1 = new HBox();
                hBox1.setSpacing(10);
                hBox1.getChildren().add(timerEnd);

                TextField timerTextField = new TextField();
                timerTextField.setPromptText("زمان بازی");
                finishEnd.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        hBox1.getChildren().remove(timerTextField);
                    }
                });

                timerEnd.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        hBox1.getChildren().add(timerTextField);
                    }
                });
                flowPane.getChildren().add(hBox1);

                HBox doneButtonHBOX = new HBox();
                doneButtonHBOX.setSpacing(10);
                Button doneButton = new Button("Done");
                doneButtonHBOX.getChildren().add(doneButton);
                flowPane.getChildren().add(doneButtonHBOX);
                Label errorLabel = new Label("حداقل 5 فیلد انتخاب کنید");
                doneButton.addEventFilter(ActionEvent.ACTION, event1 -> {
                    int countFields = 0;
                    for (int i = 0; i < NUMBER_OF_FIELDS; i++) {
                        if (fieldsCheckbox[i].isSelected()) {
                            countFields++;
                        }
                    }
                    if (countFields < 5) {
                        event1.consume();
                        doneButtonHBOX.getChildren().remove(errorLabel);
                        doneButton.setStyle("-fx-border-color: red; -fx-border-radius: 5");
                        doneButtonHBOX.getChildren().add(errorLabel);
                    }
                });
                doneButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        int nop = Integer.parseInt(numberOfPlayersTextField.getText());
                        int nor = Integer.parseInt(numberOfRoundsTextField.getText());
                        char ltr = letterTextField.getText().charAt(0);

                        boolean[] chosenFld = new boolean[NUMBER_OF_FIELDS];
                        for (int i = 0; i < NUMBER_OF_FIELDS; i++) {
                            chosenFld[i] = fieldsCheckbox[i].isSelected();
                        }

                        boolean endByTime = timerEnd.isSelected();
                        int time = -1;
                        if (endByTime)
                            time = Integer.parseInt(timerTextField.getText());

//                        Server server = null;
//                        try {
//                            server = new Server(nop, nor, ltr, chosenFld, endByTime, time);
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                        System.out.println(server.toString());
                        try {
                            new ServerGame(nop, nor, ltr, chosenFld, endByTime, time).start(new Stage());
                        } catch (Exception e) {e.printStackTrace();}

                        primaryStage.close(); // should open a loading page
                    }
                });

                Scene gameOptionsScene = new Scene(flowPane, 600, 400);
                primaryStage.setScene(gameOptionsScene);
            }
        });
        joinServerButton.setContentDisplay(ContentDisplay.TOP);
        joinServerButton.setGraphic(new ImageView("assets\\joinServer.png"));
        joinServerButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-pref-width: 100px; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-border-color: rgba(0,0,0,0.54);");
        joinServerButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.close();
                try {
                    new ChooseServer().start(new Stage());
                } catch (Exception e) {e.printStackTrace();}
            }
        });

        hBox.getChildren().add(createServerButton);
        hBox.getChildren().add(joinServerButton);
        gridPane.add(hBox, 0, 1, 1, 1);

        Scene scene = new Scene(gridPane, 600, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("اسم فامیل");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}