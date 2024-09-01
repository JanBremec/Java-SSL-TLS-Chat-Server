import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Scanner;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ChatClient extends Thread {
	// Default server port
	protected int serverPort = 1234;

	public static void main(String[] args) throws Exception {
		new ChatClient();
	}

	// Constructor to initialize the chat client and establish a connection to the server
	public ChatClient() throws Exception {
		SSLSocket socket = null;
		DataInputStream in = null;
		DataOutputStream out = null;

		try {
			System.out.println("[system] connecting to chat server ...");

			// Prompt for and retrieve the client's username
			Scanner sc = new Scanner(System.in);
			System.out.print("Username: ");
			String clientName = sc.nextLine();
			System.out.println(clientName);

			// Define the passphrase used for loading key stores
			String phrase = "yourStorePass";

			// Load the server's public key from the key store
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream("server.public"), phrase.toCharArray());

			// Load the client's private key from their specific key store file
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			try {
				clientKeyStore.load(new FileInputStream(String.format("%s.private", clientName)), phrase.toCharArray());
			} catch (Exception e) {
				System.err.println("Username does not exist. Please create it.");
				e.printStackTrace(System.err);
				System.exit(1);
			}

			// Initialize trust manager with the server's public key
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(serverKeyStore);

			// Initialize key manager with the client's private key
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(clientKeyStore, phrase.toCharArray());

			// Set up SSL context with initialized key and trust managers
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

			// Create SSL socket factory and connect to the chat server
			SSLSocketFactory sf = sslContext.getSocketFactory();
			socket = (SSLSocket) sf.createSocket("localhost", serverPort);
			socket.setEnabledCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});
			socket.startHandshake();

			// Open input and output streams for communication with the server
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("[system] connected");

			// Start a thread to handle incoming messages from the server
			ChatClientMessageReceiver message_receiver = new ChatClientMessageReceiver(in);
			message_receiver.start();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// Read user input from the console and send it to the server
		BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		while ((userInput = std_in.readLine()) != null) {
			this.sendMessage(userInput, out);
		}

		// Clean up and close all resources
		out.close();
		in.close();
		std_in.close();
		socket.close();
	}

	// Method to send a message to the server
	private void sendMessage(String message, DataOutputStream out) {
		try {
			out.writeUTF(message);
			out.flush();
		} catch (IOException e) {
			System.err.println("[system] could not send message");
			e.printStackTrace(System.err);
		}
	}
}

class ChatClientMessageReceiver extends Thread {
	private DataInputStream in;

	// Constructor to initialize the message receiver with input stream
	public ChatClientMessageReceiver(DataInputStream in) {
		this.in = in;
	}

	// Method to listen for and print messages received from the server
	public void run() {
		try {
			String message;
			while ((message = this.in.readUTF()) != null) {
				System.out.println("[Chat] " + message);
			}
		} catch (Exception e) {
			System.err.println("[system] could not read message");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
