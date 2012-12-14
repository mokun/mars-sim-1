/**
 * Mars Simulation Project
 * ConstructionSite.java
 * @version 3.04 2012-12-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.MarsClock;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A building construction site.
 */
public class ConstructionSite implements Serializable {
    
    // Construction site events.
    public static final String START_UNDERGOING_CONSTRUCTION_EVENT = "start undergoing construction";
    public static final String END_UNDERGOING_CONSTRUCTION_EVENT = "end undergoing construction";
    public static final String START_UNDERGOING_SALVAGE_EVENT = "start undergoing salvage";
    public static final String END_UNDERGOING_SALVAGE_EVENT = "end undergoing salvage";
    public static final String ADD_CONSTRUCTION_STAGE_EVENT = "adding construction stage";
    public static final String REMOVE_CONSTRUCTION_STAGE_EVENT = "removing construction stage";
    public static final String CREATE_BUILDING_EVENT = "creating new building";
    public static final String REMOVE_BUILDING_EVENT = "removing old building";
    
    // Data members
    private double width;
    private double length;
    private double xLocation;
    private double yLocation;
    private double facing;
    private ConstructionStage foundationStage;
    private ConstructionStage frameStage;
    private ConstructionStage buildingStage;
    private boolean undergoingConstruction;
    private boolean undergoingSalvage;
    private transient List<ConstructionListener> listeners;
    
    /**
     * Constructor
     */
    ConstructionSite() {
        width = 0D;
        length = 0D;
        xLocation = 0D;
        yLocation = 0D;
        facing = 0D;
        foundationStage = null;
        frameStage = null;
        buildingStage = null;
        undergoingConstruction = false;
        undergoingSalvage = false;
        listeners = Collections.synchronizedList(new ArrayList<ConstructionListener>());
    }
    
    /**
     * Gets the width of the construction site.
     * @return width (meters).
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Sets the width of the construction site.
     * @param width the width (meters).
     */
    public void setWidth(double width) {
        this.width = width;
    }
    
    /**
     * Gets the length of the construction site.
     * @return length (meters).
     */
    public double getLength() {
        return length;
    }
    
    /**
     * Sets the length of the construction site.
     * @param length the length (meters).
     */
    public void setLength(double length) {
        this.length = length;
    }
    
    /**
     * Gets the X location of the construction site.
     * @return x location in meters from center of settlement (West: positive, East: negative).
     */
    public double getXLocation() {
        return xLocation;
    }
    
    /**
     * Sets the X location of the construction site.
     * @param xLocation x location in meters from center of settlement (West: positive, East: negative).
     */
    public void setXLocation(double xLocation) {
        this.xLocation = xLocation;
    }
    
    /**
     * Gets the Y location of the construction site.
     * @return y location in meters from center of settlement (North: positive, South: negative).
     */
    public double getYLocation() {
        return yLocation;
    }
    
    /**
     * Sets the Y location of the construction site.
     * @param yLocation y location in meters from center of settlement (North: positive, South: negative).
     */
    public void setYLocation(double yLocation) {
        this.yLocation = yLocation;
    }
    
    /**
     * Gets the facing of the construction site.
     * @return facing in degrees clockwise from North.
     */
    public double getFacing() {
        return facing;
    }
    
    /**
     * Sets the facing of the construction site.
     * @param facing
     */
    public void setFacing(double facing) {
        this.facing = facing;
    }
    
    /**
     * Checks if all construction is complete at the site.
     * @return true if construction is complete.
     */
    public boolean isAllConstructionComplete() {
        if ((buildingStage != null) && !undergoingSalvage) return buildingStage.isComplete();
        else return false;
    }
    
    /**
     * Checks if all salvage is complete at the site.
     * @return true if salvage is complete.
     */
    public boolean isAllSalvageComplete() {
        if (undergoingSalvage) {
            if (foundationStage == null) return true;
            else return foundationStage.isComplete();
        }
        else return false;
    }
    
    /**
     * Checks if site is currently undergoing construction.
     * @return true if undergoing construction.
     */
    public boolean isUndergoingConstruction() {
        return undergoingConstruction;
    }
    
    /**
     * Checks if site is currently undergoing salvage.
     * @return true if undergoing salvage.
     */
    public boolean isUndergoingSalvage() {
        return undergoingSalvage;
    }
    
    /**
     * Sets if site is currently undergoing construction.
     * @param undergoingConstruction true if undergoing construction.
     */
    public void setUndergoingConstruction(boolean undergoingConstruction) {
        this.undergoingConstruction = undergoingConstruction;
        if (undergoingConstruction) fireConstructionUpdate(START_UNDERGOING_CONSTRUCTION_EVENT);
        else fireConstructionUpdate(END_UNDERGOING_CONSTRUCTION_EVENT);
    }
    
    /**
     * Sets if site is currently undergoing salvage.
     * @param undergoingSalvage true if undergoing salvage.
     */
    public void setUndergoingSalvage(boolean undergoingSalvage) {
        this.undergoingSalvage = undergoingSalvage;
        if (undergoingSalvage) fireConstructionUpdate(START_UNDERGOING_SALVAGE_EVENT);
        else fireConstructionUpdate(END_UNDERGOING_SALVAGE_EVENT);
    }
    
    /**
     * Gets the current construction stage at the site.
     * @return construction stage.
     */
    public ConstructionStage getCurrentConstructionStage() {
        ConstructionStage result = null;
        
        if (buildingStage != null) result = buildingStage;
        else if (frameStage != null) result = frameStage;
        else if (foundationStage != null) result = foundationStage;
        
        return result;
    }
    
    /**
     * Gets the next construction stage type.
     * @return next construction stage type or null if none.
     */
    public String getNextStageType() {
        String result = null;
        
        if (buildingStage != null) result = null;
        else if (frameStage != null) result = ConstructionStageInfo.BUILDING;
        else if (foundationStage != null) result = ConstructionStageInfo.FRAME;
        else result = ConstructionStageInfo.FOUNDATION;
        
        return result;
    }
    
    /**
     * Adds a new construction stage to the site.
     * @param stage the new construction stage.
     * @throws Exception if error adding construction stage.
     */
    public void addNewStage(ConstructionStage stage) {
        if (ConstructionStageInfo.FOUNDATION.equals(stage.getInfo().getType())) {
            if (foundationStage != null) throw new IllegalStateException("Foundation stage already exists.");
            foundationStage = stage;
        }
        else if (ConstructionStageInfo.FRAME.equals(stage.getInfo().getType())) {
            if (frameStage != null) throw new IllegalStateException("Frame stage already exists");
            if (foundationStage == null) throw new IllegalStateException("Foundation stage hasn't been added yet.");
            frameStage = stage;
        }
        else if (ConstructionStageInfo.BUILDING.equals(stage.getInfo().getType())) {
            if (buildingStage != null) throw new IllegalStateException("Building stage already exists");
            if (frameStage == null) throw new IllegalStateException("Frame stage hasn't been added yet.");
            buildingStage = stage;
        }
        else throw new IllegalStateException("Stage type: " + stage.getInfo().getType() + " not valid");
        
        // Update construction site dimensions.
        updateDimensions(stage);
        
        // Fire construction event.
        fireConstructionUpdate(ADD_CONSTRUCTION_STAGE_EVENT, stage);
    }
    
    /**
     * Updates the width and length dimensions to a construction stage.
     * @param stage the construction stage.
     */
    private void updateDimensions(ConstructionStage stage) {
        if (!stage.getInfo().isUnsetDimensions()) {
            if (stage.getInfo().getWidth() != width) width = stage.getInfo().getWidth();
            if (stage.getInfo().getLength() != length) length = stage.getInfo().getLength();
        }
    }
    
    /**
     * Remove a salvaged stage from the construction site.
     * @param stage the salvaged construction stage.
     * @throws Exception if error removing the stage.
     */
    public void removeSalvagedStage(ConstructionStage stage) {
        if (ConstructionStageInfo.BUILDING.equals(stage.getInfo().getType())) {
            buildingStage = null;
        }
        else if (ConstructionStageInfo.FRAME.equals(stage.getInfo().getType())) {
            frameStage = null;
        }
        else if (ConstructionStageInfo.FOUNDATION.equals(stage.getInfo().getType())) {
            foundationStage = null;
        }
        else throw new IllegalStateException("Stage type: " + stage.getInfo().getType() + " not valid");
        
        // Fire construction event.
        fireConstructionUpdate(REMOVE_CONSTRUCTION_STAGE_EVENT, stage);
    }
    
    /**
     * Removes the current salvaged construction stage.
     * @throws Exception if error removing salvaged construction stage.
     */
    public void removeSalvagedStage() {
        if (undergoingSalvage) {
            if (buildingStage != null) buildingStage = null;
            else if (frameStage != null) frameStage = null;
            else if (foundationStage != null) foundationStage = null;
            else throw new IllegalStateException("Construction site has no stage to remove");
        }
        else throw new IllegalStateException("Construction site is not undergoing salvage");
    }
    
    /**
     * Creates a new building from the construction site.
     * @param manager the settlement's building manager.
     * @return newly constructed building.
     * @throws Exception if error constructing building.
     */
    public Building createBuilding(BuildingManager manager) {
        if (buildingStage == null) throw new IllegalStateException("Building stage doesn't exist");
        
        Building newBuilding = new Building(buildingStage.getInfo().getName(), xLocation,
                yLocation, facing, manager);
        manager.addBuilding(newBuilding);
        
        // Record completed building name.
        ConstructionManager constructionManager = manager.getSettlement().getConstructionManager();
        MarsClock timeStamp = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
        constructionManager.addConstructedBuildingLogEntry(buildingStage.getInfo().getName(), timeStamp);
        
        // Clear construction value cache.
        constructionManager.getConstructionValues().clearCache();
        
        // Fire construction event.
        fireConstructionUpdate(CREATE_BUILDING_EVENT, newBuilding);
        
        return newBuilding;
    }
    
    /**
     * Gets the building name the site will construct.
     * @return building name or null if undetermined.
     */
    public String getBuildingName() {
        if (buildingStage != null) return buildingStage.getInfo().getName();
        else return null;
    }
    
    /**
     * Checks if the site's current stage is unfinished.
     * @return true if stage unfinished.
     */
    public boolean hasUnfinishedStage() {
        ConstructionStage currentStage = getCurrentConstructionStage();
        return (currentStage != null) && !currentStage.isComplete();
    }
    
    /**
     * Checks if this site contains a given stage.
     * @param stage the stage info.
     * @return true if contains stage.
     */
    public boolean hasStage(ConstructionStageInfo stage) {
        if (stage == null) throw new IllegalArgumentException("stage cannot be null");
        
        boolean result = false;
        if ((foundationStage != null) && foundationStage.getInfo().equals(stage)) result = true;
        else if ((frameStage != null) && frameStage.getInfo().equals(stage)) result = true;
        else if ((buildingStage != null) && buildingStage.getInfo().equals(stage)) result = true;
        
        return result;
    }
    
    /**
     * Gets a random location within the construction site.
     * @return Point containing the X and Y locations relative to the construction site.
     */
    public Point2D.Double getRandomLocationInsideSite() {
        
        double xLoc = RandomUtil.getRandomDouble(getWidth()) - (getWidth() / 2D);
        double yLoc = RandomUtil.getRandomDouble(getLength()) - (getLength() / 2D);
        
        return new Point2D.Double(xLoc, yLoc);
    }
    
    /**
     * Gets a settlement relative location from a location relative to this building.
     * @param xLoc the X location relative to this building.
     * @param yLoc the Y location relative to this building.
     * @return Point containing the X and Y locations relative to the settlement.
     */
    public Point2D.Double getSettlementRelativeLocation(double xLoc, double yLoc) {
        Point2D.Double result = new Point2D.Double();
        
        double radianRotation = (getFacing() * (Math.PI / 180D));
        double rotateX = (xLoc * Math.cos(radianRotation)) - (yLoc * Math.sin(radianRotation));
        double rotateY = (xLoc * Math.sin(radianRotation)) + (yLoc * Math.cos(radianRotation));
        
        double translateX = rotateX + getXLocation();
        double translateY = rotateY + getYLocation();
        
        result.setLocation(translateX, translateY);
        
        return result;
    }
    
    /**
     * Adds a listener
     * @param newListener the listener to add.
     */
    public final void addConstructionListener(ConstructionListener newListener) {
        if (listeners == null) 
            listeners = Collections.synchronizedList(new ArrayList<ConstructionListener>());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Removes a listener
     * @param oldListener the listener to remove.
     */
    public final void removeConstructionListener(ConstructionListener oldListener) {
        if (listeners == null) 
            listeners = Collections.synchronizedList(new ArrayList<ConstructionListener>());
        if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }
    
    /**
     * Fire a construction update event.
     * @param updateType the update type.
     */
    final void fireConstructionUpdate(String updateType) {
        fireConstructionUpdate(updateType, null);
    }
    
    /**
     * Fire a construction update event.
     * @param updateType the update type.
     * @param target the event target or null if none.
     */
    final void fireConstructionUpdate(String updateType, Object target) {
        if (listeners == null) 
            listeners = Collections.synchronizedList(new ArrayList<ConstructionListener>());
        synchronized(listeners) {
            Iterator<ConstructionListener> i = listeners.iterator();
            while (i.hasNext()) i.next().constructionUpdate(
                    new ConstructionEvent(this, updateType, target));
        }
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Site");
        
        ConstructionStage stage = getCurrentConstructionStage();
        if (stage != null) {
            result.append(": ").append(stage.getInfo().getName());
            if (undergoingConstruction) result.append(" - under construction");
            else if (undergoingSalvage) result.append(" - under salvage");
            else if (hasUnfinishedStage()) {
                if (stage.isSalvaging()) result.append(" - salvage unfinished");
                else result.append(" - construction unfinished");
            }
        }
        
        return result.toString();
    }
}