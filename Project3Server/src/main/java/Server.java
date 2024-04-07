import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

public class Server {

	int count = 1; // Counter for clients, used for any purpose you might have beyond identification
	HashMap<String, ClientThread> clients = new HashMap<>(); // Maps a username to each client thread
	private Consumer<Serializable> callback; // Callback for UI or logging

	Server(Consumer<Serializable> call) {
		callback = call;
		new TheServer().start(); // Start the server thread
	}

	class TheServer extends Thread {
		public void run() {
			try (ServerSocket serverSocket = new ServerSocket(5555)) {
				System.out.println("Server is waiting for a client!");

				while (true) {
					Socket clientSocket = serverSocket.accept(); // Accept a client connection
					new ClientThread(clientSocket, count++).start(); // Create and start a client thread
				}
			} catch (Exception e) {
				callback.accept("Server socket did not launch: " + e.getMessage());
			}
		}
	}

	class ClientThread extends Thread {
		Socket connection;
		int count;
		String userName;
		ObjectInputStream in;
		ObjectOutputStream out;

		ClientThread(Socket connection, int count) {
			this.connection = connection;
			this.count = count;
		}

		public void run() {
			try {
				// Initialize ObjectOutputStream first and flush it
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();

				// Initialize ObjectInputStream after the ObjectOutputStream
				in = new ObjectInputStream(connection.getInputStream());

				boolean userNameSet = false;
				while (!userNameSet) {
					// Process the initial message for username validation
					Message initialMessage = (Message) in.readObject();
					userName = initialMessage.getUserName();

					if (clients.containsKey(userName)) {
						// If username is taken, inform the client without closing the connection
						Message error = new Message();
						error.setMessage("ERROR USERNAME TAKEN");
						sendMessage(error);
						// Do not close the connection; wait for the client to send a new username
					} else {
						// Username is good, exit the loop
						Message success = new Message();
						success.setMessage("USERNAME GOOD");
						sendMessage(success);
						clients.put(userName, this); // Add this client thread to the map
						callback.accept(userName + " has connected.");
						userNameSet = true; // Exit the loop
					}
				}

				// Handle further communication after a unique username has been set
				while (true) {
					Message message = (Message) in.readObject();
					// Use callback to send message data to GUI for display or further processing
					callback.accept(userName + ": " + message.getMessage());
					// Echo the message back to the client or handle as required
					sendMessage(message);
				}
			} catch (Exception e) {
				callback.accept("Client " + userName + " disconnected.");
				clients.remove(userName); // Remove this client from the map
			} finally {
				try {
					if (connection != null) {
						connection.close(); // Ensure the connection is closed on exit
					}
				} catch (Exception e) {}
			}
		}

		// Utility method to send messages
		void sendMessage(Message message) {
			try {
				out.writeObject(message);
				out.flush();
			} catch (Exception e) {
				System.out.println("Error sending message to " + userName + ": " + e.getMessage());
			}
		}
	}

}
