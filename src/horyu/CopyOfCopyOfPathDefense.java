package horyu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CopyOfCopyOfPathDefense {

	private static final int MAX_TOWER_RANGE = 5;
	private static final boolean DEBUG = false;
	private int rangeList[][] = new int[MAX_TOWER_RANGE + 1][];
	private int N, N2, money, creepHealth, creepMoney;
	private boolean put[], base[];
	private TowerType types[];
	private Creep creeps[];
	private int[] baseHealth, basep, canPut;
	private int[] start, posValue, minBaseId;
	private int[][] baseDist, attackPos;

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
					if (base[now])
						continue;
					++posValue[now];
					next = now + N;
					if (next < N2 && func.isOK(now, next) && !used[next] && !put[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now - N;
					if (next >= 0 && func.isOK(now, next) && !used[next] && !put[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now + 1;
					if (y != N - 1 && func.isOK(now, next) && !used[next] && !put[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
					next = now - 1;
					if (y != 0 && func.isOK(now, next) && !used[next] && !put[next]) {
						queue[qs++] = next;
						used[next] = true;
					}
				}
			}
			//			for (int i = 0; i < N; ++i) {
			//				for (int j = 0; j < N; ++j) {
			//					System.out.print(String.format("%3d", posValue[pos(i, j)]));
			//				}
			//				System.out.println();
			//			}
			//			System.out.println();
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

		this.money = money;
		this.creepHealth = creepHealth;
		this.creepMoney = creepMoney;
		types = new TowerType[towerTypes.length / 3];
		for (int i = 0; i < towerTypes.length; i += 3) {
			types[i / 3] = new TowerType(i / 3, towerTypes[i], towerTypes[i + 1], towerTypes[i + 2]);
			debug("range", types[i / 3].range1, "damage", types[i / 3].damage, types[i / 3].cost, types[i / 3].value);
		}
		Arrays.sort(types, (o1, o2) -> Double.compare(o2.value, o1.value));

		List<Integer> canPut = new ArrayList<>();
		attackPos = new int[MAX_TOWER_RANGE + 1][N2];
		for (int i = 0; i < N2; ++i) {
			if (put[i]) {
				canPut.add(i);
			} else if (!base[i]) {
				for (int r = 1; r <= MAX_TOWER_RANGE; ++r) {
					int r2 = r * r;
					for (int x : rangeList[r]) {
						int j = i + x;
						if (0 <= j && j < N2 && dist(i, j) <= r2) {
							++attackPos[r][j];
						}
					}
				}
			}
		}
		this.canPut = new int[canPut.size()];
		for (int i = 0; i < this.canPut.length; ++i)
			this.canPut[i] = canPut.get(i);

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

	/*
	 * 複数のタワータイプを使ってみる
	 */
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
		List<Tower> tmpTowers = new ArrayList<>(towers);
		int tmpAttackTowers[] = Arrays.copyOf(attackTowers, attackTowers.length);
		int tmpCanPut[] = Arrays.copyOf(canPut, canPut.length);
		int income = 0;
		while (true) {
			for (Creep c : creeps)
				c.init();
			Creep tmpCreep[] = Arrays.copyOf(creeps, creeps.length);
			List<Creep> goalCreep = new ArrayList<>();
			while (true) {
				tmpCreep = updateCreeps(tmpCreep, goalCreep, tmpAttackTowers);
				if (tmpCreep.length == 0 || !goalCreep.isEmpty())
					break;
				income = Math.max(income, updateAttack(tmpCreep, tmpTowers));
			}
			if (!goalCreep.isEmpty()) {
				int routeRange[][] = new int[MAX_TOWER_RANGE + 1][N2];
				for (Creep c : goalCreep) {
					int pos = c.ip;
					if (base[pos]) {
						continue;
					}
					while (!base[pos]) {
						pos = nextPosition(pos, tmpAttackTowers);
						if (base[pos]) {
							break;
						}
						for (int r = 1; r <= MAX_TOWER_RANGE; ++r) {
							int r2 = r * r;
							for (int i : rangeList[r]) {
								int j = pos + i;
								if (0 <= j && j < N2 && dist(pos, j) <= r2) {
									++routeRange[r][j];
									// routeRange[r][j] += posValue[pos];
								}
							}
						}
					}
				}
				int index = -1, type = -1;
				double value = 0;
				for (int i = 0; i < tmpCanPut.length; ++i) {
					int p = tmpCanPut[i];
					for (int t = 0; t < types.length; ++t) {
						int range = types[t].range1;
						double pv = (double) ((attackPos[range][p] + routeRange[range][p]) * types[t].damage)
								/ types[t].cost;
						if (routeRange[range][p] > 0 && money >= types[t].cost && value < pv) {
							value = pv;
							index = i;
							type = t;
						}
					}
				}
				if (index == -1) {
					break;
				}
				Tower add = new Tower(tmpCanPut[index], types[type]);
				tmpCanPut = remove(tmpCanPut, index);
				tmpTowers.add(add);
				res.add(add);
				money -= types[type].cost;
				for (int i = 0; i < N2; ++i)
					if (dist(add.pos, i) <= types[type].range)
						++tmpAttackTowers[i];
			} else {
				break;
			}
		}
		if (income == 0)
			return new int[0];
		{
			for (int i = 0; i < res.size(); ++i) {
				boolean put = false;
				Tower t = res.get(i);
				for (Creep c : creeps) {
					c.init();
					if (dist(t.pos, nextPosition(c.pos, tmpAttackTowers)) <= t.t.range) {
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
					for (int j = 0; j < N2; ++j)
						if (dist(t.pos, j) <= t.t.range)
							++attackTowers[j];
				} else {
					res.remove(i);
					--i;
				}
			}
		}
		// new Scanner(System.in).nextLine();
		return result(res);
	}

	int attackTowers[];

	int nextPosition(int pos, int[] attackTowers) {
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

	Creep[] updateCreeps(Creep creeps[], List<Creep> goalCreep, int[] attackTowers) {
		Creep tmp[] = new Creep[creeps.length];
		int i = 0;
		for (Creep c : creeps) {
			if (c.health <= 0)
				continue;
			int next = nextPosition(c.pos, attackTowers);
			if (base[next]) {
				goalCreep.add(c);
				continue;
			}
			c.pos = next;
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

	private final int[] remove(int[] src, int i) {
		int[] res = Arrays.copyOf(src, src.length - 1);
		if (i == src.length - 1)
			return res;
		res[i] = src[src.length - 1];
		return res;
	}
}
