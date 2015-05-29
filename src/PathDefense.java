import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PathDefense {

	private static final int MAX_TOWER_RANGE = 5;
	private static final boolean DEBUG = false;
	private int rangeList[][] = new int[MAX_TOWER_RANGE + 1][];
	private int N, N2, money, creepHealth, creepMoney;
	private boolean put[], base[];
	private TowerType type[], best;
	private Creep creeps[];
	private int[] baseHealth, basep;
	private int[] start, posValue, minBaseId;
	private int[][] baseDist;
	private Position canPut[];

	int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes) {
		N = board.length;
		N2 = N * N;
		put = new boolean[N2];
		base = new boolean[N2];
		int bc = 0, si = 0;
		start = new int[N2];
		posValue = new int[N2];
		for (int i = 0; i < N2; ++i) {
			int x = getX(i), y = getY(i);
			char c = board[x].charAt(y);
			put[i] = c == '#';
			if ('0' <= c && c <= '9') {
				base[i] = true;
				bc++;
			}
			if (!put[i] && (x == 0 || y == 0 || x == N - 1 || y == N - 1)) {
				start[si++] = i;
			}
		}
		start = Arrays.copyOf(start, si);
		basep = new int[bc];
		baseDist = new int[bc][];
		minBaseId = new int[N2];
		attackTowers = new int[N2];
		// testCount = new int[N2];
		int minBaseDist[] = new int[N2];
		Arrays.fill(minBaseDist, Integer.MAX_VALUE);
		bc = 0;
		for (int i = 0; i < N2; ++i)
			if (base[i]) {
				minBaseDist[i] = 0;
				basep[bc] = i;
				int d[] = new int[N2];
				Arrays.fill(d, -1);
				int queue[] = new int[N2], qi = 0, qs = 1;
				queue[qi] = i;
				d[i] = 0;
				while (qi < qs) {
					int now = queue[qi++], dn = d[now], next, y = getY(now);
					next = now + N;
					if (next < N2 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							minBaseId[next] = bc;
						}
					}
					next = now - N;
					if (next >= 0 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							minBaseId[next] = bc;
						}
					}
					next = now + 1;
					if (y != N - 1 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							minBaseId[next] = bc;
						}
					}
					next = now - 1;
					if (y != 0 && d[next] == -1 && !put[next]) {
						d[next] = dn + 1;
						queue[qs++] = next;
						if (minBaseDist[next] > d[next]) {
							minBaseDist[next] = d[next];
							minBaseId[next] = bc;
						}
					}
				}
				baseDist[bc] = d;

				for (int s : start) {
					qi = 0;
					qs = 1;
					boolean used[] = new boolean[N2];
					queue[qi] = s;
					used[s] = true;
					while (qi < qs) {
						int now = queue[qi++], dn = d[now], next, y = getY(now);
						++posValue[now];
						next = now + N;
						if (next < N2 && d[next] < dn && !used[next] && !put[next]) {
							queue[qs++] = next;
							used[next] = true;
						}
						next = now - N;
						if (next >= 0 && d[next] < dn && !used[next] && !put[next]) {
							queue[qs++] = next;
							used[next] = true;
						}
						next = now + 1;
						if (y != N - 1 && d[next] < dn && !used[next] && !put[next]) {
							queue[qs++] = next;
							used[next] = true;
						}
						next = now - 1;
						if (y != 0 && d[next] < dn && !used[next] && !put[next]) {
							queue[qs++] = next;
							used[next] = true;
						}
					}
				}

				++bc;
			}
		//		for (int i = 0; i < N; ++i) {
		//			for (int j = 0; j < N; ++j) {
		//				System.out.print(String.format("%3d ", posValue[pos(i, j)]));
		//			}
		//			System.out.println();
		//		}
		//		System.out.println();

		for (int range = 1; range <= MAX_TOWER_RANGE; ++range) {
			int range2 = range * range;
			List<Integer> list = new ArrayList<>();
			for (int a = 1; dist(a, 0) <= range2; ++a) {
				for (int b = 0;; b++) {
					if (dist(0, pos(a, b)) <= range2) {
						list.add(pos(a, b));
						list.add(pos(-b, a));
						list.add(pos(-a, -b));
						list.add(pos(b, -a));
					} else
						break;
				}
			}
			int res[] = new int[list.size()];
			for (int i = 0; i < res.length; ++i)
				res[i] = list.get(i);
			rangeList[range] = res;
			// debug("list : ", res.length);
		}

		this.money = money;
		this.creepHealth = creepHealth;
		this.creepMoney = creepMoney;
		type = new TowerType[towerTypes.length / 3];
		for (int i = 0; i < towerTypes.length; i += 3) {
			type[i / 3] = new TowerType(i / 3, towerTypes[i], towerTypes[i + 1], towerTypes[i + 2]);
			debug(type[i / 3].range, type[i / 3].damage, type[i / 3].cost, type[i / 3].value);
		}
		Arrays.sort(type, (o1, o2) -> Double.compare(o2.value, o1.value));
		best = type[0];

		List<Position> canPut = new ArrayList<>();
		for (int i = 0; i < N2; ++i) {
			if (put[i]) {
				int value = 0;
				for (int j = 0; j < N2; ++j)
					if (!put[j] && !base[j] && best.range >= dist(i, j)) {
						// value += posValue[j];
						++value;
					}
				canPut.add(new Position(i, value));
			}
		}
		Collections.sort(canPut, (o1, o2) -> Integer.compare(o2.value, o1.value));
		this.canPut = canPut.toArray(new Position[0]);

		//		for (int i = 0; i < N; ++i) {
		//			for (int j = 0; j < N; ++j) {
		//				int pos = pos(i, j);
		//				print: {
		//					for (Position position : this.canPut) {
		//						if (pos == position.pos) {
		//							System.out.print(String.format("%3d ", position.value));
		//							break print;
		//						}
		//					}
		//					System.out.print("    ");
		//				}
		//			}
		//			System.out.println();
		//		}
		//		System.out.println();

		//		debug("board", board);
		//		debug("money", money);
		debug("creepHealth", creepHealth);
		debug("creepMoney", creepMoney);
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
	// int testCount[];
	int step = 0;

	int[] placeTowers(int[] creep, int money, int[] baseHealth) {
		{// input
			this.creeps = new Creep[creep.length / 4];
			for (int i = 0; i < creep.length; i += 4) {
				this.creeps[i / 4] = new Creep(creep[i], creep[i + 1], pos(creep[i + 3], creep[i + 2]));
				// ++testCount[creeps[i / 4].pos];
				// debug("creep", creeps[i / 4].id, creeps[i / 4].health);
			}
			this.money = money;
			this.baseHealth = baseHealth;
			//			step++;
			//			if (step == 1999) {
			//				for (int i = 0; i < N; ++i) {
			//					for (int j = 0; j < N; ++j) {
			//						System.out.print(String.format("%3d ", testCount[pos(i, j)]));
			//					}
			//					System.out.println();
			//				}
			//				System.out.println();
			//			}
		}
		//		debug("creep", creep);
		//		debug("money", money);
		//		debug("baseHealth", baseHealth);

		List<Tower> res = new ArrayList<>();
		while (true) {
			for (Creep c : creeps)
				c.init();
			Creep tmp[] = Arrays.copyOf(creeps, creeps.length);
			List<Creep> goalCreep = new ArrayList<>();
			while (true) {
				tmp = updateCreeps(tmp, goalCreep);
				if (tmp.length == 0 || !goalCreep.isEmpty())
					break;
				updateAttack(tmp, towers);
			}
			if (!goalCreep.isEmpty() && money >= best.cost) {
				int index = -1, value = 0;
				int rl[] = rangeList[best.range1];
				for (Creep c : goalCreep) {
					int pos = c.ip;
					if (base[pos]) {
						continue;
					}
					int routeRange[] = new int[N2];
					while (!base[pos]) {
						pos = nextPosition(pos);
						if (base[pos]) {
							break;
						}
						for (int i : rl) {
							int j = pos + i;
							if (0 <= j && j < N2 && dist(pos, j) <= best.range) {
								++routeRange[j];
								// posValueでスコア落ちる、なんでや
								// routeRange[j] += posValue[pos];
							}
						}
					}
					/**
					 * 攻略困難or不可能なステージの時に、creepの撃破数を再優先にして、
					 * towerを置いても撃破数が伸びない場合は置くべきじゃない
					 */
					for (int i = 0; i < canPut.length; ++i) {
						Position p = canPut[i];
						int pv = routeRange[p.pos] * 10 + p.value;
						if (routeRange[p.pos] > 0
								&& value < pv
								&& !(towers.isEmpty() && money < best.cost * 2 && c.health > best.damage
										* routeRange[p.pos])) {
							value = pv;
							index = i;
						}
					}
					//					if (index != -1) {
					//						for (int i = 0; i < N; ++i) {
					//							for (int j = 0; j < N; ++j) {
					//								System.out.print(String.format("%3d ", routeRange[pos(i, j)]));
					//							}
					//							System.out.println();
					//						}
					//						System.out.println();
					//					}
				}
				if (index == -1) {
					break;
				}
				Tower add = new Tower(canPut[index].pos, best);
				canPut = remove(canPut, index);
				towers.add(add);
				res.add(add);
				money -= best.cost;
				for (int i = 0; i < N2; ++i)
					if (dist(add.pos, i) <= best.range)
						++attackTowers[i];
			} else {
				break;
			}
		}
		// new Scanner(System.in).nextLine();
		return result(res);
	}

	int attackTowers[];

	int nextPosition(int pos) {
		//		if (base[pos])
		//			throw new RuntimeException();
		int res = -1;
		final int dpos[] = { 1, -1, N, -N };
		final int dist[] = baseDist[minBaseId[pos]];
		final int nowDist = dist[pos];
		for (int d : dpos) {
			int next = pos + d;
			if (0 <= next && next < N2 && dist[next] + 1 == nowDist
					&& (res == -1 || attackTowers[res] > attackTowers[next])) {
				res = next;
			}
		}
		//		if (res == -1) {
		//			for (int i = 0; i < N; i++) {
		//				for (int j = 0; j < N; j++) {
		//					System.out.print(String.format("%2d ", dist[pos(i, j)]));
		//				}
		//				System.out.println();
		//			}
		//			throw new RuntimeException();
		//		}
		return res;
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

	Creep[] updateCreeps(Creep creeps[], List<Creep> goalCreep) {
		Creep tmp[] = new Creep[creeps.length];
		int i = 0;
		for (Creep c : creeps) {
			if (c.health <= 0)
				continue;
			if (base[c.pos]) {
				goalCreep.add(c);
				continue;
			}
			c.pos = nextPosition(c.pos);
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
				if (c.health > 0 && !base[c.pos]) {
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
		final int id, range1, range, damage, cost;
		final double value;

		TowerType(int id, int range, int damage, int cost) {
			this.id = id;
			this.range1 = range;
			this.range = range * range;
			this.damage = damage;
			this.cost = cost;
			value = (double) (range * damage) / cost;
			// value = (double) (rangeList[range].length * damage) / cost;
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
		if (DEBUG)
			System.err.println(Arrays.deepToString(obj));
	}

	private final <T> T[] remove(T[] src, int i) {
		T[] res = Arrays.copyOf(src, src.length - 1);
		if (i == src.length - 1)
			return res;
		res[i] = src[src.length - 1];
		return res;
	}
}
