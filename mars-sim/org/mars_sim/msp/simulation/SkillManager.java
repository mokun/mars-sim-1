/**
 * Mars Simulation Project
 * SkillManager.java
 * @version 2.71 2000-10-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.Hashtable;

/** The SkillManager class manages skills for a given person.
 *  Each person has one skill manager.
 */
public class SkillManager {

    // Data members
    private Hashtable skills; // A list of the person's skills keyed by name.

    /** Constructs a new SkillManager object */
    SkillManager() {
        skills = new Hashtable();

        // Add starting skills randomly for person.
        String[] startingSkills = {"Driving", "Greenhouse Farming", "Vehicle Mechanic"};

        for (int x = 0; x < startingSkills.length; x++) {
            int skillLevel = getInitialSkillLevel(0, 50);
            if (skillLevel > 0) {
                Skill newSkill = new Skill(startingSkills[x]);
                newSkill.setLevel(skillLevel);
                addNewSkill(newSkill);
            }
        }
    }

    /** Returns an initial skill level. 
     *  @param level lowest possible skill level
     *  @param chance the chance that the skill will be greater
     *  @return the initial skill level
     */
    private int getInitialSkillLevel(int level, int chance) {
        if (RandomUtil.lessThanRandPercent(chance))
            return getInitialSkillLevel(level + 1, chance / 2);
        else
            return level;
    }

    /** Returns the number of skills. 
     *  @return the number of skills
     */
    public int getSkillNum() {
        return skills.size();
    }

    /** Returns number of skills at skill level 1 or better. 
     *  @return the number of skills of level 1 or better
     */
    public int getDisplayableSkillNum() {
        String[] keys = getKeys();
        int count = 0;
        for (int x = 0; x < keys.length; x++)
            if (getSkillLevel(keys[x]) >= 1)
                count++;

        return count;
    }

    /** Returns an array of the skill names as strings. 
     *  @return an array of the skill names
     */
    public String[] getKeys() {
        Object[] tempArray = skills.keySet().toArray();
        String[] keyArray = new String[tempArray.length];
        for (int x = 0; x < tempArray.length; x++)
            keyArray[x] = (String) tempArray[x];

        return keyArray;
    }

    /** Returns true if the SkillManager has the named skill, false otherwise. 
     *  @param skillName the skill's name
     *  @return true if the manager has the named skill
     */
    public boolean hasSkill(String skillName) {
        if (skills.containsKey(skillName))
            return true;
        else
            return false;
    }

    /** Returns the integer skill level from a named skill if it exists in the SkillManager.
     *  Returns 0 otherwise.
     *  @param skillName the skill's name
     *  @return the skill's level
     */
    public int getSkillLevel(String skillName) {
        int result = 0;
        if (skills.containsKey(skillName))
            result = ((Skill) skills.get(skillName)).getLevel();

        return result;
    }

    /** Adds a new skill to the SkillManager and indexes it under its name. 
     *  @param newSkill the skill to be added
     */
    void addNewSkill(Skill newSkill) {
        skills.put(newSkill.getName(), newSkill);
    }

    /** Adds given experience points to a named skill if it exists in the SkillManager.
     *  If it doesn't exist, create a skill of that name in the SkillManager and add the experience points to it.
     *  @param skillName the skill's name
     *  @param experiencePoints the experience points to be added
     */
    void addExperience(String skillName, double experiencePoints) {
        if (hasSkill(skillName))
            ((Skill) skills.get(skillName)).addExperience(experiencePoints);
        else {
            addNewSkill(new Skill(skillName));
            addExperience(skillName, experiencePoints);
        }
    }
}