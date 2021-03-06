/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The PowerStorage class is a building function for storing power.
 */
public class PowerStorage
extends Function
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Building function name.
	private static final BuildingFunction FUNCTION = BuildingFunction.POWER_STORAGE;

	// Data members.
	private double powerStorageCapacity;
	private double powerStored;

	/**
	 * Constructor.
	 * @param building the building with the function.
	 * @throws BuildingException if error parsing configuration.
	 */
	public PowerStorage(Building building) {
		// Call Function constructor.
		super(FUNCTION, building);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

		powerStorageCapacity = config.getPowerStorageCapacity(building.getBuildingType());
	}

	/**
	 * Gets the value of the function for a named building.
	 * @param buildingName the building name.
	 * @param newBuilding true if adding a new building.
	 * @param settlement the settlement.
	 * @return value (VP) of building function.
	 * @throws Exception if error getting function value.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding,
			Settlement settlement) {

		PowerGrid grid = settlement.getPowerGrid();

		double hrInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
		double demand = grid.getRequiredPower() * hrInSol;

		double supply = 0D;
		Iterator<Building> iStore = settlement.getBuildingManager().getBuildings(PowerStorage.FUNCTION).iterator();
		while (iStore.hasNext()) {
			Building building = iStore.next();
			PowerStorage store = (PowerStorage) building.getFunction(PowerStorage.FUNCTION);
			double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
			supply += store.powerStorageCapacity * wearModifier;
		}

		double existingPowerStorageValue = demand / (supply + 1D);

		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		double powerStorage = config.getPowerStorageCapacity(buildingName);

		double value = powerStorage * existingPowerStorageValue / hrInSol;
		if (value > 10D) value = 10D;

		return value;
	}

	/**
	 * Gets the building's power storage capacity.
	 * @return capacity (kW hr).
	 */
	public double getPowerStorageCapacity() {
		return powerStorageCapacity;
	}

	/**
	 * Gets the building's stored power.
	 * @return power (kW hr).
	 */
	public double getPowerStored() {
		return powerStored;
	}

	/**
	 * Sets the power stored in the building.
	 * @param powerStored the stored power (kW hr).
	 */
	public void setPowerStored(double powerStored) {
		if (powerStored > powerStorageCapacity) powerStored = powerStorageCapacity;
		else if (powerStored < 0D) powerStored = 0D;
		this.powerStored = powerStored;
	}

	@Override
	public double getFullPowerRequired() {
		return 0;
	}

	@Override
	public double getPoweredDownPowerRequired() {
		return 0;
	}

	@Override
	public void timePassing(double time) {
		// Do nothing.
	}

	@Override
	public double getMaintenanceTime() {
		return powerStorageCapacity / 5D;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}