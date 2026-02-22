package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QuizServer {
	private static final int PORT = 9999;
	private ServerSocket serverSocket;
	private boolean running = false;

	public QuizServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			running = true;
			System.out.println("===========================================");
			System.out.println("   Quiz Server Started on Port " + PORT);
			System.out.println("===========================================");
		} catch (IOException e) {
			System.err.println("Error starting server: " + e.getMessage());
			System.exit(1);
		}
	}

	public void start() {
		if (!DatabaseConnection.testConnection()) {
			System.err.println("Failed to connect to database. Please check database configuration.");
			System.err.println("Make sure MySQL is running and quiz_system database is created.");
			return;
		}

		System.out.println("Waiting for client connections...\n");

		while (running) {
			try {
				Socket clientSocket = serverSocket.accept();
				ClientHandler handler = new ClientHandler(clientSocket);
				Thread clientThread = new Thread(handler);
				clientThread.start();
			} catch (IOException e) {
				if (running) {
					System.err.println("Error accepting connection: " + e.getMessage());
				}
			}
		}
	}

	public void stop() {
		running = false;
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			DatabaseConnection.closeConnection();
			System.out.println("Server stopped.");
		} catch (IOException e) {
			System.err.println("Error stopping server: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		QuizServer server = new QuizServer();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down server...");
			server.stop();
		}));

		server.start();
	}
}
