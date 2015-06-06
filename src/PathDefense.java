import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathDefense {

	private static final int SIMULATION_TIME = 2000;
	private static final int MAX_TOWER_RANGE = 5;
	private static final boolean DEBUG = false;

	private int rangeList[][] = new int[MAX_TOWER_RANGE + 1][];
	private int N, N2, money, creepHealth, creepMoney;
	private boolean put[];
	private TowerType types[], best;
	private Creep creeps[];
	private int[] basep, base, baseIndex, canPut;
	private int[] start, routeCount, minBaseId, baseHealth;
	private int[][] baseDist, simpleValue;

	int init(String[] board, int money, int creepHealth, int creepMoney, int[] towerTypes) {
		N = board.length;
		N2 = N * N;
		put = new boolean[N2];
		base = new int[N2];
		Arrays.fill(base, -1);
		int bc = 0, si = 0;
		start = new int[N2];
		routeCount = new int[N2];
		baseIndex = new int[10];
		for (int i = 0; i < N2; ++i) {
			int x = getX(i), y = getY(i);
			char c = board[x].charAt(y);
			put[i] = c == '#';
			if ('0' <= c && c <= '9') {
				boolean ok = false;
				ok |= x != N - 1 && board[x + 1].charAt(y) != '#';
				ok |= y != N - 1 && board[x].charAt(y + 1) != '#';
				ok |= x != 0 && board[x - 1].charAt(y) != '#';
				ok |= y != 0 && board[x].charAt(y - 1) != '#';
				if (ok) {
					base[i] = c - '0';
					baseIndex[base[i]] = i;
					bc++;
				}
			}
			if (!put[i] && (x == 0 || y == 0 || x == N - 1 || y == N - 1)) {
				start[si++] = i;
			}
		}
		start = Arrays.copyOf(start, si);
		basep = new int[bc];
		baseDist = new int[bc][];
		minBaseId = new int[N2];
		// testCount = new int[N2];
		int minBaseDist[] = new int[N2];
		Arrays.fill(minBaseDist, Integer.MAX_VALUE);
		bc = 0;
		for (int i = 0; i < N2; ++i)
			if (base[i] >= 0) {
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

				++bc;
			}
		{
			class Func {
				boolean isOK(int i, int j) {
					for (int[] d : baseDist)
						if (d[i] > d[j])
							return true;
					return false;
				}
			}
			Func func = new Func();
			for (int s : start) {
				int queue[] = new int[N2], qi = 0, qs = 1;
				boolean used[] = new boolean[N2];
				queue[qi] = s;
				used[s] = true;
				while (qi < qs) {
					int now = queue[qi++], next, y = getY(now);
					if (base[now] >= 0)
						continue;
					++routeCount[now];
					next = now + N;
					if (next < N2 && !put[next] && func.isOK(now, next) && !used[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now - N;
					if (next >= 0 && !put[next] && func.isOK(now, next) && !used[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now + 1;
					if (y != N - 1 && !put[next] && func.isOK(now, next) && !used[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now - 1;
					if (y != 0 && !put[next] && func.isOK(now, next) && !used[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
				}
			}
		}

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
		}
		simpleValue = new int[MAX_TOWER_RANGE + 1][N2];
		for (int r = 1; r <= MAX_TOWER_RANGE; ++r) {
			int d[] = new int[N2], r2 = r * r;
			int range[] = rangeList[r];
			for (int i = 0; i < N2; ++i) {
				if (put[i])
					for (int j : range) {
						int x = i + j;
						if (0 <= x && x < N2 && dist(i, x) <= r2) {
							d[i] += routeCount[x];
						}
					}
			}
			simpleValue[r] = d;
		}

		this.money = money;
		this.creepHealth = creepHealth;
		this.creepMoney = creepMoney;
		types = new TowerType[towerTypes.length / 3];
		for (int i = 0; i < towerTypes.length; i += 3) {
			types[i / 3] = new TowerType(i / 3, towerTypes[i], towerTypes[i + 1], towerTypes[i + 2]);
			debug("range", types[i / 3].range1, "damage", types[i / 3].damage, types[i / 3].cost, types[i / 3].value);
		}
		Arrays.sort(types, (o1, o2) -> Double.compare(o2.value, o1.value));
		best = types[0];

		List<Integer> canPut = new ArrayList<>();
		for (int i = 0; i < N2; ++i) {
			if (put[i] && simpleValue[MAX_TOWER_RANGE][i] > 0) {
				canPut.add(i);
			}
		}
		this.canPut = new int[canPut.size()];
		for (int i = 0; i < this.canPut.length; ++i)
			this.canPut[i] = canPut.get(i);

		//		debug("board", board);
		//		debug("money", money);
		debug("creepHealth", creepHealth);
		debug("creepMoney", creepMoney);
		//		debug("towerTypes", towerTypes);

		return 0;
	}

	private final int dist(final int pos1, final int pos2) {
		final int dx = getX(pos1) - getX(pos2), dy = getY(pos1) - getY(pos2);
		return dx * dx + dy * dy;
	}

	private final List<Tower> towers = new ArrayList<>();
	// int testCount[];
	private int step = 0;
	Map<Integer, Creep> creepIdMap = new HashMap<>();

	int[] placeTowers(int[] creep, int money, int[] baseHealth) {
		{// input
			step++;
			this.creeps = new Creep[creep.length / 4];
			for (int i = 0; i < creep.length; i += 4) {
				Creep c = new Creep(creep[i], creep[i + 1], pos(creep[i + 3], creep[i + 2]));
				Creep t = creepIdMap.get(c.id);
				if (t == null) {
					this.creeps[i / 4] = c;
					creepIdMap.put(c.id, c);
				} else {
					t.update(c);
					this.creeps[i / 4] = t;
				}
				// ++testCount[creeps[i / 4].pos];
				// debug("creep", creeps[i / 4].id, creeps[i / 4].health);
			}
			Arrays.sort(this.creeps, (o1, o2) -> o1.id - o2.id);
			this.money = money;
			this.baseHealth = baseHealth;
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

		class Simulation {
			int income;
			List<Creep> goal = new ArrayList<>();

			public Simulation(final List<Tower> add) {
				for (Creep c : creeps)
					c.init();
				Creep tmpCreep[] = Arrays.copyOf(creeps, creeps.length);
				List<Tower> tmpTowers = new ArrayList<>(towers);
				tmpTowers.addAll(add);
				Tower[] towers = tmpTowers.toArray(new Tower[0]);
				while (true) {
					tmpCreep = updateCreeps(tmpCreep, goal);
					if (tmpCreep.length == 0)
						break;
					income += updateAttack(tmpCreep, towers);
				}
			}

			Creep[] updateCreeps(final Creep creeps[], final List<Creep> goalCreep) {
				Creep tmp[] = new Creep[creeps.length];
				int i = 0;
				for (Creep c : creeps) {
					if (c.health <= 0)
						continue;
					int next = nextPosition(c);
					if (base[next] >= 0) {
						if (baseHealth[base[next]] > 0)
							goalCreep.add(c);
						continue;
					}
					c.pos = next;
					tmp[i++] = c;
				}
				return Arrays.copyOf(tmp, i);
			}

			int updateAttack(final Creep creeps[], final Tower[] towers) {
				int income = 0;
				for (Tower t : towers) {
					// search for nearest attackable creep
					Creep def = null;
					int cdist = t.t.range + 1;
					for (Creep c : creeps) {
						if (c.health > 0) {
							int dst = dist(t.pos, c.pos);
							// nearest creep?
							if (dst < cdist) {
								cdist = dst;
								def = c;
							}
						}
					}
					if (def != null) {
						// debugAlways(def.id, def.health, def.health - t.t.damage);
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
		}

		List<Tower> res = new ArrayList<>();
		int tmpCanPut[] = Arrays.copyOf(canPut, canPut.length);
		int income = 0;
		Simulation sim;
		while (true) {
			sim = new Simulation(res);
			income = Math.max(income, sim.income);
			if (!sim.goal.isEmpty() && money >= (res.size() + 1) * best.cost) {
				int rl[] = rangeList[best.range1];
				int routeRange[][] = new int[sim.goal.size()][N2];
				for (int ci = 0; ci < sim.goal.size(); ++ci) {
					Creep c = sim.goal.get(ci);
					c.pos = c.ip;
					if (base[c.pos] >= 0) {
						continue;
					}
					while (base[c.pos] == -1) {
						c.pos = nextPosition(c);
						if (base[c.pos] >= 0) {
							break;
						}
						for (int i : rl) {
							int j = c.pos + i;
							if (0 <= j && j < N2 && dist(c.pos, j) <= best.range) {
								++routeRange[ci][j];
							}
						}
					}
				}
				int index = -1, value = 0;
				for (int i = 0; i < tmpCanPut.length; ++i) {
					int p = tmpCanPut[i];
					int willKill = 0, willAttack = 0;
					for (int ci = 0; ci < sim.goal.size(); ++ci) {
						willAttack += routeRange[ci][p];
						if (routeRange[ci][p] >= (sim.goal.get(ci).health + best.damage - 1) / best.damage) {
							++willKill;
						}
					}
					int pv;
					if (basep.length == 1) {
						pv = (willKill << 10) + simpleValue[best.range1][p];
					} else {
						pv = (willKill << 10) + (willAttack << 6) + simpleValue[best.range1][p];
					}
					if (willAttack > 0 && value < pv) {
						value = pv;
						index = i;
					}
				}
				if (index == -1) {
					break;
				}
				Tower add = new Tower(tmpCanPut[index], best);
				tmpCanPut = remove(tmpCanPut, index);
				res.add(add);
			} else {
				break;
			}
		}
		// new Scanner(System.in).nextLine();
		if (income == 0)
			return new int[0];
		{
			for (int i = 0; i < res.size(); ++i) {
				boolean put = false;
				Tower t = res.get(i);
				for (Creep c : creeps) {
					c.init();
					if (dist(t.pos, nextPosition(c)) <= t.t.range) {
						put = true;
						break;
					}
				}
				if (put) {
					for (int j = 0; j < canPut.length; ++j)
						if (t.pos == canPut[j]) {
							canPut = remove(canPut, j);
							break;
						}
					towers.add(t);
				} else {
					res.remove(i);
					--i;
				}
			}
		}
		return result(res);
	}

	private final int nextPosition(Creep c) {
		final int pos = c.pos, dist[] = c.base, nowDist = dist[pos];
		int next;
		next = pos + 1;
		if (next < N2 && dist[next] + 1 == nowDist) {
			return next;
		}
		next = pos - 1;
		if (0 <= next && dist[next] + 1 == nowDist) {
			return next;
		}
		next = pos + N;
		if (next < N2 && dist[next] + 1 == nowDist) {
			return next;
		}
		next = pos - N;
		if (0 <= next && dist[next] + 1 == nowDist) {
			return next;
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
		throw new RuntimeException();
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
		final int id;
		int ih, ip;
		int health, pos, bit = 0, base[];

		Creep(int id, int health, int pos) {
			this.id = id;
			ih = this.health = health;
			ip = this.pos = pos;
			base = baseDist[minBaseId[pos]];
		}

		void init() {
			health = ih;
			pos = ip;
		}

		/*
		 * Creepの移動に関して、シミューレーションを寄せる
		 * 具体的には、あるベースに近づかない移動をした場合は、そのベースが距離最短でも向かわない
		 */
		void update(final Creep c) {
			ih = health = c.health;
			base = null;
			for (int i = 0; i < baseDist.length; ++i) {
				int tmp[] = baseDist[i], b = (1 << i);
				if (tmp[pos] <= tmp[c.pos]) {
					bit |= b;
				} else if ((bit & b) == 0 && (base == null || base[c.pos] > tmp[c.pos])) {
					base = tmp;
				}
			}
			ip = pos = c.pos;
			if (base == null) {
				base = baseDist[minBaseId[pos]];
			}
		}
	}

	private class TowerType {
		final int id, range1, range, damage, cost;
		final double value;

		TowerType(final int id, final int range, final int damage, final int cost) {
			this.id = id;
			this.range1 = range;
			this.range = range * range;
			this.damage = damage;
			this.cost = cost;
			value = (double) (range * damage) / cost;
			// これはうまくいかない。何でかは分かってないから考察の価値はある
			// value = (double) (range * range * damage) / cost;
		}
	}

	private final int pos(final int x, final int y) {
		return x * N + y;
	}

	private final int getX(final int pos) {
		return pos / N;
	}

	private final int getY(final int pos) {
		return pos % N;
	}

	private static void debug(final Object... obj) {
		if (DEBUG)
			System.err.println(Arrays.deepToString(obj));
	}

	private static void debugAlways(final Object... obj) {
		System.err.println(Arrays.deepToString(obj));
	}

	private final <T> T[] remove(final T[] src, final int i) {
		T[] res = Arrays.copyOf(src, src.length - 1);
		if (i == src.length - 1)
			return res;
		res[i] = src[src.length - 1];
		return res;
	}

	private final int[] remove(final int[] src, final int i) {
		int[] res = Arrays.copyOf(src, src.length - 1);
		if (i == src.length - 1)
			return res;
		res[i] = src[src.length - 1];
		return res;
	}

	private final int[] result(final List<Tower> res) {
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
}
