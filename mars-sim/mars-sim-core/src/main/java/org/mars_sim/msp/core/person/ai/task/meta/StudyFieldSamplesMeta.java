/**
 * Mars Simulation Project
 * StudyFieldSamplesMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

/**
 * Meta task for the StudyFieldSamples task.
 */
public class StudyFieldSamplesMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.studyFieldSamples"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(StudyFieldSamplesMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new StudyFieldSamples(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Create list of possible sciences for studying field samples.
        List<ScienceType> fieldSciences = StudyFieldSamples.getFieldSciences();

        // Add probability for researcher's primary study (if any).
        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
            if (!primaryStudy.isPrimaryResearchCompleted()) {
                if (fieldSciences.contains(primaryStudy.getScience())) {
                    try {
                        Lab lab = StudyFieldSamples.getLocalLab(person, primaryStudy.getScience());
                        if (lab != null) {
                            double primaryResult = 50D;

                            // Get lab building crowding modifier.
                            primaryResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);

                            // If researcher's current job isn't related to study science, divide by two.
                            Job job = person.getMind().getJob();
                            if (job != null) {
                                ScienceType jobScience = ScienceType.getJobScience(job);
                                if (!primaryStudy.getScience().equals(jobScience)) {
                                    primaryResult /= 2D;
                                }
                            }

                            result += primaryResult;
                        }
                    }
                    catch (Exception e) {
                        logger.severe("getProbability(): " + e.getMessage());
                    }
                }
            }
        }

        // Add probability for each study researcher is collaborating on.
        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
        while (i.hasNext()) {
            ScientificStudy collabStudy = i.next();
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
                    ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);
                    if (fieldSciences.contains(collabScience)) {
                        try {
                            Lab lab = StudyFieldSamples.getLocalLab(person, collabScience);
                            if (lab != null) {
                                double collabResult = 25D;

                                // Get lab building crowding modifier.
                                collabResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);

                                // If researcher's current job isn't related to study science, divide by two.
                                Job job = person.getMind().getJob();
                                if (job != null) {
                                    ScienceType jobScience = ScienceType.getJobScience(job);
                                    if (!collabScience.equals(jobScience)) {
                                        collabResult /= 2D;
                                    }
                                }

                                result += collabResult;
                            }
                        }
                        catch (Exception e) {
                            logger.severe("getProbability(): " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Check that there are available field samples to study.
        try {
            Unit container = person.getContainerUnit();
            if (container != null) {
                Inventory inv = container.getInventory();
                AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
                if (inv.getAmountResourceStored(rockSamples, false) < StudyFieldSamples.SAMPLE_MASS) {
                    result = 0D;
                }
            }
        }
        catch (Exception e) {
            logger.severe("getProbability(): " + e.getMessage());
        }

        // Check if person is in a moving rover.
        if (PerformLaboratoryExperiment.inMovingRover(person)) {
            result = 0D;
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(StudyFieldSamples.class);
        }

        // Modify if research is the person's favorite activity.
        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Research")) {
            result *= 2D;
        }

        // 2015-06-07 Added Preference modifier
        if (result > 0)
        	result += person.getPreference().getPreferenceScore(this);
        if (result < 0) result = 0;

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