/**
 * Mars Simulation Project
 * CookMeal.java
 * @version 3.08 2015-04-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The CookMeal class is a task for cooking meals in a building
 * with the Cooking function.
 * This is an effort driven task.
 */
public class CookMeal
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(CookMeal.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COOKING = new TaskPhase(Msg.getString(
            "Task.phase.cooking")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Starting meal times (millisol) for 0 degrees longitude.
	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
	private static final double BREAKFAST_START = 250D; // at 6am
	private static final double LUNCH_START = 500D; // at 12 am
	private static final double DINNER_START = 750D; // at 6 pm
	private static final double MIDNIGHT_SHIFT_MEAL_START = 005D; // avoid conflict with TabPanelCooking when at 0D all yesterday's cookedMeals are removed

	// Time (millisols) duration of meals.
	private static final double MEALTIME_DURATION = 100D; // 250 milliSol = 6 hours

	// Data members
	/** The kitchen the person is cooking at. */
	private Cooking kitchen;

	private int counter;
	private int solElapsedCache;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public CookMeal(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription(Msg.getString("Task.description.cookMeal.detail",
                getMealName())); //$NON-NLS-1$

        // Get an available kitchen.
        Building kitchenBuilding = getAvailableKitchen(person);

	    if (kitchenBuilding != null) {
	    	kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);

	        // Walk to kitchen building.
	    	walkToActivitySpotInBuilding(kitchenBuilding, false);

		    double size = kitchen.getMealRecipesWithAvailableIngredients().size();
	        if (size == 0) {
	        	counter++;
	        	boolean display = false;

	        	// display the msg when no ingredients are detected at first and after 15 warnings
	        	if (counter > 30 || counter == 0)
	        		display = true;

	        	if (display) {
	        		logger.severe("Warning: cannot cook meals in "
	            		+ kitchenBuilding.getBuildingManager().getSettlement().getName()
	            		+ " because none of the ingredients of a meal are available ");
		            //counter = 0;
	        	}

	            // 2015-01-15 Added solElapsed
	            MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
	            int solElapsed = MarsClock.getSolOfYear(marsClock);

	            if (solElapsed != solElapsedCache) {
	            	counter = 0;
	            	solElapsedCache = solElapsed;

	            }
	            endTask();
		    }
	        else {

		    	counter = 0;

		    	// Set the chef name at the kitchen.
				kitchen.setChef(person.getName());

		    	// Add task phase
			    addPhase(COOKING);
				setPhase(COOKING);

				String jobName = person.getMind().getJob().getName(person.getGender());
				logger.finest(jobName + " " + person.getName() + " cooking at " + kitchen.getBuilding().getNickName() +
				    	                " in " + person.getSettlement());
		    }
	    }
	    else {
	        endTask();
	    }
    }

	public CookMeal(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, false, STRESS_MODIFIER, false, 0D);

        //logger.info("just called CookMeal's constructor");

        // Initialize data members
        setDescription(Msg.getString("Task.description.cookMeal.detail",
                getMealName())); //$NON-NLS-1$

        // Get available kitchen if any.
        Building kitchenBuilding = getAvailableKitchen(robot);

	    if (kitchenBuilding != null) {
	    	kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);

	        // Walk to kitchen building.
	    	walkToActivitySpotInBuilding(kitchenBuilding, false);

		    double size = kitchen.getMealRecipesWithAvailableIngredients().size();
	        if (size == 0) {
	        	counter++;
	        	if (counter < 2)
	        		logger.severe("Warning: cannot cook meals in "
	            		+ kitchenBuilding.getBuildingManager().getSettlement().getName()
	            		+ " because none of the ingredients of a meal are available ");

	            endTask();

		    }
	        else {

		    	counter = 0;
				// 2015-01-06
				kitchen.setChef(robot.getName());

		    	// Add task phase
			    addPhase(COOKING);
				setPhase(COOKING);

				String jobName = RobotJob.getName(robot.getRobotType());
				logger.finest(jobName + " " + robot.getName() + " cooking at " + kitchen.getBuilding().getNickName() +
				    	                " in " + robot.getSettlement());

		    }
	    }
	    else endTask();

    }

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.COOKING;
    }

    @Override
    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return BuildingFunction.COOKING;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("The Cooking task phase is null");
        }
        else if (COOKING.equals(getPhase())) {
            return cookingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the cooking phase of the task.
     * @param time the amount of time (millisol) to perform the cooking phase.
     * @return the amount of time (millisol) left after performing the cooking phase.
     */
    private double cookingPhase(double time) {
    	//System.out.println("CookMeal.java entering cookingPhase() ");

        // If kitchen has malfunction, end task.
        if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            //System.out.println(person + " ending cooking due to malfunction.");
            return time;
        }

		if (person != null) {
	        // If meal time is over, end task.
	        if (!isMealTime(person.getCoordinates())) {
	            logger.finest(person + " ending cooking due to meal time over.");
	        	endTask();
	            return time;
	        }

	        // If enough meals have been cooked for this meal, end task.
	        if (kitchen.getCookNoMore()) {
	            logger.finest(person + " ending cooking due cook no more.");
	            endTask();
	            return time;
	        }
		}
		else if (robot != null) {
	        // If meal time is over, end task.
	        if (!isMealTime(robot)) {
	            logger.finest(robot + " ending cooking due to meal time over.");
	        	endTask();
	            return time;
	        }

	        // If enough meals have been cooked for this meal, end task.
            if (kitchen.getCookNoMore()) {
                logger.finest(robot + " ending cooking due cook no more.");
                endTask();
                return time;
            }
		}

        double workTime = time;

        if (robot != null) {
		     // A robot moves slower than a person and incurs penalty on workTime
	        workTime = time/2;
		}

		// Determine amount of effective work time based on "Cooking" skill.
	    int cookingSkill = getEffectiveSkillLevel();
	    if (cookingSkill == 0) {
	        workTime /= 2;
	    }
	    else {
	        workTime += workTime * (.2D * (double) cookingSkill);
	    }

	    // Add this work to the kitchen.
	    kitchen.addWork(workTime);

	    // Add experience
	    addExperience(time);

	    // Check for accident in kitchen.
	    checkForAccident(time);

        return 0D;
    }

    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
    protected void addExperience(double time) {
        // Add experience to "Cooking" skill
        // (1 base experience point per 25 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 25D;
        int experienceAptitude = 0;

		if (person != null) {
			experienceAptitude = person.getNaturalAttributeManager().getAttribute(
	                NaturalAttribute.EXPERIENCE_APTITUDE);
		}
		else if (robot != null) {
			experienceAptitude = robot.getNaturalAttributeManager().getAttribute(
                NaturalAttribute.EXPERIENCE_APTITUDE);
		}

        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();

		if (person != null) {
			person.getMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
		}
		else if (robot != null) {
			robot.getBotMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
		}
    }

    /**
     * Gets the kitchen the person is cooking in.
     * @return kitchen
     */
    public Cooking getKitchen() {
        return kitchen;
    }

    /**
     * Check for accident in kitchen.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;
        int skill = 0;

		if (person != null)
	        // Cooking skill modification.
	        skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
		else if (robot != null)
	        skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the kitchen building's wear condition.
        chance *= kitchen.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
			if (person != null) {
	            logger.info(person.getName() + " has accident while cooking.");
			}
			else if (robot != null) {
				logger.info(robot.getName() + " has accident while cooking.");
			}

            kitchen.getBuilding().getMalfunctionManager().accident();
        }
    }

    /**
     * Checks if it is currently a meal time at the location.
     * @param location the coordinate location to check for.
     * @return true if meal time
     */
    public static boolean isMealTime(Coordinates location) {
        double timeDiff = 1000D * (location.getTheta() / (2D * Math.PI));
	    return mealTime(timeDiff);
    }

    public static boolean isMealTime(Robot robot) {
        double timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));
		return mealTime(timeDiff);
    }

    public static boolean mealTime(double timeDiff) {

        boolean result = false;
        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        double modifiedTime = timeOfDay + timeDiff;
        if (modifiedTime >= 1000D) {
            modifiedTime -= 1000D;
        }

        if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
            result = true;
        }
        if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
            result = true;
        }
        if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
            result = true;
        }
    	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
        if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START) && (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
            result = true;
        }
        return result;
    }

    /**
     * Gets the name of the meal the person is cooking based on the time.
     * @return mean name ("Breakfast", "Lunch" or "Dinner) or empty string if none.
     */
    private String getMealName() {
        String result = "";
        double timeDiff = 0;

		if (person != null)
	        timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
		else if (robot != null)
			timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));

        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();

        double modifiedTime = timeOfDay + timeDiff;
        if (modifiedTime >= 1000D) {
            modifiedTime -= 1000D;
        }

        if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
            result = "Breakfast";
        }
        if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
            result = "Lunch";
        }
        if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
            result = "Dinner";
        }
    	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
        if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START) && (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
            result = "Midnight Meal";
        }

        return result;
    }

    /**
     * Gets an available kitchen building at the person's settlement.
     * @param person the person to check for.
     * @return kitchen building or null if none available.
     */
    public static Building getAvailableKitchen(Person person) {
        Building result = null;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(BuildingFunction.COOKING);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
            kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(kitchenBuildings);

            if (kitchenBuildings.size() > 0) {

                Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, kitchenBuildings);

                result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
            }
        }

        return result;
    }

    public static Building getAvailableKitchen(Robot robot) {
        Building result = null;

        LocationSituation location = robot.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = robot.getSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(BuildingFunction.COOKING);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
            kitchenBuildings = BuildingManager.getEvenNumOfBotsForBuildings(kitchenBuildings);

            if (kitchenBuildings.size() > 0) {
                //Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                       // robot, kitchenBuildings);
                //result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
              	int selected = RandomUtil.getRandomInt(kitchenBuildings.size()-1);
            	result = kitchenBuildings.get(selected);
            }
        }

        return result;
    }
    /**
     * Gets a list of kitchen buildings that have room for more cooks.
     * @param kitchenBuildings list of kitchen buildings
     * @return list of kitchen buildings
     * @throws BuildingException if error
     */
    private static List<Building> getKitchensNeedingCooks(List<Building> kitchenBuildings) {
        List<Building> result = new ArrayList<Building>();

        if (kitchenBuildings != null) {
            Iterator<Building> i = kitchenBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
                if (kitchen.getNumCooks() < kitchen.getCookCapacity()) {
                    result.add(building);
                }
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = null;
		if (person != null) {
			manager = person.getMind().getSkillManager();
		}
		else if (robot != null) {
			manager = robot.getBotMind().getSkillManager();
		}

        return manager.getEffectiveSkillLevel(SkillType.COOKING);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.COOKING);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        kitchen = null;
    }
}