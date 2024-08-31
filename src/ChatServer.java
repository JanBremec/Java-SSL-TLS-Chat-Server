import java.io.*;
import java.util.*;
import javax.net.ssl.*;
import java.security.*;

public class ChatServer {

	// Default server port
	protected int serverPort = 1234;
	// List to keep track of all connected client sockets
	protected List<SSLSocket> clients = new ArrayList<SSLSocket>();

	public static void main(String[] args) throws Exception {
		new ChatServer();
	}

	// Constructor to initialize the SSL server socket and start accepting clients
	public ChatServer() {
		SSLServerSocket serverSocket = null;

		try {
			String passphrase = "ChangeThis"; // Passphrase for key store files

			// Load the server's private key from the key store
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream("server.private"), passphrase.toCharArray());

			// Load the client's public key from the key store
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			clientKeyStore.load(new FileInputStream("client.public"), passphrase.toCharArray());

			// Initialize trust manager with the client's public key
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(clientKeyStore);

			// Initialize key manager with the server's private key
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(serverKeyStore, passphrase.toCharArray());

			// Set up SSL context with the initialized key and trust managers
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

			// Create SSL server socket factory and bind the server socket to the specified port
			SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			serverSocket = (SSLServerSocket) factory.createServerSocket(serverPort);
			serverSocket.setNeedClientAuth(true); // Require client authentication
			serverSocket.setEnabledCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});

		} catch (Exception e) {
			System.err.println("[system] could not create socket on port " + this.serverPort);
			e.printStackTrace(System.err);
			System.exit(1);
		}

		System.out.println("[system] listening ...");
		try {
			// Main loop to accept incoming client connections
			while (true) {
				SSLSocket newClientSocket = (SSLSocket) serverSocket.accept();

				// Perform SSL handshake with the new client
				newClientSocket.startHandshake();

				// Add the new client to the list of clients
				synchronized (this) {
					clients.add(newClientSocket);
				}

				// Start a new thread to handle communication with the new client
				ChatServerConnector conn = new ChatServerConnector(this, newClientSocket);
				conn.start();
			}
		} catch (Exception e) {
			System.err.println("[error] Accept failed.");
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// Close the server socket when the server is shutting down
		System.out.println("[system] closing server socket ...");
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	// Method to send a message to all connected clients
	public void sendToAllClients(String message) throws Exception {
		Iterator<SSLSocket> i = clients.iterator();
		while (i.hasNext()) {
			SSLSocket socket = (SSLSocket) i.next();
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(message);
			} catch (Exception e) {
				System.err.println("[system] could not send message to a client");
				e.printStackTrace(System.err);
			}
		}
	}

	// Method to remove a client from the list when disconnected
	public void removeClient(SSLSocket socket) {
		synchronized (this) {
			clients.remove(socket);
		}
	}
}

class ChatServerConnector extends Thread {
	private ChatServer server; // Reference to the main server
	private SSLSocket socket; // Client socket connection
	private String clientName; // The client's name extracted from the SSL certificate

	// Constructor to initialize the connector with the server and the client socket
	public ChatServerConnector(ChatServer server, SSLSocket socket) {
		this.server = server;
		this.socket = socket;
		try {
			// Attempt to retrieve and set the client's name from the SSL certificate
			this.clientName = ((SSLSocket) socket).getSession().getPeerPrincipal().getName();
			this.clientName = this.clientName.substring(3, this.clientName.length());
		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
		}
	}

	// Method to handle client communication
	public void run() {
		System.out.println("[system] connected with " + this.socket.getInetAddress().getHostName() + ":" + this.socket.getPort());

		DataInputStream in;
		try {
			in = new DataInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			System.err.println("[system] could not open input stream!");
			e.printStackTrace(System.err);
			this.server.removeClient(socket);
			return;
		}

		while (true) {
			String msg_received;
			try {
				// Read message from the client
				msg_received = in.readUTF();
			} catch (Exception e) {
				System.err.println("[system] there was a problem while reading message client on port " + this.socket.getPort() + ", removing client");
				e.printStackTrace(System.err);
				this.server.removeClient(this.socket);
				return;
			}

			// Ignore empty messages
			if (msg_received.length() == 0)
				continue;

			System.out.println("[Chat] [" + this.socket.getPort() + "] : " + msg_received);

			String msg_send = String.format("Client [%s] said: ", this.clientName) + msg_received;

			try {
				// Forward the received message to all connected clients
				this.server.sendToAllClients(msg_send);
			} catch (Exception e) {
				System.err.println("[system] there was a problem while sending the message to all clients");
				e.printStackTrace(System.err);
			}
		}
	}
}
