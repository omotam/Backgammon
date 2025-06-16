package backgammon;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

import backgammon.Message.Type;
import backgammon.Piece.PlayerColor;

public class Server extends Thread {
	public static final int LISTENING_PORT = 9877;
	static int connectCount = 0;
	static ArrayList<ObjectOutputStream> outPipes;
	ArrayList<ConnectionHandler> connections;
	ServerSocket server; // Listens for incoming connections.
	Socket connection; // For communication with the connecting program.
	public volatile ArrayList<Piece>[] board;
	ArrayList<Piece> bar;
	ArrayList<Piece> whiteHome;
	ArrayList<Piece> blackHome;
	boolean whiteTurn = true;
	static ArrayList<Integer> dice;

	public Server() {
		ServerSocket server; // Listens for incoming connections.
		Socket connection; // For communication with the connecting program.
		connections = new ArrayList<ConnectionHandler>();
		outPipes = new ArrayList<ObjectOutputStream>();
		board = new ArrayList[24]; // Stores all of the triangles
		bar = new ArrayList<Piece>();
		whiteHome = new ArrayList<Piece>();
		blackHome = new ArrayList<Piece>();
		this.initialize();
		this.start();
		/*
		 * Accept and process connections until we have 2 connections, or until some
		 * error occurs
		 */

	}

	public void run() {
		try {
			server = new ServerSocket(LISTENING_PORT);
			System.out.println("Listening on port " + LISTENING_PORT);
			while (connections.size() < 2) {
				// Accept next connection request and handle it.
				Socket s = server.accept();
				connections.add(new ConnectionHandler(s));

				System.out.println("accepted a connection");

			}
		} catch (Exception e) {
			System.out.println("Sorry, the server has shut down.");
			System.out.println("Error:  " + e);
			return;
		}
	}

	public void initialize() {

		for (int i = 0; i < 24; i++) {
			board[i] = new ArrayList<Piece>();
		}

		// Place all white pieces
		for (int i = 0; i < 5; i++) {
			board[5].add(new Piece(PlayerColor.WHITE));
		}
		for (int i = 0; i < 3; i++) {
			board[7].add(new Piece(PlayerColor.WHITE));
		}
		for (int i = 0; i < 5; i++) {
			board[12].add(new Piece(PlayerColor.WHITE));
		}

		for (int i = 0; i < 2; i++) {
			board[23].add(new Piece(PlayerColor.WHITE));
		}
		// Now all black pieces
		for (int i = 0; i < 5; i++) {
			board[18].add(new Piece(PlayerColor.BLACK));
		}
		for (int i = 0; i < 3; i++) {
			board[16].add(new Piece(PlayerColor.BLACK));
		}
		for (int i = 0; i < 5; i++) {
			board[11].add(new Piece(PlayerColor.BLACK));
		}
		for (int i = 0; i < 2; i++) {
			board[0].add(new Piece(PlayerColor.BLACK));
		}
	}

	public static ArrayList<Integer> roll() {
		dice = new ArrayList<Integer>();
		Random r = new Random();
		int roll1 = r.nextInt(5) + 1; // Result between 1 and 6
		int roll2 = r.nextInt(5) + 1;
		if (roll1 == roll2) { //If the result is a double, you get 4 moves
			for (int i = 0; i < 4; i++) {
				dice.add((Integer) roll1);
			}
		} else {
			dice.add((Integer) roll1);
			dice.add((Integer) roll2);
		}
		return dice;
	}

	public void move(int tri1, int tri2) {
		System.out.println("moving");
		int distance = Math.abs(tri1 - tri2);
		if (whiteTurn) {
			System.out.println("White turn");
			if (tri1 > tri2) { //White can only move towards their home (towards lower-number triangles)
				for (int rollIndex = 0; rollIndex < dice.size(); rollIndex++) {
					int i = dice.get(rollIndex);
					if (i == distance) {
						board[tri1].remove(0);
						board[tri2].add(new Piece(PlayerColor.WHITE));
						int count = 0;

						for (ArrayList<Piece> p : board) {
							System.out.println(count + " " + p.size());
							count++;
						}
						dice.remove(rollIndex);
						System.out.println("move is legal");
						for (ObjectOutputStream o : outPipes) {
							try {

								o.writeObject(new Message(board.clone()));
								o.flush();
								System.out.println("Board sent");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				}
			}
		}
	}

	private class ConnectionHandler extends Thread {
		Socket client;
		int id;
		ObjectOutputStream stream;

		ConnectionHandler(Socket socket) {
			client = socket;
			id = connectCount;
			connectCount++;
			try {
				stream = new ObjectOutputStream(client.getOutputStream());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outPipes.add(stream);
			this.start();
		}

		public void run() {
			String clientAddress = client.getInetAddress().toString();
			System.out.println("Connection established with " + clientAddress);
			try {
				ObjectInputStream fromClient = new ObjectInputStream(client.getInputStream());
				for (ObjectOutputStream o : outPipes) {
					o.writeObject(new Message(board));
				}
				while (true) {

					Object message;
					try {
						message = fromClient.readObject();
						System.out.println("got something from client");
						if (message instanceof Message) {
							if (((Message) message).getT() == Type.EXIT) {
								for (ObjectOutputStream o : outPipes) {
									o.writeObject("-- user" + id + "disconnected--");
								}
								fromClient.close();

								stream.close();
								outPipes.remove(stream);
								client.close();
								break;
							} else if (((Message) message).getT() == Type.ROLL_REQUEST) {
								ArrayList<Integer> result = roll();
								System.out.println("rolling");
								for (ObjectOutputStream o : outPipes) {
									o.writeObject(new Message(result));
								}
							} else if (((Message) message).getT() == Type.MOVE) {
								move(((Message) message).getTri1(), ((Message) message).getTri2());
							}

						} else {
							System.out.println("user" + id + ": " + message);
						}
						// your code to send messages goes here.
					} catch (Exception e) {
						System.out.println("Error on connection with: " + clientAddress + ": " + e);
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Error on line 215");
				e1.printStackTrace();

			}
		}
	}

	public static void main(String[] args) {

		new Server();
	}

}
