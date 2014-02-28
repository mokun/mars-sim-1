package org.mars_sim.msp.ui.ogl.sandbox.scene.sphere;

import org.mars_sim.msp.ui.ogl.sandbox.scene.RotatingObjectAbstract;

/**
 * an abstract sphere.
 * @author stpa
 */
public abstract class SphereAbstract
extends RotatingObjectAbstract {

	public SphereAbstract(
		double[] center,
		double[] rotation,
		double[] deltaRotation,
		double radius
	) {
		super(center,rotation, deltaRotation);
		this.setRadius(radius);
	}

	public double getDiameter() {
		return 2d * this.getRadius();
	}
	
	public void setDiameter(double diameter) {
		this.setRadius(0.5d * diameter);
	}

	public double getEquator() {
		return Math.PI * 2d * this.getRadius();
	}

	public void setEquator(double equator) {
		this.setRadius(0.5d * equator / Math.PI);
	}
	
	public double getVolume() {
		return 4d * Math.PI * Math.pow(this.getRadius(),3) / 3d;
	}
	
	public void setVolume(double volume) {
		this.setRadius(Math.pow(3d * volume / (4d * Math.PI),1d / 3d)); 
	}
	
	public double[] getCenter() {
		return this.getTranslation();
	}

	public double getRadius() {
		return this.getParamDouble(PARAM_RADIUS);
	}

	public void setCenter(double[] center) {
		this.setTranslation(center);
	}

	public void setRadius(double radius) {
		this.setParam(PARAM_RADIUS,radius);
	}
}
