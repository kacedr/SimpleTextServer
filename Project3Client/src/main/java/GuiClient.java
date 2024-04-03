import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/*
* todo: There should only be two scenes, one for choosing username, one for actual text convos, all other actions should
*  be handled through dialog boxes and or drop down menus etc.
* */

public class GuiClient extends Application{

	TextField c1, nameEnter;
	Button b1, b2, b3, b4, b5, b6, b7;
	Label l1, l2;
	HashMap<String, Scene> sceneMap;
	VBox clientBox, usernameBox;
	HBox buttonBox;
	Client clientConnection;
	ListView<String> listItems2;
	
	
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
		
		c1 = new TextField();

		b1 = new Button("Send");
		b1.setOnAction(e->{clientConnection.send(c1.getText()); c1.clear();});

		b3 = new Button("Send All");

		b4 = new Button("Create Group");

		b5 = new Button("Members");

		b6 = new Button("Groups");

		l2 = new Label("Funky Wunky Text Server");
		
		sceneMap = new HashMap<String, Scene>();

		// the server will hold all usernames, we need a way to contact the server to validate if a username is allowed
		l1 = new Label("Enter unique username");
		nameEnter = new TextField();
		b2 = new Button("Enter");

		sceneMap.put("username", createNameGui());
		sceneMap.put("client",  createClientGui());

		b2.setOnAction(e ->{
			// change this to a try catch block to validate username
			nameEnter.getText();
			primaryStage.setScene(sceneMap.get("client"));
			primaryStage.centerOnScreen();
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
		clientBox = new VBox(10, l2, listItems2, c1, buttonBox);

		clientBox.setAlignment(Pos.CENTER);
		buttonBox.setAlignment(Pos.CENTER);

		l2.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; -fx-text-fill: black; " +
				"-fx-font-size: 23px; -fx-font-weight: bold;");

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
