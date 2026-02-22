package client;

import common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 9999;

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean connected = false;

	public boolean connect() {
		try {
			socket = new Socket(SERVER_HOST, SERVER_PORT);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			connected = true;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void sendMessage(String message) {
		if (connected && out != null) {
			out.println(message);
		}
	}

	public String receiveMessage() throws IOException {
		if (connected && in != null) {
			return in.readLine();
		}
		return null;
	}

	public void disconnect() {
		try {
			if (connected) {
				sendMessage(Protocol.DISCONNECT);
				if (in != null) in.close();
				if (out != null) out.close();
				if (socket != null) socket.close();
				connected = false;
			}
		} catch (IOException e) {
			// ignore disconnect errors
		}
	}

	public boolean isConnected() {
		return connected;
	}
}
