/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 2.73 2001-11-25
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.task.*;

/**
 * The PersonTableModel that maintns a list of Person objects.
 * It maps key attributes of the Person into Columns.
 */
public class PersonTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;           // Person name column
    private final static int  LOCATION = 1;      // Situation column
    private final static int  COORDS = 2;       // Location column
    private final static int  HUNGER = 3;         // Hunger column
    private final static int  FATIGUE = 4;        // Fatigue column
    private final static int  TASK = 5;           // Task column
    private final static int  MISSION = 6;        // Mission column
    private final static int  COLUMNCOUNT = 7;    // The number of Columns

    // Data members
    private String columnNames[]; // Names of Columns
    private UIProxyManager proxyManager;

    /** Constructs a PersonTableModel object
     */
    public PersonTableModel(UIProxyManager proxyManager) {
        super("Person");

        columnNames = new String[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnNames[HUNGER] = "Hunger";
        columnNames[FATIGUE] = "Fatigue";
        columnNames[COORDS] = "Coordinates";
        columnNames[LOCATION] = "Location";
        columnNames[MISSION] = "Mission";
        columnNames[TASK] = "Task";

        this.proxyManager = proxyManager;
    }

    /**
     * Find all the Person units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedPersonProxies());
    }

    /**
     * Return the number of columns
     * @return column count.
     */
    public int getColumnCount() {
        return COLUMNCOUNT;
    }

    /**
     * Return the name of the column requested.
     * @param columnIndex Index of column.
     * @return name of specified column.
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < COLUMNCOUNT)) {
            return columnNames[columnIndex];
        }
        return "Unknown";
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Person person = (Person)getUnit(rowIndex).getUnit();

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = person.getName();
            } break;

            case HUNGER : {
                result = new Integer(new Float(person.getHunger()).intValue());
            } break;

            case FATIGUE : {
                result = new Integer(new Float(person.getFatigue()).intValue());;
            } break;

            case COORDS : {
                result = person.getCoordinates().getFormattedString();
            } break;

            // Create a composite sdtring containing Vehicle & Settlement
            case LOCATION : {
                Settlement house = person.getSettlement();
                if (house != null) {
                    result = house.getName();
                }
                else {
                    Vehicle vech = person.getVehicle();
                    if (vech != null) {
                        result = vech.getName();
                    }
                }
            } break;

            case TASK : {
                result =
                        person.getMind().getTaskManager().getTaskDescription();
            } break;

            case MISSION : {
                Mission mission = person.getMind().getMission();
                if (mission != null) {
                    StringBuffer cellValue = new StringBuffer();
                    cellValue.append(mission.getName());
                    cellValue.append(" - ");
                    cellValue.append(mission.getPhase());
                    result = cellValue.toString();
                }
            } break;
        }

        return result;
    }
}