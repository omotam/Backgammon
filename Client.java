package backgammon;

import java.awt.Color;
import java.awt.Label;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import backgammon.Message.Type;
import backgammon.Piece.PlayerColor;

import javax.swing.*;

public class Client implements MouseListener {

	Polygon[] triangles = new Polygon[24];

	JFrame f;
	int right = 900;
	int bottom = 600;
	ObjectInputStream fromServer;
	InetAddress host;
	Socket socket;
	ObjectOutputStream fromClient;
	boolean connected;
	volatile ArrayList<Integer> rollResult;
	ArrayList<Label> dice;
	ArrayList<Piece>[] board = new ArrayList[24];

	int tri1 = -1;
	int tri2 = -1;
	JButton rollButton;

	public Client() {
		for (ArrayList<Piece> t : board) {
			t = new ArrayList<Piece>();
		}
		f = new JFrame("Backgammon");
		rollButton = new JButton("Roll");
		try {
			host = InetAddress.getLocalHost();
			socket = new Socket(host.getHostName(), 9877);
			fromClient = new ObjectOutputStream(socket.getOutputStream());
			fromServer = new ObjectInputStream(socket.getInputStream());
			connected = true;

			@SuppressWarnings("unused")
			ReadThread r = new ReadThread();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			connected = false;
			e.printStackTrace();
		}
		f.setBackground(new Color(255, 248, 220));

		rollButton.setBounds(400, 250, 100, 50);
		rollButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {

				System.out.println("Roll request sent");
				send(new Message(Type.ROLL_REQUEST));

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}
		});

		f.setSize(right, bottom);
		f.setResizable(false);
		f.addMouseListener(this);
		int rightX = right - 50;
		for (int i = 0; i < 6; i++) {
			triangles[i] = new Polygon(new int[] { rightX - 50, rightX - 25, rightX },
					new int[] { bottom, bottom - 200, bottom }, 3);
			rightX -= 65;

		}
		Rectangle bar = new Rectangle(rightX - 50, 0, 60, bottom);
		rightX -= 65;

		for (int i = 6; i < 12; i++) {
			triangles[i] = new Polygon(new int[] { rightX - 50, rightX - 25, rightX },
					new int[] { bottom, bottom - 200, bottom }, 3);
			rightX -= 65;

		}
		rightX += 10;

		for (int i = 12; i < 24; i++) {
			triangles[i] = new Polygon(new int[] { rightX + 50, rightX + 25, rightX }, new int[] { 0, 200, 0 }, 3);
			rightX += 65;

			if (i == 17) {
				rightX += 65;
			}

		}

		JPanel panel = new JPanel() {

			public void paintComponent(Graphics g) {
				System.out.println("Screen drawn!!");
				Graphics2D g2d = (Graphics2D) g;
				for (int i = 0; i < 24; i++) {
					if (i % 2 == 0) {
						g.setColor(new Color(135, 14, 14));
					} else {
						g.setColor(Color.BLACK);
					}
					g.fillPolygon(triangles[i]);

					addPieces(i, g2d);
				}

				g2d.setColor(new Color(160, 82, 45));
				g2d.fill(bar);

			}

		};

		panel.setPreferredSize(new Dimension(right, bottom));
		panel.add(rollButton);
		dice = new ArrayList<Label>();
		JPanel holder = new JPanel();
		holder.add(panel);
		holder.setBackground(new Color(237, 216, 197));

		for (int i = 0; i < 4; i++) {
			Label l = new Label();

			dice.add(l);
			holder.add(l);
		}

		f.getContentPane().add(holder);

		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public void addPieces(int index, Graphics2D g) {
		// System.out.println("Drawing pieces");
		ArrayList<Piece> triangle = board[index];
		boolean bottomRow;
		int centerY;
		int centerX = (int) (triangles[index].getBounds().getCenterX() * 2 + 50) / 2;
		if (index < 12) {
			centerY = bottom;
			bottomRow = true;
		} else {
			centerY = 50;
			bottomRow = false;
		}

		for (Piece p : triangle) {
			Color main = new Color(255, 255, 240);
			Color outline = new Color(170, 170, 170);
			if (p.color == PlayerColor.BLACK) {
				main = new Color(100, 50, 0);
				outline = new Color(51, 24, 0);
			}
			g.setColor(outline);
			Ellipse2D current = new Ellipse2D.Double(centerX - 50, centerY - 50, 50, 50);
			g.fill(current);
			g.setColor(main);
			current = new Ellipse2D.Double(centerX - 50, centerY - 50, 45, 45);
			g.fill(current);
			if (bottomRow) {
				centerY -= 50;
			} else {
				centerY += 50;
			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		for (int i = 0; i < 24; i++) {
			if (triangles[i].contains(e.getX(), e.getY())) {
				if (tri1 == -1) {
				    if (board[i] != null && !board[i].isEmpty()) {
				        tri1 = i;
				        System.out.println("First triangle:" + tri1);
				    } else {
				        System.out.println("Selected triangle " + i + " is empty. Please select a triangle with pieces.");
				    }

				} else {
					tri2 = i;
					System.out.println("Second triangle:" + tri2);

					send(new Message(tri1, tri2));
					System.out.println("message sent");
					tri1 = -1;
					tri2 = -1;
				}
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void send(Message m) {
		try {
			fromClient.writeObject(m);
			fromClient.reset();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		Client g = new Client();
	}

	private class ReadThread extends Thread {
		public ReadThread() {
			this.start();
		}

		public void run() {
			while (connected) {
				try {
					Object message = fromServer.readObject();
					if (((Message) message).getT() == Message.Type.ROLL_RESULT) {
						System.out.println(((((Message) message).getRoll())).toString());
						rollResult = (((Message) message).getRoll());
						for (int i = 0; i < 4; i++) {

							if (rollResult != null && rollResult.size() > i) {
								dice.get(i).setText(Integer.toString(rollResult.get(i)));
							} else {
								dice.get(i).setText(" ");
							}
						}
						rollButton.setVisible(false);

					} else if (((Message) message).getT() == Message.Type.BOARD) {
						board = ((Message) message).getBoard();
						int count = 0;

						for (ArrayList<Piece> p : board) {
							System.out.println(count + " " + p.size());
							count++;
						}
						// board[5].remove(0);
						System.out.println("New board state recieved");

						f.repaint();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("didn't get the roll");
				}

			}
		}
	}

}
