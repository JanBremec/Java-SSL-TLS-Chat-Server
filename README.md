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
  - **OpenSSL**: For generating keys and certificates.
  - **Keytool**: Comes with the JDK.

## Setup
  ### 1. Clone the repository:
    git clone https://github.com/JanBremec/Java-SSL-TLS-Chat-Server.git
    cd Java-SSL-TLS-Chat-Server

  ### 2. Compile the Java files:
    javac ChatServer.java ChatClient.java

## Generating Keys and Certificates
  To enable SSL/TLS communication, you need to create self-signed certificates and import them into Java KeyStores. Follow the steps below:

  ### 1. Generate Server Key and Certificate:
    openssl req -new -newkey rsa:2048 -days 365 -nodes -sha256 -x509 -keyout serverkey.pem -out server.pem
