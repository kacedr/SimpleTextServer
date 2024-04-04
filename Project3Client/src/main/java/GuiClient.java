import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.tools.Tool;

/*
* todo: There should only be two scenes, one for choosing username, one for actual text convos, all other actions should
*  be handled through dialog boxes and or drop down menus etc.
* */

public class GuiClient extends Application{

	TextField c1, nameEnter;
	Button b1, b2, b3, b4, b5, b6, b7;
	Label l1, l2, l3, l4, l5;
	HashMap<String, Scene> sceneMap;
	VBox clientBox, usernameBox;
	HBox buttonBox;
	Client clientConnection;
	ListView<String> listItems2;

	// for label showing username and who message is being sent to
	StringProperty usName, groupORuser, nameOfGroupOrUser;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		clientConnection = new Client(data->{
				Platform.runLater(()->{listItems2.getItems().add(data.toString());
			});
		});
							
		clientConnection.start();

		listItems2 = new ListView<String>();

		// create a new message class for this user
		Message user = new Message();
		
		c1 = new TextField();

		b1 = new Button("Send");
		b1.setOnAction(e->{
			//
			clientConnection.send(c1.getText()); c1.clear();
		});

		b3 = new Button("Send All");
		b4 = new Button("Create Group");
		b5 = new Button("Members");
		b6 = new Button("Groups");

		// used for hover text
		Tooltip t1 = new Tooltip("Red Means You Are Sending To All");
		Tooltip t2 = new Tooltip("Create A New Group");
		Tooltip t3 = new Tooltip("View/Choose Members");
		Tooltip t4 = new Tooltip("View/Choose Groups");
		t1.setShowDelay(Duration.seconds(0.002));
		t2.setShowDelay(Duration.seconds(0.002));
		t3.setShowDelay(Duration.seconds(0.002));
		t4.setShowDelay(Duration.seconds(0.002));
		Tooltip.install(b3, t1);
		Tooltip.install(b4, t2);
		Tooltip.install(b5, t3);
		Tooltip.install(b6, t4);

		l2 = new Label("Funky Wunky Text Server");

		// these are used to update the l3 label
		StringProperty s1 = new SimpleStringProperty("NULL");
		StringProperty s2 = new SimpleStringProperty("NULL");
		StringProperty s3 = new SimpleStringProperty("NULL");

		// s1: username s2: is it a group, user, or whole server s3: group, or users name
		l3 = new Label();
		l3.textProperty().bind(
				// javaFX has no way to auto size text size, which is awesome, so short usernames please
				Bindings.concat("Username: ", s1, " Sending To: ", s2, " ", s3)
		);

		sceneMap = new HashMap<String, Scene>();

		// the server will hold all usernames, we need a way to contact the server to validate if a username is allowed
		l1 = new Label("Enter unique username");
		nameEnter = new TextField();
		b2 = new Button("Enter");

		sceneMap.put("username", createNameGui());
		sceneMap.put("client",  createClientGui());

		b2.setOnAction(e ->{
			// ry catch block to validate username
			try {
				// todo: check for duplicate usernames
				if (nameEnter.getText().isEmpty()) {
					showAlert("Must Enter A Username");
				} else {
					user.userName = nameEnter.getText();
					s1.setValue(user.userName);
					primaryStage.setScene(sceneMap.get("client"));
					primaryStage.centerOnScreen();
				}
			} catch (NumberFormatException f) {
				showAlert("Must Enter A Valid Username");
			}
		});

		b3.setOnAction(e->{
			// all the "Send All" button does is change the target sender to the whole server
			// user still has to click send button
			String getStyle = b3.getStyle();
			if (getStyle.contains("black")) {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
				s2.setValue("Whole Server");
				s3.setValue("");
			} else {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
				s2.setValue("Choose Destination");
				s3.setValue("");
			}
		});

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });


		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.setTitle("Client");
		primaryStage.show();
		
	}

	// simple error box
	private void showAlert(String msg) {
		// shows an alert box on the screen
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("");
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	public Scene createNameGui() {
		usernameBox = new VBox(10);
		usernameBox.setAlignment(Pos.CENTER);

		l1.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; -fx-text-fill: black; " +
				"-fx-font-size: 15px; -fx-font-weight: bold;");

		nameEnter.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
		nameEnter.setMaxWidth(200);

		b2.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

		usernameBox.getChildren().addAll(l1, nameEnter, b2);

        return new Scene(usernameBox, 400, 300);
	}
	
	public Scene createClientGui() {

		buttonBox = new HBox(10, b1, b3, b4, b5, b6);
		clientBox = new VBox(10, l2, l3, listItems2, c1, buttonBox);

		clientBox.setAlignment(Pos.CENTER);
		buttonBox.setAlignment(Pos.CENTER);

		// rainbow text lol
		l2.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; " +
				"-fx-font-size: 23px; -fx-font-weight: bold;" +
				"-fx-text-fill: linear-gradient(to left, violet, indigo, blue, green, yellow, orange, red);");

		// this will change
		l3.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; " +
				"-fx-font-size: 15px; -fx-font-weight: bold;" + "-fx-text-fill: red;");

		listItems2.setStyle("-fx-border-color: black;");

		c1.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");
		c1.setMaxWidth(600);

		b1.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b4.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
		b6.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");

		return new Scene(clientBox, 800, 600);
		
	}

}
