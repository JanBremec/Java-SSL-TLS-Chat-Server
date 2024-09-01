# Java SSL/TLS Chat Server and Client
A straightforward chat server and client application in Java, leveraging SSL/TLS for secure communication. This project showcases the use of Java’s SSL/TLS APIs to enable encrypted interactions between a server and multiple clients, utilizing self-signed certificates for authentication.

## Table of Contents
  - Overview
  - Features
  - Prerequisites
  - Setup
  - Generating Keys and Certificates
  - Usage
  - License

## Overview
The project consist of:
 1. **ChatServer**: A multi-threaded server that listens for incoming client connections and relays messages between all connected clients.
 2. **ChatClient**: A client that connects to the server using SSL/TLS, sends messages, and receives messages from other clients.

## Features
  - Secure communication using SSL/TLS.
  - Multi-threaded server to handle multiple clients simultaneously.
  - Self-signed certificates for both server and client authentication.
  - Simple command-line interface.

## Prerequisites
Before running this project, make sure you have the following:
  - **Java Development Kit (JDK)**: Version 8 or higher.
  - **Keytool**: Comes with the JDK.

## Setup
  ### 1. Clone the repository:
    git clone https://github.com/JanBremec/Java-SSL-TLS-Chat-Server.git
    cd Java-SSL-TLS-Chat-Server

  ### 2. Compile the Java files:
    javac ChatServer.java ChatClient.java

## Generating Keys and Certificates
  To enable SSL/TLS communication, you need to create self-signed certificates and import them into Java KeyStores. Follow the steps below:
  (make sure your storepass/keypass matches passphrase in both client and server script)
  ### 1. Generate Server Key and Certificate:
    keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -keystore server.private -storetype JKS -storepass yourStorePass -keypass yourStorePass -validity 365 -dname "CN=Server, OU=YourOrg, O=YourCompany, L=YourCity, S=YourState, C=YourCountry"

### 2. Export Server Public Certificate: Export the server's public certificate to server.public:
    keytool -exportcert -alias server -keystore server.private -file server.crt -storepass yourStorePass
    keytool -import -alias server-cert -file server.crt -keystore server.public -storepass yourStorePass -noprompt

### 3. Generate Client Keystores:
    keytool -genkeypair -alias yourClientName -keyalg RSA -keysize 2048 -keystore yourClientName.private -storetype JKS -storepass yourStorePass -keypass yourStorePass -validity 365 -dname "CN=yourClientName, OU=YourOrg, O=YourCompany, L=YourCity, S=YourState, C=YourCountry"

### 4. Export Client Public Certificates (yourClientName.public): Export each client's public certificate and create a combined trust store.
    keytool -exportcert -alias yourClientName -keystore yourClientName.private -file yourClientName.crt -storepass yourStorePass
    keytool -import -alias yourClientName-cert -file yourClientName.crt -keystore client.public -storepass yourStorePass -noprompt

## Usage
  ### 1. Start the Chat Server:
      java ChatServer.java
      
  ### 2. Start Chat Client:
      java ChatClient.java
      
  - When prompted, enter the username that corresponds to the client's private key (e.g., "client" if the private key file is client.private).
  - After connecting, you can start typing messages in the client terminal.
  - All messages will be broadcast to all connected clients.
    
## Licence
  See the License for more details.
