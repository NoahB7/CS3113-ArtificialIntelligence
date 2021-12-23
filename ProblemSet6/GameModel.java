
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

class GameModel {
	// just a small number
	public static final float EPSI = 0.0001f;

	// Max horizontal screen position with a minimum of 0.
	public static final float XMAXIMUM = 1200.0f - EPSI;

	// Max vertical screen position with a minimum of 0.
	public static final float YMAXIMUM = 600.0f - EPSI;

	private GameController controller;
	private byte[] terrain;
	private ArrayList<Robot> robots;

	GameModel(GameController c) {
		this.controller = c;
	}

	void initializeGame() throws Exception {
		BufferedImage img = ImageIO.read(new File("terrain.png"));
		if (img.getWidth() != 60 || img.getHeight() != 60)
			throw new Exception("The terrain image is expected to have the dimensions of 60x60 pixels");
		terrain = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		robots = new ArrayList<Robot>();
		robots.add(new Robot(100, 100));
	}

	// These methods are for internal use only. They are not useful to the game
	// agent.
	byte[] getTerrain() {
		return this.terrain;
	}

	ArrayList<Robot> getRobots() {
		return this.robots;
	}

	void update() {
		// Update the agents
		for (int i = 0; i < robots.size(); i++)
			robots.get(i).update();
	}

	// 0 <= x < MAP WIDTH
	// 0 <= y < MAP HEIGHT
	float getSpeedOfTravel(float x, float y) {
		int xx = (int) (x * 0.1f);
		int yy = (int) (y * 0.1f);
		if (xx >= 60) {
			xx = 119 - xx;
			yy = 59 - yy;
		}
		int pos = 4 * (60 * yy + xx);
		return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
	}

	GameController getController() {
		return controller;
	}

	float getX() {
		return robots.get(0).x;
	}

	float getY() {
		return robots.get(0).y;
	}

	float getDestXValue() {
		return robots.get(0).xDest;
	}

	float getDestYValue() {
		return robots.get(0).yDest;
	}

	void setDest(float x, float y) {
		Robot s = robots.get(0);
		s.xDest = x;
		s.yDest = y;
	}

	double getDistanceToDest(int robot) {
		Robot s = robots.get(robot);
		return Math.sqrt((s.x - s.xDest) * (s.x - s.xDest) + (s.y - s.yDest) * (s.y - s.yDest));
	}

	class Robot {
		float x;
		float y;
		float xDest;
		float yDest;

		Robot(float x, float y) {
			this.x = x;
			this.y = y;
			this.xDest = x;
			this.yDest = y;
		}

		void update() {
			float speed = GameModel.this.getSpeedOfTravel(this.x, this.y);
			float dx = this.xDest - this.x;
			float dy = this.yDest - this.y;
			float dist = (float) Math.sqrt(dx * dx + dy * dy);
			float t = speed / Math.max(speed, dist);
			dx *= t;
			dy *= t;
			this.x += dx;
			this.y += dy;
			this.x = Math.max(0.0f, Math.min(XMAXIMUM, this.x));
			this.y = Math.max(0.0f, Math.min(YMAXIMUM, this.y));
		}
	}
}
