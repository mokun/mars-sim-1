/**
 * Mars Simulation Project
 * ToggleFuelPowerSourceMeta.java
 * @version 3.08 2015-06-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.ToggleFuelPowerSource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.FuelPowerSource;

/**
 * Meta task for the ToggleFuelPowerSource task.
 */
public class ToggleFuelPowerSourceMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.toggleFuelPowerSource"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ToggleFuelPowerSource(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            boolean isEVA = false;

            Settlement settlement = person.getSettlement();

            try {
                Building building = ToggleFuelPowerSource.getFuelPowerSourceBuilding(person);
                if (building != null) {
                    FuelPowerSource powerSource = ToggleFuelPowerSource.getFuelPowerSource(building);
                    isEVA = !building.hasFunction(BuildingFunction.LIFE_SUPPORT);
                    double diff = ToggleFuelPowerSource.getValueDiff(settlement, powerSource);
                    double baseProb = diff * 10000D;
                    if (baseProb > 100D) {
                        baseProb = 100D;
                    }
                    result += baseProb;

                    if (!isEVA) {
                        // Factor in building crowding and relationship factors.
                        result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                        result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }

            if (isEVA) {
                // Check if an airlock is available
                if (EVAOperation.getWalkableAvailableAirlock(person) == null) {
                    result = 0D;
                }

                // Check if it is night time.
                SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
                if (surface.getSolarIrradiance(person.getCoordinates()) == 0D) {
                    if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                        result = 0D;
                    }
                }

                // Crowded settlement modifier
                if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                    if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                        result *= 2D;
                    }
                }
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(ToggleFuelPowerSource.class);
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