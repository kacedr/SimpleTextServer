import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class GuiClient extends Application{

	TextField c1, nameEnter;
	Button b1, b2, b3, b4, b5, b6, b7;
	Label l1, l2, l3, l4, l5;
	HashMap<String, Scene> sceneMap;
	VBox clientBox, usernameBox;
	HBox buttonBox;
	Client clientConnection;
	ListView<String> listItems2;
	ContextMenu usernameMenu;

	// for label showing username and who message is being sent to
	String userName;
	Boolean sendAll = false;
	Boolean whisper = false;

	// used to log the username the message is being sent to
	String usernameToSendTo;

	ArrayList<String> allUsers = new ArrayList<>();

	public static void main(String[] args) {
		launch(args);
	}

	// todo: need to go through this entire code and fix all the flags for message class, the flags are causing
	// 	weird behavior when trying to whisper i think they are all fucked up
	@Override
	public void start(Stage primaryStage) throws Exception {
		// create a new message class for this user
		allUsers.add("null");
		clientConnection = new Client(data -> {
			Platform.runLater(() -> {
				if ("USERNAME TAKEN".equals(data.toString())) {
					showAlert("Username is already taken. Please choose a different one.");
				}
				else if ("USERNAME GOOD".equals(data.toString())) {
					primaryStage.setScene(sceneMap.get("client"));
					primaryStage.centerOnScreen();
				}
				else {
					// Assuming data is a Message object for normal flow
					Message incomingMessage = (Message) data;

					// If it's for the whole server from a user
					if (incomingMessage.getIsSendAll()) {
						String displayText = "[GLOBAL] " + incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();
					}
					// if it's a whisper
					else if (incomingMessage.getIsWhisper()) {
						String displayText = "[WHISPER] " + incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();
					}
					// if it's a server message
					else if (incomingMessage.getIsServer()) {
						String displayText = incomingMessage.getUserName() + ": " + incomingMessage.getMessage();
						listItems2.getItems().add(displayText);
						listItems2.refresh();

						allUsers.clear();
						allUsers.addAll(incomingMessage.getUsers());
						updateUserMenu();

						// for making text red and bold for server messages
						listItems2.setCellFactory(lv -> new ListCell<String>() {
							@Override
							protected void updateItem(String item, boolean empty) {
								super.updateItem(item, empty);
								if (empty || item == null) {
									setText(null);
									setGraphic(null);
								} else {
									setText(item);
									if (item.startsWith("[SERVER]")) {
										// Red and bold for SERVER messages
										setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else if (item.startsWith("[GLOBAL]")) {
										// Yellow for GLOBAL messages
										setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else if (item.startsWith("[WHISPER]")) {
										// Light blue for WHISPER messages
										setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-family: 'Constantia';");
									} else {
										// Default style for any other type of message
										setStyle("-fx-text-fill: black; fx-font-weight: bold; -fx-font-family: 'Constantia';");
									}
								}
							}
						});
					}
				}
			});
		});

		clientConnection.start();

		listItems2 = new ListView<String>();

		c1 = new TextField();

		b1 = new Button("Send");

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

		// this is the only button that sends messages
		b1.setOnAction(e->{
			if (!sendAll && !whisper) {
				showAlert("Must Choose Destination");
			}
			else {
				Message messageToSend = new Message();
				messageToSend.setMessage(c1.getText());
				messageToSend.setUserName(userName);

				// flag setting
				messageToSend.setIsNewUser(false); // Since it's not a new user registration message
				messageToSend.setIsServer(false); // not a server message
				messageToSend.setIsDeletedUser(false); // user still exists if its sending messages
				messageToSend.setIsWhisper(whisper); // decided through choosing a user
				messageToSend.setUserNameToSendTo(usernameToSendTo); // if not a whisper it is null
				messageToSend.setIsSendAll(sendAll); // decided through send all button

				// Debugging
				System.out.println("Whisper: " + messageToSend.getIsWhisper());
				System.out.println(messageToSend.getUserNameToSendTo());
				System.out.println("SendAll: " + messageToSend.getIsSendAll());

				clientConnection.send(messageToSend);
				c1.clear();
			}
		});

		/// sets username but checks with server that username is not taken
		b2.setOnAction(e ->{
			newUserEnter();
			s1.setValue(userName);
		});

		// all the "Send All" button does is change the target sender to the whole server
		// user still has to click send button
		b3.setOnAction(e->{
			String getStyle = b3.getStyle();
			if (getStyle.contains("black")) {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
				b5.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
				s2.setValue("Whole Server");
				s3.setValue("");
				usernameToSendTo = "";
				whisper = false;
				sendAll = true;
			} else {
				b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
				s2.setValue("Choose Destination");
				s3.setValue("");
				usernameToSendTo = "";
				whisper = false;
				sendAll = false;
			}
		});

		// for listing users, userMenu is updated everytime a [SERVER] message is sent over
		usernameMenu = new ContextMenu();
		updateUserMenu();

		// work around to get the usernameMenu to open upwards, very efficient! (joke)
		b5.setOnAction(event -> {
			usernameMenu.show(b5, 0, 0);
			Platform.runLater(() -> {
				double menuHeight = usernameMenu.getHeight();
				double posX = b5.localToScreen(b5.getBoundsInLocal()).getMinX();
				double posY = b5.localToScreen(b5.getBoundsInLocal()).getMinY() - menuHeight;
				usernameMenu.hide();
				usernameMenu.show(b5, posX, posY);
			});
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

	private void updateUserMenu() {
		usernameMenu.getItems().clear();

		for (String username : allUsers) {
			// do not display the own user
			if (!username.equals(userName)){
				Label label = new Label(username);
				Tooltip tooltip = new Tooltip("Whisper to " + username);
				tooltip.setShowDelay(Duration.seconds(0.002));
				Tooltip.install(label, tooltip);

				CustomMenuItem menuItem = new CustomMenuItem(label, false);
				menuItem.setOnAction(e -> {
					usernameToSendTo = username;
					usernameMenu.hide();
					b3.setStyle("-fx-cursor: hand; -fx-background-color: black; -fx-text-fill: white;");
					b5.setStyle("-fx-cursor: hand; -fx-background-color: red; -fx-text-fill: white;");
					sendAll = false;
					whisper = true;
				});

				usernameMenu.getItems().add(menuItem);
			}
		}
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

	private void newUserEnter() {
		// try catch block to validate username
		try {
			if (nameEnter.getText().isEmpty()) {
				showAlert("Must Enter A Username");
			} else {
				Message user = new Message();
				user.setUserName(nameEnter.getText());
				userName = nameEnter.getText();
				user.setIsNewUser(true);
				clientConnection.send(user);
			}
		} catch (NumberFormatException f) {
			showAlert("Must Enter A Valid Username");
		}
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

		l2.setStyle("-fx-cursor: not-allowed; -fx-font-family: 'Constantia'; " +
				"-fx-font-size: 23px; -fx-font-weight: bold;" +
				"-fx-text-fill: linear-gradient(to left, black);");

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
