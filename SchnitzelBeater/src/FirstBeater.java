import java.awt.geom.Point2D;

import robocode.*;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class FirstBeater extends AdvancedRobot {

	private byte scanDirection = 1;
	int count = 0; // Keeps track of how long we've
	// been searching for our target
	double gunTurnAmt; // How much to turn our gun when searching
	String trackName; // Name of the robot we're currently tracking

	public void run() {
		// Prepare gun
		trackName = null; // Initialize to not tracking anyone
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		gunTurnAmt = 10; // Initialize gunTurn to 10
		while (true) {
			// Tell the game that when we take move,
			// we'll also want to turn right... a lot.
			setTurnRight(100000);
			// Limit our speed to 5
			setMaxVelocity(6);
			// Start moving (and turning)
			ahead(100000);
			// Repeat.
			// turn the Gun (looks for enemy)

			setTurnGunRight(10000);
			// Keep track of how long we've been looking
//			count++;
//			// If we've haven't seen our target for 2 turns, look left
//			if (count > 2) {
//				gunTurnAmt = -10;
//			}
//			// If we still haven't seen our target for 5 turns, look right
//			if (count > 5) {
//				gunTurnAmt = 10;
//			}
//			// If we *still* haven't seen our target after 10 turns, find another target
//			if (count > 11) {
//				trackName = null;
//			}
			scan();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

		
		if (e.getDistance() > 250) {
			gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));

			setTurnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
			setTurnRight(e.getBearing()); // and see how much Tracker improves...
											// (you'll have to make Tracker an AdvancedRobot)
			ahead(e.getDistance() - 210);
			execute();
			scan();
			return;
		} else {

			// If it's close enough, fire!
			if (Math.abs(bearingFromGun) <= 3) {
				turnGunRight(bearingFromGun);
				// We check gun heat here, because calling fire()
				// uses a turn, which could cause us to lose track
				// of the other robot.
				if (getGunHeat() == 0) {
					fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
				}
			} // otherwise just set the gun to turn.
				// Note: This will have no effect until we call scan()
			else {
				setTurnGunRight(bearingFromGun);
				execute();
				scan();
			}

			if (bearingFromGun == 0) {
				scan();
			}

			// // Our target is close.
			// gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
			// turnGunRight(gunTurnAmt);
			// fire(3);
		}
		// Our target is too close! Back up.
		if (e.getDistance() < 100) {
			if (e.getBearing() > -90 && e.getBearing() <= 90) {
				setBack(40);
				execute();
				scan();
			} else {
				setAhead(40);
				execute();
				scan();
			}
		}
		scan();

	}

	public void analyzeSituation() {

	}

	public void onHitWall(HitWallEvent e) {
		double bearing = e.getBearing(); // get the bearing of the wall
		turnRight(-bearing); // This isn't accurate but release your robot.
		ahead(100); // The robot goes away from the wall.
	}

	public void onHitRobot(HitRobotEvent e) {
		// Only print if he's not already our target.
		if (trackName != null && !trackName.equals(e.getName())) {
			out.println("Tracking " + e.getName() + " due to collision");
		}
		// Set the target
		trackName = e.getName();
		// Back up a bit.
		// Note: We won't get scan events while we're doing this!
		// An AdvancedRobot might use setBack(); execute();
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		turnGunRight(gunTurnAmt);
		fire(3);
		setBack(50);
		execute();
		scan();
	}

	public void onHitByBullet(HitByBulletEvent h, ScannedRobotEvent e) {
		double previousEnergy = 100;
		int movementDirection = 1;
		int gunDirection = 1;
		// double energy = getEnergy();
		// double bearing = e.getBearing(); // Get the direction which is arrived the
		// bullet.
		// if (energy < 100) { // if the energy is low, the robot go away from the enemy
		// turnRight(-bearing); // This isn't accurate but releases your robot.
		// ahead(100); // The robot goes away from the enemy.
		// } else
		// turnRight(360); // scan

		// Stay at right angles to the opponent
		setTurnRight(e.getBearing() + 90 - 30 * movementDirection);

		// If the bot has small energy drop,
		// assume it fired
		double changeInEnergy = previousEnergy - e.getEnergy();
		if (changeInEnergy > 0 && changeInEnergy <= 3) {
			// Dodge!
			movementDirection = -movementDirection;
			setAhead((e.getDistance() / 4 + 25) * movementDirection);
		}
		// When a bot is spotted,
		// sweep the gun and radar
		gunDirection = -gunDirection;
		setTurnGunRight(99999 * gunDirection);

		// Fire directly at target
		fire(3);

		// Track the energy level
		previousEnergy = e.getEnergy();
	}

	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	// computes the absolute bearing between two points
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}
}
