
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameView extends JFrame implements ActionListener {
	GameController control;
	GameModel model;
	private GamePanel panel;

	public GameView(GameController c, GameModel m) throws Exception {
		this.control = c;
		this.model = m;

		// Make the window of the game
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("");
		this.setSize(1200, 500);
		this.panel = new GamePanel();
		this.panel.addMouseListener(control);
		this.getContentPane().add(this.panel);
		this.setVisible(true);

	}

	public void actionPerformed(ActionEvent evt) {
		repaint(); // indirectly calls GamePanel.paintComponent() method
	}

	class GamePanel extends JPanel {
		public static final int FLAGIMGHEIGHT = 25;

		Image robotImg;

		GamePanel() throws Exception {
			this.robotImg = ImageIO.read(new File("robot.png"));
		}

		void drawTerrainMap(Graphics g) {
			byte[] terrain = model.getTerrain();
			int posBlue = 0;
			int posRed = (60 * 60 - 1) * 4;
			for (int y = 0; y < 60; y++) {
				for (int x = 0; x < 60; x++) {
					int bb = terrain[posBlue + 1] & 0xff;
					int gg = terrain[posBlue + 2] & 0xff;
					int rr = terrain[posBlue + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posBlue += 4;
				}
				for (int x = 60; x < 120; x++) {
					int bb = terrain[posRed + 1] & 0xff;
					int gg = terrain[posRed + 2] & 0xff;
					int rr = terrain[posRed + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posRed -= 4;
				}
			}
		}

		void drawRobots(Graphics g) {
			ArrayList<GameModel.Robot> robots = model.getRobots();
			for (int i = 0; i < robots.size(); i++) {

				// Draw the robot image
				GameModel.Robot s = robots.get(i);
				g.drawImage(robotImg, (int) s.x - 12, (int) s.y - 32, null);
			}
		}

		public void paintComponent(Graphics g) {

			// Provide the robots a chance to make decisions about what to do
			if (!control.update()) {
				// Closes this window
				this.dispatchEvent(new WindowEvent(GameView.this, WindowEvent.WINDOW_CLOSING));

			}

			// Draw the view for the game
			drawTerrainMap(g);
			drawRobots(g);
			control.agent.drawPlan(g, model);
		}
	}
}
