import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathDefense {

	private int N, N2, money, creepHealth, creepMoney;
	private boolean put[], base[];
	private TowerType type[], best;
	private Creep creeps[];
	private int[] baseHealth, basep;
	private int[] next;
	private Position canPut[];

	int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes) {
		N = board.length;
		N2 = N * N;
		put = new boolean[N2];
		base = new boolean[N2];
		int bc = 0;
		for (int i = 0; i < N2; ++i) {
			char c = board[getX(i)].charAt(getY(i));
			put[i] = c == '#';
			if ('0' <= c && c <= '9') {
				base[i] = true;
				bc++;
			}
		}
		basep = new int[bc];
		next = new int[N2];
		Arrays.fill(next, -1);
		int minBaseDist[] = new int[N2];
		Arrays.fill(minBaseDist, Integer.MAX_VALUE);
		bc = 0;
		for (int i = 0; i < N2; ++i)
			if (base[i]) {
				minBaseDist[i] = 0;
				next[i] = -1;
				basep[bc] = i;
				int d[] = new int[N2];
				Arrays.fill(d, -1);
				int queue[] = new int[N2], qi = 0, qs = 1;
				queue[qi] = i;
				d[qi] = 0;
				while (qi < qs) {
					int now = queue[qi++], dn = d[now], next, y = getY(now);
					next = now + N;
					if (next < N2 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							this.next[next] = now;
						}
					}
					next = now - N;
					if (next >= 0 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							this.next[next] = now;
						}
					}
					next = now + 1;
					if (y != N - 1 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							this.next[next] = now;
						}
					}
					next = now - 1;
					if (y != 0 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							this.next[next] = now;
						}
					}
				}
				++bc;
			}
		//		for (int i = 0; i < N; ++i) {
		//			for (int j = 0; j < N; j++) {
		//				System.out.print(String.format("%3d ", next[pos(i, j)]));
		//			}
		//			System.out.println();
		//		}
		this.money = money;
		this.creepHealth = creepHealth;
		this.creepMoney = creepMoney;
		type = new TowerType[towerTypes.length / 3];
		for (int i = 0; i < towerTypes.length; i += 3) {
			type[i / 3] = new TowerType(i / 3, towerTypes[i], towerTypes[i + 1], towerTypes[i + 2]);
			// debug(type[i / 3].range, type[i / 3].damage, type[i / 3].cost, type[i / 3].value);
		}
		Arrays.sort(type, (o1, o2) -> Double.compare(o2.value, o1.value));
		best = type[0];

		List<Position> canPut = new ArrayList<>();
		for (int i = 0; i < N2; ++i) {
			if (put[i]) {
				int value = 0;
				for (int j = 0; j < N2; ++j)
					if (!put[j] && !base[i] && best.range >= dist(i, j))
						value++;
				canPut.add(new Position(i, value));
			}
		}
		Collections.sort(canPut, (o1, o2) -> Integer.compare(o2.value, o1.value));
		this.canPut = canPut.toArray(new Position[0]);

		//		debug("board", board);
		//		debug("money", money);
		//		debug("creepHealth", creepHealth);
		//		debug("creepMoney", creepMoney);
		//		debug("towerTypes", towerTypes);
		return 0;
	}

	private final int dist(int pos1, int pos2) {
		int dx = getX(pos1) - getX(pos2), dy = getY(pos1) - getY(pos2);
		return dx * dx + dy * dy;
	}

	private class Position {
		final int pos, value;

		Position(int pos, int value) {
			this.pos = pos;
			this.value = value;
		}
	}

	List<Tower> towers = new ArrayList<>();

	int[] placeTowers(int[] creep, int money, int[] baseHealth) {
		{// input
			this.creeps = new Creep[creep.length / 4];
			for (int i = 0; i < creep.length; i += 4) {
				this.creeps[i / 4] = new Creep(creep[i], creep[i + 1], pos(creep[i + 3], creep[i + 2]));
			}
			this.money = money;
			this.baseHealth = baseHealth;
		}
		//		debug("creep", creep);
		//		debug("money", money);
		//		debug("baseHealth", baseHealth);

		int allIncome = creeps.length * creepMoney;
		List<Tower> res = new ArrayList<>();
		while (true) {
			for (Creep c : creeps)
				c.init();
			Creep tmp[] = Arrays.copyOf(creeps, creeps.length);
			int sum = 0;
			while (true) {
				tmp = updateCreeps(tmp);
				if (tmp.length == 0)
					break;
				sum += updateAttack(tmp, towers);
			}
			if (sum < allIncome && money >= best.cost) {
				Tower add = new Tower(canPut[towers.size()].pos, best);
				towers.add(add);
				res.add(add);
				money -= best.cost;
			} else
				break;
		}
		return result(res);
	}

	int[] result(List<Tower> res) {
		int n = res.size() * 3;
		int x[] = new int[n];
		for (int i = 0; i < n; i += 3) {
			Tower t = res.get(i / 3);
			x[i] = getY(t.pos);
			x[i + 1] = getX(t.pos);
			x[i + 2] = t.type;
		}
		return x;
	}

	Creep[] updateCreeps(Creep creeps[]) {
		Creep tmp[] = new Creep[creeps.length];
		int i = 0;
		for (Creep c : creeps) {
			if (c.health <= 0 || next[c.pos] == -1)
				continue;
			c.pos = next[c.pos];
			tmp[i++] = c;
		}
		return Arrays.copyOf(tmp, i);
	}

	int updateAttack(Creep creeps[], List<Tower> towers) {
		int income = 0;
		for (Tower t : towers) {
			// search for nearest attackable creep
			Creep def = null;
			int cdist = 1 << 29;
			for (Creep c : creeps) {
				if (c.health > 0) {
					int dst = dist(t.pos, c.pos);
					// within range of tower?
					if (dst <= t.t.range) {
						// nearest creep?
						if (dst < cdist) {
							cdist = dst;
							def = c;
						} else if (dst == cdist && c.id < c.id) {
							// creep with smallest id gets attacked first if they are the same distance away
							cdist = dst;
							def = c;
						}
					}
				}
			}
			if (def != null) {
				// we hit something
				def.health -= t.t.damage;
				if (def.health <= 0) {
					// killed it!
					income += creepMoney;
				}
			}
		}
		return income;
	}

	private class Tower {
		final int pos, type;
		final TowerType t;

		Tower(int pos, TowerType t) {
			this.pos = pos;
			this.t = t;
			type = t.id;
		}
	}

	private class Creep {
		final int id, ih, ip;
		int health, pos;

		Creep(int id, int health, int pos) {
			this.id = id;
			ih = this.health = health;
			ip = this.pos = pos;
		}

		void init() {
			health = ih;
			pos = ip;
		}
	}

	private class TowerType {
		final int id, range, damage, cost;
		final double value;

		TowerType(int id, int range, int damage, int cost) {
			this.id = id;
			this.range = range * range;
			this.damage = damage;
			this.cost = cost;
			value = (double) (range * damage) / cost;
		}
	}

	private final int pos(int x, int y) {
		return x * N + y;
	}

	private final int getX(int pos) {
		return pos / N;
	}

	private final int getY(int pos) {
		return pos % N;
	}

	private static void debug(Object... obj) {
		System.err.println(Arrays.deepToString(obj));
	}
}
