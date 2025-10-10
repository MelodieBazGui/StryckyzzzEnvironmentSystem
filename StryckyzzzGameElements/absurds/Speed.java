package absurds;

import gameHandlers.MouvementHandler;
import math.Vec3;

public class Speed {
	private double time;
	private double mouvementSpeed;
	private boolean travelSpeed;
	private MouvementHandler movement;
	
	public Speed(MouvementHandler mvmnt) {
		this.movement = mvmnt;
		mouvementSpeed = 7d;
		travelSpeed = false;
	}
	
	public void makeTravelSpeed(boolean bool) {
		travelSpeed = true;
	}
	
	/**
	 * 
	 * @param time
	 * @param distance
	 * @return distance/1 if travelSpeed true
	 * @return distance/time 
	 */
	private double calculateSpeed(double distance, double time) {
		if(travelSpeed) {
			return distance/1;
		}
		return distance/time;
	}
	
	/**
	 * 
	 * @param time
	 * @return Vec3 Vector that indicates 3D space Movement
	 */
	public Vec3 getSpeedVector(double time) {
		return new Vec3(
				(float) movement.getX() * calculateSpeed(mouvementSpeed,time) * movement.getMouvementSpeedMultiplier(),
				(float) movement.getY() * calculateSpeed(mouvementSpeed,time) * movement.getMouvementSpeedMultiplier(),
				(float) movement.getZ() * calculateSpeed(mouvementSpeed,time) * movement.getMouvementSpeedMultiplier()
				);
				
	}
}
