import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class Beater extends AdvancedRobot {

	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while (true) {
			setTurnRadarLeft(360);
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		
		setTurnGunLeft(getHeading() - getGunHeading() + e.getBearing());
		// if the gun is cool and we're pointed at the target, shoot!
		if (Math.abs(getGunTurnRemaining()) < 10) {
			setFire(3);
			execute();
			scan();
		}
	}

	public void onHitWall(HitWallEvent e) {

	}

	public void onHitRobot(HitRobotEvent e) {

	}

	public void onHitByBullet(HitByBulletEvent h) {

	}
}
