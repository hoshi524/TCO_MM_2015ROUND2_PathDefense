import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

class Constants {
	public static final int SIMULATION_TIME = 2000;
	public static final int[] DY = new int[] { 1, 0, -1, 0 };
	public static final int[] DX = new int[] { 0, -1, 0, 1 };
}

class CreepType {
	public static final int MIN_CREEP_HEALTH = 1;
	public static final int MAX_CREEP_HEALTH = 20;
	public static final int MIN_CREEP_MONEY = 1;
	public static final int MAX_CREEP_MONEY = 20;
	int health;
	int money;

	public CreepType(Random rnd) {
		health = rnd.nextInt(MAX_CREEP_HEALTH - MIN_CREEP_HEALTH + 1) + MIN_CREEP_HEALTH;
		money = rnd.nextInt(MAX_CREEP_MONEY - MIN_CREEP_MONEY + 1) + MIN_CREEP_MONEY;
	}
}

class Creep {
	int id;
	int health;
	int x, y;
	int spawnTime;
	ArrayList<Integer> moves = new ArrayList<Integer>();
}

class TowerType {
	public static final int MIN_TOWER_RANGE = 1;
	public static final int MAX_TOWER_RANGE = 5;
	public static final int MIN_TOWER_DAMAGE = 1;
	public static final int MAX_TOWER_DAMAGE = 5;
	public static final int MIN_TOWER_COST = 5;
	public static final int MAX_TOWER_COST = 40;
	int range;
	int damage;
	int cost;

	public TowerType(Random rnd) {
		range = rnd.nextInt(MAX_TOWER_RANGE - MIN_TOWER_RANGE + 1) + MIN_TOWER_RANGE;
		damage = rnd.nextInt(MAX_TOWER_DAMAGE - MIN_TOWER_DAMAGE + 1) + MIN_TOWER_DAMAGE;
		cost = rnd.nextInt(MAX_TOWER_COST - MIN_TOWER_COST + 1) + MIN_TOWER_COST;
	}

}

class Tower {
	int x, y;
	int type;
}

class AttackVis {
	int x1, y1, x2, y2;

	public AttackVis(int _x1, int _y1, int _x2, int _y2) {
		x1 = _x1;
		y1 = _y1;
		x2 = _x2;
		y2 = _y2;
	}
}

class TestCase {
	public static final int MIN_CREEP_COUNT = 500;
	public static final int MAX_CREEP_COUNT = 2000;
	public static final int MIN_TOWER_TYPES = 1;
	public static final int MAX_TOWER_TYPES = 20;
	public static final int MIN_BASE_COUNT = 1;
	public static final int MAX_BASE_COUNT = 8;
	public static final int MIN_WAVE_COUNT = 1;
	public static final int MAX_WAVE_COUNT = 15;
	public static final int MIN_BOARD_SIZE = 20;
	public static final int MAX_BOARD_SIZE = 60;

	public int boardSize;
	public int money;

	public CreepType creepType;

	public int towerTypeCnt;
	public TowerType[] towerTypes;

	public char[][] board;
	public int pathCnt;
	public int[] spawnX;
	public int[] spawnY;
	public int[][] boardPath;
	public int tx, ty;

	public int baseCnt;
	public int[] baseX;
	public int[] baseY;

	public int creepCnt;
	public Creep[] creeps;
	public int waveCnt;

	public SecureRandom rnd = null;

	public void connect(Random rnd, int x1, int y1, int x2, int y2) {
		while (x1 != x2 || y1 != y2) {
			if (board[y1][x1] >= '0' && board[y1][x1] <= '9')
				return;
			board[y1][x1] = '.';
			int x1_ = x1;
			int y1_ = y1;
			if (x1 == x2) {
				if (y2 > y1) {
					y1++;
				} else {
					y1--;
				}
			} else if (y1 == y2) {
				if (x2 > x1) {
					x1++;
				} else {
					x1--;
				}
			} else {
				int nx = x1;
				int ny = y1;
				if (x2 > x1)
					nx++;
				else
					nx--;
				if (y2 > y1)
					ny++;
				else
					ny--;
				if (board[ny][x1] == '.') {
					y1 = ny;
				} else if (board[y1][nx] == '.') {
					x1 = nx;
				} else {
					if (rnd.nextInt(2) == 0)
						y1 = ny;
					else
						x1 = nx;
				}
			}
			if (x1 > x1_)
				boardPath[y1_][x1_] |= 8;
			else if (x1 < x1_)
				boardPath[y1_][x1_] |= 2;
			else if (y1 > y1_)
				boardPath[y1_][x1_] |= 1;
			else if (y1 < y1_)
				boardPath[y1_][x1_] |= 4;
		}
	}

	public void addPath(Random rnd, int baseIdx) {
		int sx = 0, sy = 0;
		boolean nextTo = false;
		int tryEdge = 0;
		do {
			tryEdge++;
			if (tryEdge > boardSize)
				break;
			nextTo = false;
			sx = rnd.nextInt(boardSize - 1) + 1;
			if (rnd.nextInt(2) == 0) {
				sy = rnd.nextInt(2) * (boardSize - 1);
				if (sx > 0 && board[sy][sx - 1] == '.')
					nextTo = true;
				if (sx + 1 < boardSize && board[sy][sx + 1] == '.')
					nextTo = true;
			} else {
				sy = sx;
				sx = rnd.nextInt(2) * (boardSize - 1);
				if (sy > 0 && board[sy - 1][sx] == '.')
					nextTo = true;
				if (sy + 1 < boardSize && board[sy + 1][sx] == '.')
					nextTo = true;
			}
		} while (nextTo || board[sy][sx] != '#');
		if (tryEdge > boardSize)
			return;
		board[sy][sx] = '.';
		spawnX[baseIdx] = sx;
		spawnY[baseIdx] = sy;
		if (sx == 0) {
			boardPath[sy][sx] |= 8;
			sx++;
		} else if (sy == 0) {
			boardPath[sy][sx] |= 1;
			sy++;
		} else if (sx == boardSize - 1) {
			boardPath[sy][sx] |= 2;
			sx--;
		} else {
			boardPath[sy][sx] |= 4;
			sy--;
		}
		int b = baseIdx % baseCnt;
		if (baseIdx >= baseCnt)
			b = rnd.nextInt(baseCnt);
		connect(rnd, sx, sy, baseX[b], baseY[b]);
	}

	public TestCase(long seed) {

		try {
			rnd = SecureRandom.getInstance("SHA1PRNG");
		} catch (Exception e) {
			System.err.println("ERROR: unable to generate test case.");
			System.exit(1);
		}

		rnd.setSeed(seed);
		boolean genDone = true;
		do {
			boardSize = rnd.nextInt(MAX_BOARD_SIZE - MIN_BOARD_SIZE + 1) + MIN_BOARD_SIZE;
			if (seed == 1)
				boardSize = 20;
			board = new char[boardSize][boardSize];
			boardPath = new int[boardSize][boardSize];
			creepType = new CreepType(rnd);

			towerTypeCnt = rnd.nextInt(MAX_TOWER_TYPES - MIN_TOWER_TYPES + 1) + MIN_TOWER_TYPES;
			towerTypes = new TowerType[towerTypeCnt];
			money = 0;
			for (int i = 0; i < towerTypeCnt; i++) {
				towerTypes[i] = new TowerType(rnd);
				money += towerTypes[i].cost;
			}
			baseCnt = rnd.nextInt(MAX_BASE_COUNT - MIN_BASE_COUNT + 1) + MIN_BASE_COUNT;
			for (int y = 0; y < boardSize; y++) {
				for (int x = 0; x < boardSize; x++) {
					board[y][x] = '#';
					boardPath[y][x] = 0;
				}
			}
			baseX = new int[baseCnt];
			baseY = new int[baseCnt];
			for (int i = 0; i < baseCnt; i++) {
				int bx, by;
				do {
					bx = rnd.nextInt(boardSize - 8) + 4;
					by = rnd.nextInt(boardSize - 8) + 4;
				} while (board[by][bx] != '#');
				board[by][bx] = (char) ('0' + i);
				baseX[i] = bx;
				baseY[i] = by;
			}

			pathCnt = rnd.nextInt(baseCnt * 10 - baseCnt + 1) + baseCnt;
			spawnX = new int[pathCnt];
			spawnY = new int[pathCnt];
			for (int i = 0; i < pathCnt; i++) {
				addPath(rnd, i);
			}

			creepCnt = rnd.nextInt(MAX_CREEP_COUNT - MIN_CREEP_COUNT + 1) + MIN_CREEP_COUNT;
			if (seed == 1)
				creepCnt = MIN_CREEP_COUNT;
			creeps = new Creep[creepCnt];
			for (int i = 0; i < creepCnt; i++) {
				Creep c = new Creep();
				int j = rnd.nextInt(pathCnt);
				c.x = spawnX[j];
				c.y = spawnY[j];
				c.id = i;
				c.spawnTime = rnd.nextInt(Constants.SIMULATION_TIME);
				c.health = creepType.health * (1 << (c.spawnTime / 500));
				creeps[i] = c;
			}
			// waves
			waveCnt = rnd.nextInt(MAX_WAVE_COUNT - MIN_WAVE_COUNT + 1) + MIN_WAVE_COUNT;
			int wi = 0;
			for (int w = 0; w < waveCnt; w++) {
				int wavePath = rnd.nextInt(pathCnt);
				int waveSize = 5 + rnd.nextInt(creepCnt / 20);
				int waveStartT = rnd.nextInt(Constants.SIMULATION_TIME - waveSize);
				for (int i = 0; i < waveSize; i++) {
					if (wi >= creepCnt)
						break;
					creeps[wi].x = spawnX[wavePath];
					creeps[wi].y = spawnY[wavePath];
					creeps[wi].spawnTime = waveStartT + rnd.nextInt(waveSize);
					// creeps[wi].health = creepType.health;
					creeps[wi].health = creepType.health * (1 << (creeps[wi].spawnTime / 500));
					wi++;
				}
				if (wi >= creepCnt)
					break;
			}

			// determine paths for each creep
			genDone = true;
			for (Creep c : creeps) {
				c.moves.clear();
				int x = c.x;
				int y = c.y;
				int prevx = -1;
				int prevy = -1;
				int tryPath = 0;
				while (!(board[y][x] >= '0' && board[y][x] <= '9')) {
					int dir = 0;
					tryPath++;
					if (tryPath > boardSize * boardSize)
						break;
					// select a random direction that will lead to a base, don't go back to where the creep was in the previous time step
					int tryDir = 0;
					do {
						if (tryDir == 15) {
							tryDir = -1;
							break;
						}
						dir = rnd.nextInt(4);
						tryDir |= (1 << dir);
					} while ((boardPath[y][x] & (1 << dir)) == 0
							|| (x + Constants.DX[dir] == prevx && y + Constants.DY[dir] == prevy));
					if (tryDir < 0)
						break;
					c.moves.add(dir);
					prevx = x;
					prevy = y;
					x += Constants.DX[dir];
					y += Constants.DY[dir];
				}
				if (!(board[y][x] >= '0' && board[y][x] <= '9')) {
					genDone = false;
					break;
				}
			}
		} while (!genDone);
	}
}

class Drawer extends JFrame {
	public static final int EXTRA_WIDTH = 200;
	public static final int EXTRA_HEIGHT = 100;

	public World world;
	public DrawerPanel panel;

	public int cellSize, boardSize;
	public int width, height;

	public boolean pauseMode = false;
	public boolean stepMode = false;
	public boolean debugMode = false;

	class DrawerKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			synchronized (keyMutex) {
				if (e.getKeyChar() == ' ') {
					pauseMode = !pauseMode;
				}
				if (e.getKeyChar() == 'd') {
					debugMode = !debugMode;
				}
				if (e.getKeyChar() == 's') {
					stepMode = !stepMode;
				}
				keyPressed = true;
				keyMutex.notifyAll();
			}
		}
	}

	class DrawerPanel extends JPanel {
		public void paint(Graphics g) {
			synchronized (world.worldLock) {
				int cCnt[][] = new int[boardSize][boardSize];
				g.setColor(new Color(0, 128, 0));
				g.fillRect(15, 15, cellSize * boardSize + 1, cellSize * boardSize + 1);
				g.setColor(Color.BLACK);
				for (int i = 0; i <= boardSize; i++) {
					g.drawLine(15 + i * cellSize, 15, 15 + i * cellSize, 15 + cellSize * boardSize);
					g.drawLine(15, 15 + i * cellSize, 15 + cellSize * boardSize, 15 + i * cellSize);
				}
				g.setColor(new Color(32, 32, 32));
				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						if (world.tc.board[i][j] == '.') {
							cCnt[i][j] = 0;
							g.fillRect(15 + j * cellSize + 1, 15 + i * cellSize + 1, cellSize - 1, cellSize - 1);
						}
					}
				}

				g.setColor(Color.WHITE);
				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						if (world.tc.board[i][j] >= '0' && world.tc.board[i][j] <= '9') {
							g.fillRect(15 + j * cellSize + 1, 15 + i * cellSize + 1, cellSize - 1, cellSize - 1);
						}
					}
				}
				// draw the health of each base
				for (int b = 0; b < world.tc.baseCnt; b++) {
					int col = world.baseHealth[b] * 255 / 1000;
					g.setColor(new Color(col, col, col));
					g.fillRect(15 + world.tc.baseX[b] * cellSize + 1 + cellSize / 4, 15 + world.tc.baseY[b] * cellSize
							+ 1 + cellSize / 4, cellSize / 2 - 1, cellSize / 2 - 1);
				}

				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						if (world.tc.board[i][j] >= 'A') {
							int ttype = world.tc.board[i][j] - 'A';
							float hue = (float) (ttype) / world.tc.towerTypeCnt;
							// tower color
							g.setColor(Color.getHSBColor(hue, 0.9f, 1.0f));
							g.fillOval(15 + j * cellSize + 2, 15 + i * cellSize + 2, cellSize - 4, cellSize - 4);
							if (debugMode) {
								// draw area of attack
								int r = world.tc.towerTypes[ttype].range;
								g.drawOval(15 + (j - r) * cellSize + 1, 15 + (i - r) * cellSize + 1, cellSize
										* (r * 2 + 1) - 1, cellSize * (r * 2 + 1) - 1);
								g.setColor(new Color(128, 128, 128, 30));
								g.fillOval(15 + (j - r) * cellSize + 1, 15 + (i - r) * cellSize + 1, cellSize
										* (r * 2 + 1) - 1, cellSize * (r * 2 + 1) - 1);
							}
						}
					}
				}
				for (Creep c : world.tc.creeps)
					if (c.health > 0 && c.spawnTime < world.curStep) {
						float h = Math.min(1.f, (float) (c.health) / (world.tc.creepType.health));
						g.setColor(new Color(h, 0, 0));
						g.fillRect(15 + c.x * cellSize + 1 + cCnt[c.y][c.x], 15 + c.y * cellSize + 1 + cCnt[c.y][c.x],
								cellSize - 1, cellSize - 1);
						cCnt[c.y][c.x] += 2;
					}

				g.setColor(Color.GREEN);
				for (AttackVis a : world.attacks) {
					g.drawLine(15 + a.x1 * cellSize + cellSize / 2, 15 + a.y1 * cellSize + cellSize / 2, 15 + a.x2
							* cellSize + cellSize / 2, 15 + a.y2 * cellSize + cellSize / 2);
				}

				g.setColor(Color.BLACK);
				g.setFont(new Font("Arial", Font.BOLD, 12));
				Graphics2D g2 = (Graphics2D) g;

				int horPos = 40 + boardSize * cellSize;

				g2.drawString("Board size = " + boardSize, horPos, 30);
				g2.drawString("Simulation Step = " + world.curStep, horPos, 50);
				g2.drawString("Creeps Spawned = " + world.numSpawned, horPos, 70);
				g2.drawString("Creeps killed = " + world.numKilled, horPos, 90);
				g2.drawString("Towers placed = " + world.numTowers, horPos, 110);
				g2.drawString("Money gained = " + world.moneyIncome, horPos, 130);
				g2.drawString("Money spend = " + world.moneySpend, horPos, 150);
				g2.drawString("Money = " + world.totMoney, horPos, 170);
				int baseh = 0;
				for (int i = 0; i < world.baseHealth.length; i++) {
					g.setColor(Color.GREEN);
					g.fillRect(horPos + 30, 205 + i * 20, world.baseHealth[i] / 10, 19);
					g.setColor(Color.BLACK);
					g.fillRect(horPos + 30 + world.baseHealth[i] / 10, 205 + i * 20, 100 - world.baseHealth[i] / 10, 19);
					g2.drawString(Integer.toString(world.baseHealth[i]), horPos, 220 + i * 20);
					baseh += world.baseHealth[i];
					g2.drawString(Integer.toString(i), 15 + world.tc.baseX[i] * cellSize + 2, 15
							+ (world.tc.baseY[i] + 1) * cellSize - 1);
				}
				g2.drawString("Base health = " + baseh, horPos, 195);
				int score = world.totMoney + baseh;
				g2.drawString("Score = " + score, horPos, 225 + world.baseHealth.length * 20);
			}
		}
	}

	class DrawerWindowListener extends WindowAdapter {
		public void windowClosing(WindowEvent event) {
			System.exit(0);
		}
	}

	final Object keyMutex = new Object();
	boolean keyPressed;

	public void processPause() {
		synchronized (keyMutex) {
			if (!stepMode && !pauseMode) {
				return;
			}
			keyPressed = false;
			while (!keyPressed) {
				try {
					keyMutex.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	public Drawer(World world, int cellSize) {
		super();

		panel = new DrawerPanel();
		getContentPane().add(panel);

		addWindowListener(new DrawerWindowListener());

		this.world = world;

		boardSize = world.tc.boardSize;
		this.cellSize = cellSize;
		width = cellSize * boardSize + EXTRA_WIDTH;
		height = cellSize * boardSize + EXTRA_HEIGHT;

		addKeyListener(new DrawerKeyListener());

		setSize(width, height);
		setTitle("Visualizer tool for problem PathDefense");

		setResizable(false);
		setVisible(true);
	}
}

class World {
	final Object worldLock = new Object();
	TestCase tc;
	int totMoney;
	int curStep = -1;
	int numSpawned;
	int numKilled;
	int numTowers;
	int moneyIncome;
	int moneySpend;
	int[] baseHealth;
	List<Tower> towers = new ArrayList<Tower>();
	List<AttackVis> attacks = new ArrayList<AttackVis>();

	public World(TestCase tc) {
		this.tc = tc;
		totMoney = tc.money;
		numSpawned = 0;
		numKilled = 0;
		numTowers = 0;
		moneyIncome = 0;
		moneySpend = 0;
		baseHealth = new int[tc.baseCnt];
		for (int i = 0; i < tc.baseCnt; i++)
			baseHealth[i] = 1000;
	}

	public void updateCreeps() {
		synchronized (worldLock) {
			for (Creep c : tc.creeps)
				if (c.health > 0 && c.spawnTime < curStep) {
					int dir = c.moves.get(curStep - c.spawnTime - 1);
					c.x += Constants.DX[dir];
					c.y += Constants.DY[dir];
					if (tc.board[c.y][c.x] >= '0' && tc.board[c.y][c.x] <= '9') {
						// reached a base
						int b = tc.board[c.y][c.x] - '0';
						// decrease the health of the base
						baseHealth[b] = Math.max(0, baseHealth[b] - c.health);
						c.health = 0;
					}
				} else if (c.spawnTime == curStep) {
					numSpawned++;
				}
		}
	}

	public void updateAttack() {
		synchronized (worldLock) {
			attacks.clear();
			for (Tower t : towers) {
				// search for nearest attackable creep
				int cidx = -1;
				int cdist = 1 << 29;
				for (int i = 0; i < tc.creeps.length; i++)
					if (tc.creeps[i].health > 0 && tc.creeps[i].spawnTime <= curStep) {
						int dst = (t.x - tc.creeps[i].x) * (t.x - tc.creeps[i].x) + (t.y - tc.creeps[i].y)
								* (t.y - tc.creeps[i].y);
						// within range of tower?
						if (dst <= tc.towerTypes[t.type].range * tc.towerTypes[t.type].range) {
							// nearest creep?
							if (dst < cdist) {
								cdist = dst;
								cidx = i;
							} else if (dst == cdist) {
								// creep with smallest id gets attacked first if they are the same distance away
								if (tc.creeps[i].id < tc.creeps[cidx].id) {
									cdist = dst;
									cidx = i;
								}
							}
						}
					}
				if (cidx >= 0) {
					// we hit something
					tc.creeps[cidx].health -= tc.towerTypes[t.type].damage;
					attacks.add(new AttackVis(t.x, t.y, tc.creeps[cidx].x, tc.creeps[cidx].y));
					if (tc.creeps[cidx].health <= 0) {
						// killed it!
						totMoney += tc.creepType.money;
						numKilled++;
						moneyIncome += tc.creepType.money;
					}
				}
			}

		}
	}
}

public class PathDefenseVis {
	public static String execCommand = null;
	public static boolean vis = true;
	public static boolean debug = false;
	public static int cellSize = 12;
	public static int delay = 50;
	public static boolean startPaused = false;

	public int runTest(long seed, Solver solver) {
		TestCase tc = new TestCase(seed);

		// Board information
		String board[] = new String[tc.boardSize];
		for (int y = 0; y < tc.boardSize; y++) {
			String row = "";
			for (int x = 0; x < tc.boardSize; x++) {
				row += tc.board[y][x];
			}
			board[y] = row;
		}
		// Creep type information
		// Tower type information
		int[] towerTypeData = new int[tc.towerTypeCnt * 3];
		int ii = 0;
		for (int i = 0; i < tc.towerTypeCnt; i++) {
			towerTypeData[ii++] = tc.towerTypes[i].range;
			towerTypeData[ii++] = tc.towerTypes[i].damage;
			towerTypeData[ii++] = tc.towerTypes[i].cost;
		}
		solver.init(board, tc.money, tc.creepType.health, tc.creepType.money, towerTypeData);

		World world = new World(tc);
		Drawer drawer = null;
		if (vis) {
			drawer = new Drawer(world, cellSize);
			drawer.debugMode = debug;
			if (startPaused) {
				drawer.pauseMode = true;
			}
		}

		for (int t = 0; t < Constants.SIMULATION_TIME; t++) {
			world.curStep++;

			int numLive = 0;
			for (Creep c : world.tc.creeps)
				if (c.health > 0 && c.spawnTime < world.curStep)
					numLive++;

			int[] creeps = new int[numLive * 4];
			int ci = 0;
			for (Creep c : world.tc.creeps)
				if (c.health > 0 && c.spawnTime < world.curStep) {
					creeps[ci++] = c.id;
					creeps[ci++] = c.health;
					creeps[ci++] = c.x;
					creeps[ci++] = c.y;
				}

			int[] newTowers = solver.placeTowers(creeps, world.totMoney, world.baseHealth);
			int commandCnt = newTowers.length;
			if (commandCnt > tc.boardSize * tc.boardSize * 3) {
				System.err.println("ERROR: Return array from placeTowers too large.");
				return -1;
			}
			if ((commandCnt % 3) != 0) {
				System.err.println("ERROR: Return array from placeTowers must be a multiple of 3.");
				return -1;
			}
			if (commandCnt > 0) {
				for (int i = 0; i < newTowers.length; i += 3) {
					Tower newT = new Tower();
					newT.x = newTowers[i];
					newT.y = newTowers[i + 1];
					newT.type = newTowers[i + 2];
					if (newT.x < 0 || newT.x >= tc.boardSize || newT.y < 0 || newT.y >= tc.boardSize) {
						System.err.println("ERROR: Placement (" + newT.x + "," + newT.y + ") outside of bounds.");
						return -1;
					}
					if (tc.board[newT.y][newT.x] != '#') {
						System.err.println("ERROR: Cannot place a tower at (" + newT.x + "," + newT.y + ").");
						return -1;
					}
					if (newT.type < 0 || newT.type >= tc.towerTypeCnt) {
						System.err.println("ERROR: Trying to place an illegal tower type.");
						return -1;
					}
					if (world.totMoney < tc.towerTypes[newT.type].cost) {
						System.err.println("ERROR: Not enough money to place tower. " + world.totMoney + " < "
								+ tc.towerTypes[newT.type].cost + " " + newT.type);
						return -1;
					}
					world.totMoney -= tc.towerTypes[newT.type].cost;
					tc.board[newT.y][newT.x] = (char) ('A' + newT.type);
					world.towers.add(newT);
					world.numTowers++;
					world.moneySpend += tc.towerTypes[newT.type].cost;
				}
			}

			world.updateCreeps();
			world.updateAttack();

			if (vis) {
				drawer.processPause();
				drawer.repaint();
				try {
					Thread.sleep(delay);
				} catch (Exception e) {
					// do nothing
				}
			}
		}

		int score = world.totMoney;
		for (int b = 0; b < world.baseHealth.length; b++)
			score += world.baseHealth[b];

		if (debug) {
			System.err.println("Money = " + world.totMoney);
			System.err.println("Total base health = " + (score - world.totMoney));
		}

		return score;
	}

	public static void main(String[] args) {
		new PathDefenseVis().run(args);
	}

	void run(String[] args) {
		for (int i = 0; i < args.length; i++)
			if (args[i].equals("-exec")) {
				execCommand = args[++i];
			} else if (args[i].equals("-novis")) {
				vis = false;
			} else if (args[i].equals("-debug")) {
				debug = true;
			} else if (args[i].equals("-sz")) {
				cellSize = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-delay")) {
				delay = Integer.parseInt(args[++i]);
			} else if (args[i].equals("-pause")) {
				startPaused = true;
			} else {
				System.out.println("WARNING: unknown argument " + args[i] + ".");
			}

		if (false) {
			debug = true;
			vis = true;
			try {
				for (long seed = 4809, N = seed + 0; seed <= N; seed++) {
					final long Seed = seed;
					Thread t0 = new Thread(new Runnable() {
						@Override
						public void run() {
							int score = runTest(Seed, new Wrapper());
							System.out.println("Score = " + score);
						}
					});
					//					Thread t1 = new Thread(new Runnable() {
					//						@Override
					//						public void run() {
					//							int score = runTest(Seed, new Wrapper2());
					//							System.out.println("Score = " + score);
					//						}
					//					});
					//					t1.start();
					//					t1.join();
					t0.start();
					t0.join();
				}
			} catch (Exception e) {
				System.err.println("ERROR: Unexpected error while running your test case.");
				e.printStackTrace();
			}
		} else {
			vis = false;
			compare();
		}
	}

	interface Solver {
		int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes);

		int[] placeTowers(int[] creep, int money, int[] baseHealth);
	}

	class Wrapper implements Solver {
		CopyOfPathDefense solver = new CopyOfPathDefense();

		public int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes) {
			return solver.init(board, money, creepHealth, creepMoney, towerTypes);
		}

		public int[] placeTowers(int[] creep, int money, int[] baseHealth) {
			return solver.placeTowers(creep, money, baseHealth);
		}
	}

	class Wrapper2 implements Solver {
		PathDefense solver = new PathDefense();

		public int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes) {
			return solver.init(board, money, creepHealth, creepMoney, towerTypes);
		}

		public int[] placeTowers(int[] creep, int money, int[] baseHealth) {
			return solver.placeTowers(creep, money, baseHealth);
		}
	}

	void compare() {
		class ParameterClass {
			volatile double d;
			volatile int timeover;
		}

		final int MAX_TIME = 20000;
		final ParameterClass sum0 = new ParameterClass(), sum1 = new ParameterClass();
		ExecutorService es = Executors.newFixedThreadPool(3);

		for (int seed = 1, size = seed + 10000; seed < size; seed++) {
			final int Seed = seed;
			es.submit(() -> {
				try {
					PathDefenseVis vis = new PathDefenseVis();
					long start0 = System.currentTimeMillis();
					int score0 = vis.runTest(Seed, new Wrapper());
					long end0 = System.currentTimeMillis();
					long start1 = System.currentTimeMillis();
					int score1 = vis.runTest(Seed, new Wrapper2());
					long end1 = System.currentTimeMillis();
					int max = Math.max(score0, score1);
					boolean change = false;
					if (score0 > 1 || score1 > 1) {
						sum0.d += (double) score0 / max;
						sum1.d += (double) score1 / max;
						change = (double) score0 / max < 0.9 || (double) score1 / max < 0.9;
					}
					if ((end0 - start0) >= MAX_TIME)
						sum0.timeover++;
					if ((end1 - start1) >= MAX_TIME)
						sum1.timeover++;
					String out = String.format("%3d   %5d : %5d    %5d : %5d    %.1f : %.1f   %d : %d", Seed, score0,
							score1, (end0 - start0), (end1 - start1), sum0.d, sum1.d, sum0.timeover, sum1.timeover);
					if (change)
						System.err.println(out);
					else
						System.out.println(out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		try {
			es.shutdown();
			if (!es.awaitTermination(1000000L, TimeUnit.SECONDS))
				es.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
			es.shutdownNow();
		}
	}
}

class ErrorStreamRedirector extends Thread {
	public BufferedReader reader;

	public ErrorStreamRedirector(InputStream is) {
		reader = new BufferedReader(new InputStreamReader(is));
	}

	public void run() {
		while (true) {
			String s;
			try {
				s = reader.readLine();
			} catch (Exception e) {
				//e.printStackTrace();
				return;
			}
			if (s == null) {
				break;
			}
			System.err.println(s);
		}
	}
}