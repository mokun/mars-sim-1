/**
 * Mars Simulation Project
 * CompileScientificStudyResultsMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the CompileScientificStudyResults task.
 */
public class CompileScientificStudyResultsMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(CompileScientificStudyResultsMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new CompileScientificStudyResults(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	        // Add probability for researcher's primary study (if any).
	        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
	        if ((primaryStudy != null) && ScientificStudy.PAPER_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryPaperCompleted()) {
	                try {
	                    double primaryResult = 50D;

	                    // If researcher's current job isn't related to study science, divide by two.
	                    Job job = person.getMind().getJob();
	                    if (job != null) {
	                        ScienceType jobScience = ScienceType.getJobScience(job);
	                        if (!primaryStudy.getScience().equals(jobScience)) primaryResult /= 2D;
	                    }

	                    result += primaryResult;
	                }
	                catch (Exception e) {
	                    logger.severe("getProbability(): " + e.getMessage());
	                }
	            }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.PAPER_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativePaperCompleted(person)) {
	                    try {
	                        ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);

	                        double collabResult = 25D;

	                        // If researcher's current job isn't related to study science, divide by two.
	                        Job job = person.getMind().getJob();
	                        if (job != null) {
	                            ScienceType jobScience = ScienceType.getJobScience(job);
	                            if (!collabScience.equals(jobScience)) collabResult /= 2D;
	                        }

	                        result += collabResult;
	                    }
	                    catch (Exception e) {
	                        logger.severe("getProbability(): " + e.getMessage());
	                    }
	                }
	            }
	        }

	        // Crowding modifier
            Building adminBuilding = CompileScientificStudyResults.getAvailableAdministrationBuilding(person);
            if (adminBuilding != null) {

                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, adminBuilding);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, adminBuilding);
            }

	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(CompileScientificStudyResults.class);
	        }

	        // Modify if research is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Research")) {
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