/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVAMeta.java
 * @version 3.08 2015-05-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Iterator;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleEVA task.
 */
public class MaintainGroundVehicleEVAMeta implements MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleEVA"); //$NON-NLS-1$

    private SurfaceFeatures surface;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MaintainGroundVehicleEVA(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person) == null)
        	result = 0D;

        // Check if it is night time.
        if (surface == null)
        	surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getPreviousSolarIrradiance(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
        }

        if (result != 0)
	        // Determine if settlement has a garage.
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT)
	            if (person.getSettlement().getBuildingManager().getBuildings(
	                    BuildingFunction.GROUND_VEHICLE_MAINTENANCE).size() > 0) {

            	Settlement settlement = person.getSettlement();
            	if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity())
		                result *= 2D;

		        // Get all vehicles needing maintenance.
		        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
		            Iterator<Vehicle> i = MaintainGroundVehicleEVA.getAllVehicleCandidates(person).iterator();
		            while (i.hasNext()) {
		                MalfunctionManager manager = i.next().getMalfunctionManager();
		                double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 50D);
		                if (entityProb > 100D) {
		                    entityProb = 100D;
		                }
		                result += entityProb;
		            }
		        }

		        // Effort-driven task modifier.
		        result *= person.getPerformanceRating();

		        // Job modifier.
		        Job job = person.getMind().getJob();
		        if (job != null) {
		            result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleEVA.class);
		        }

		        // Modify if tinkering is the person's favorite activity.
		        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Tinkering")) {
		            result *= 2D;
		        }

	            // 2015-06-07 Added Preference modifier
	            if (result > 0)
	            	result += person.getPreference().getPreferenceScore(this);
	            if (result < 0) result = 0;

            }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}