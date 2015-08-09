package stormyHaystack;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
	static RobotController rc;
	static final int BARRACK = 1;
	static final int TANKFACTORY = 2;
	static final int HELIPAD = 3;
	static final int BEAVER = 4;
	static final int SOLDIER = 5;
	static final int BASHER = 6;
	static final int MINERFACTORY = 7;
	static final int MINER = 8;
	static final int TANK = 9;
	
	
	static Team myTeam;
	static Team enemyTeam;
	static int myRange;
	static Random rand;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static MapLocation towerFocus;

	public static void run(RobotController tomatojuice) {
		rc = tomatojuice;
		rand = new Random(rc.getID());

		myRange = rc.getType().attackRadiusSquared;
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		Direction lastDirection = null;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		RobotInfo[] myRobots;

		while(true) {
			try {
				rc.setIndicatorString(0, "This is an indicator string.");
				rc.setIndicatorString(1, "I am a " + rc.getType());
			} catch (Exception e) {
				System.out.println("Unexpected exception");
				e.printStackTrace();
			}

			if (rc.getType() == RobotType.HQ) {
				try {
					int fate = rand.nextInt(10000);
					myRobots = rc.senseNearbyRobots(999999, myTeam);
					int numSoldiers = 0;
					int numBashers = 0;
					int numBeavers = 0;
					int numBarracks = 0;
					int numTankfactory = 0;
					int numMinerfactory = 0;
					int numMiner = 0;
					int numTank = 0;
					for (RobotInfo r : myRobots) {
						RobotType type = r.type;
						if (type == RobotType.SOLDIER) {
							numSoldiers++;
						} else if (type == RobotType.BASHER) {
							numBashers++;
						} else if (type == RobotType.BEAVER) {
							numBeavers++;
						} else if (type == RobotType.BARRACKS) {
							numBarracks++;
						} else if (type == RobotType.TANKFACTORY) {
							numTankfactory++;
						} else if (type == RobotType.MINERFACTORY) {
							numMinerfactory++;
						} else if (type == RobotType.MINER) {
							numMiner++;
						} else if (type == RobotType.TANK) {
							numTank++;
						}
					}
					rc.broadcast(BEAVER, numBeavers);
					rc.broadcast(SOLDIER, numSoldiers);
					rc.broadcast(BASHER, numBashers);
					rc.broadcast(BARRACK, numBarracks);
					rc.broadcast(TANKFACTORY, numTankfactory);
					rc.broadcast(MINERFACTORY, numMinerfactory);
					rc.broadcast(MINER, numMiner);
					rc.broadcast(TANK, numTank);

					if (rc.isWeaponReady()) {
						attackSomething();
					}

					if (rc.isCoreReady() && rc.getTeamOre() >= 100 && rc.readBroadcast(BEAVER) < 2) {
						trySpawn(directions[rand.nextInt(8)], RobotType.BEAVER);
					}
				} catch (Exception e) {
					System.out.println("HQ Exception");
					e.printStackTrace();
				}
			}
			
			if (rc.getType() == RobotType.MINERFACTORY) {
				try {
					int fate = rand.nextInt(10000);
					if (rc.isCoreReady() && rc.getTeamOre() >= 60 && rc.readBroadcast(MINER) < 10 && fate < Math.pow(1.2,30-rc.readBroadcast(MINER))*10000) {
						trySpawn(directions[rand.nextInt(8)], RobotType.MINER);
					}
				} catch (Exception e) {
					System.out.println("Minerfactory");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.TOWER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
				} catch (Exception e) {
					System.out.println("Tower Exception");
					e.printStackTrace();
				}
			}


			if (rc.getType() == RobotType.BASHER) {
				try {
					RobotInfo[] adjacentEnemies = rc.senseNearbyRobots(2, enemyTeam);

					// BASHERs attack automatically, so let's just move around mostly randomly
					if (rc.isCoreReady()) {
						MapLocation myLoc = rc.getLocation();
						
						if(towerFocus == null){
							List<MapLocation> list = getDefenceLocations();
							towerFocus = list.get(0);
						}
						
						if (myLoc.distanceSquaredTo(towerFocus) > 2){
							// GO TO FOCUS TOWER
							tryMove(myLoc.directionTo(towerFocus));
						}
					}
				} catch (Exception e) {
					System.out.println("Basher Exception");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.SOLDIER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						MapLocation myLoc = rc.getLocation();
						
						if(towerFocus == null){
							List<MapLocation> list = getDefenceLocations();
							towerFocus = list.get(0);
						}
						
						if (myLoc.distanceSquaredTo(towerFocus) > 2){
							// GO TO FOCUS TOWER
							tryMove(myLoc.directionTo(towerFocus));
						}
					}
				} catch (Exception e) {
					System.out.println("Soldier Exception");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.BEAVER) {
				try {
					int fate = rand.nextInt(1000);
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						MapLocation myLoc = rc.getLocation();
						
						if(rc.readBroadcast(MINERFACTORY) < 1){
							if(rc.getTeamOre() >= 500){
								tryBuild(directions[rand.nextInt(8)],RobotType.MINERFACTORY);
							}
						} else if (rc.readBroadcast(MINER) > 6 && fate < 200 ) {
							if (rc.readBroadcast(BARRACK) < 1 && rc.getTeamOre() >= 300){
								tryBuild(directions[rand.nextInt(8)],RobotType.BARRACKS);
								rc.broadcast(BARRACK, rc.readBroadcast(BARRACK)+1);
							} else if (rc.readBroadcast(TANKFACTORY) < 2 && rc.getTeamOre() >= 500 && rc.readBroadcast(BARRACK) >= 1){
								tryBuild(directions[rand.nextInt(8)],RobotType.TANKFACTORY);
							}	
						} else if (rc.senseOre(myLoc) > 2) {
							rc.mine();
						} else {
							tryMove(directions[rand.nextInt(8)]);
						}	 
					}
				} catch (Exception e) {
					System.out.println("Beaver Exception");
					e.printStackTrace();
				}
			}

			if (rc.getType() == RobotType.BARRACKS) {
				try {
					int fate = rand.nextInt(10000);

					// get information broadcasted by the HQ
					int numSoldiers = rc.readBroadcast(1);
					int numBashers = rc.readBroadcast(2);

					if (rc.isCoreReady() && rc.getTeamOre() >= 60 && fate < Math.pow(1.2,3-numSoldiers-numBashers+rc.readBroadcast(MINER))*10000) {
						if (rc.getTeamOre() > 80 && fate % 2 == 0) {
							trySpawn(directions[rand.nextInt(8)],RobotType.BASHER);
						} else {
							trySpawn(directions[rand.nextInt(8)],RobotType.SOLDIER);
						}
					}
				} catch (Exception e) {
					System.out.println("Barracks Exception");
					e.printStackTrace();
				}
			}
			if (rc.getType() == RobotType.TANKFACTORY) {
				try {
					int fate = rand.nextInt(10000);
					if (rc.isCoreReady() && rc.getTeamOre() >= 250 && fate < Math.pow(1.2,20-rc.readBroadcast(TANK))*10000) {
						trySpawn(directions[rand.nextInt(8)],RobotType.TANK);
					}
				} catch (Exception e) {
					System.out.println("Barracks Exception");
					e.printStackTrace();
				}
			}
			
			if (rc.getType() == RobotType.TANK) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						MapLocation myLoc = rc.getLocation();
						
						if(towerFocus == null){
							List<MapLocation> list = getDefenceLocations();
							towerFocus = list.get(0);		
						}
						
						if (myLoc.distanceSquaredTo(towerFocus) > 2){
							// GO TO FOCUS TOWER
							tryMove(myLoc.directionTo(towerFocus));
						}	
					}
				} catch (Exception e) {
					System.out.println("Tank Exception");
					e.printStackTrace();
				}
			}
			
			if (rc.getType() == RobotType.MINER) {
				try {
					if (rc.isWeaponReady()) {
						attackSomething();
					}
					if (rc.isCoreReady()) {
						int fate = rand.nextInt(1000);
						if (rc.senseOre(rc.getLocation()) > 4 && fate < 900) {
							rc.mine();
						} else {
							tryMove(directions[rand.nextInt(8)]);
						}
					}
				} catch (Exception e) {
					System.out.println("Miner Exception");
					e.printStackTrace();
				}
			}
			
			rc.yield();
		}
	}

	// This method will attack an enemy in sight, if there is one
	static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myRange, enemyTeam);
		if (enemies.length > 0) {
			rc.attackLocation(enemies[0].location);
		}
	}

	// This method will attempt to move in Direction d (or as close to it as possible)
	static void tryMove(Direction d) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 5 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 5) {
			rc.move(directions[(dirint+offsets[offsetIndex]+8)%8]);
		}
	}

	// This method will attempt to spawn in the given direction (or as close to it as possible)
	static void trySpawn(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canSpawn(directions[(dirint+offsets[offsetIndex]+8)%8], type)) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.spawn(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}

	// This method will attempt to build in the given direction (or as close to it as possible)
	static void tryBuild(Direction d, RobotType type) throws GameActionException {
		int offsetIndex = 0;
		int[] offsets = {0,1,-1,2,-2,3,-3,4};
		int dirint = directionToInt(d);
		boolean blocked = false;
		while (offsetIndex < 8 && !rc.canMove(directions[(dirint+offsets[offsetIndex]+8)%8])) {
			offsetIndex++;
		}
		if (offsetIndex < 8) {
			rc.build(directions[(dirint+offsets[offsetIndex]+8)%8], type);
		}
	}
	
	// Return a list of defend priority
	static List<MapLocation> getDefenceLocations(){
		MapLocation[] m = rc.senseTowerLocations();
		MapLocation eHQ = rc.senseEnemyHQLocation();
		TreeMap<Integer, MapLocation> tm = new TreeMap<Integer, MapLocation>();
		tm.put(rc.senseHQLocation().distanceSquaredTo(eHQ), rc.senseHQLocation());
		if(m.length != 0){
			for (MapLocation l: m){
				tm.put(l.distanceSquaredTo(eHQ), l);
			}
		}
		return new ArrayList<MapLocation>(tm.values());
		
	}

	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}
	
	
}
