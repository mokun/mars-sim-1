/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 3.02 2011-11-26
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The UnitTableModel that maintains a table model of Units objects.
 * It is only a partial implementation of the TableModel interface.
 */
abstract public class UnitTableModel extends AbstractTableModel
            implements MonitorModel, UnitListener {

    // Data members
    private Collection<Unit> units;   // Collection of units
    private String name;            // Model name
    private String statusSuffix;    // Suffix to added to status message
    private String columnNames[];   // Names of the displayed columns
    private Class  columnTypes[];   // Types of the individual columns
    private int size = -1;
    private boolean refreshSize = true;

    /**
     * Constructs a UnitTableModel object.
     *
     *  @param name Name of the model.
     *  @param suffix A string to add to the status message.
     *  @param names Names of the columns displayed.
     *  @param types The Classes of the individual columns.
     */
    protected UnitTableModel(String name, String suffix,
                             String names[], Class types[]) {

        // Initialize data members
        this.name = name;
        this.statusSuffix = suffix;
        this.units = new ConcurrentLinkedQueue<Unit>();
        getRowCount();
        this.columnNames = names;
        this.columnTypes = types;
    }

    /**
     * Add a unit to the model.
     * @param newUnit Unit to add to the model.
     */
    protected void addUnit(Unit newUnit) {
        if (!units.contains(newUnit)) {
            units.add(newUnit);
            refreshSize = true;
            newUnit.addUnitListener(this);
            
            // Inform listeners of new row
            SwingUtilities.invokeLater(new Runnable() {
            	public void run() {
            		fireTableRowsInserted(getUnitNumber() - 1, getUnitNumber() - 1);
            	}
            });
        }
    }

    /**
     * Remove a unit from the model.
     * @param oldUnit Unit to remove from the model.
     */
    protected void removeUnit(Unit oldUnit) {
        if (units.contains(oldUnit)) {
            int index = getIndex(oldUnit);
            
            units.remove(oldUnit);
            refreshSize = true;
            oldUnit.removeUnitListener(this);

            // Inform listeners of new row
            SwingUtilities.invokeLater(new RemoveUnitTableUpdater(index));
        }
    }
    
    /**
     * Gets the index value of a given unit.
     * @param unit the unit
     * @return the index value.
     */
    private int getIndex(Unit unit) {
        final Iterator<Unit> it = units.iterator();
        int idx = -1;
        Unit u;
        while(it.hasNext()){
            idx++;
            u = it.next();
            if(u.equals(unit)){
                return idx;
            }
        }
        throw new IllegalStateException("Could not find index for unit " + unit);
//    	Object[] array = units.toArray();
//    	int size = array.length;
//    	int result = 0;
//
//    	for(int i = 0; i < size; i++) {
//    		Unit temp = (Unit) array[i];
//
//    		if(temp.equals(unit)) {
//    			result = i;
//    			break;
//    		}
//    	}
//
//    	return result;
    }
    
    /**
     * Adds a collection of units to the model.
     * @param newUnits the units to add.
     */
    protected void addAll(Collection<Unit> newUnits) {
    	Iterator<Unit> i = newUnits.iterator();
    	while (i.hasNext()) addUnit(i.next());
    }
    
    /**
     * Clears out units from the model.
     */
    protected void clear() {
    	Iterator<Unit> i = units.iterator();
    	while (i.hasNext()) i.next().removeUnitListener(this);
    	units.clear();
        refreshSize = true;
    	fireTableDataChanged();
    }
    
    /**
     * Checks if unit is in table model already.
     * @param unit the unit to check.
     * @return true if unit is in table.
     */
    protected boolean containsUnit(Unit unit) {
    	return units.contains(unit);
    }
    
    /**
     * Gets the number of units in the model.
     * @return number of units.
     */
    protected int getUnitNumber() {
        if(refreshSize){
            this.size = units == null ? 0 : units.size();
            refreshSize = false;
        }
//        if (units != null) return units.size();
//    	else return 0;
        return this.size;
    }

    /**
     * Return the number of columns
     * @return column count.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Return the type of the column requested.
     * @param columnIndex Index of column.
     * @return Class of specified column.
     */
    public Class<?> getColumnClass(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
            return columnTypes[columnIndex];
        }
        return Object.class;
    }

    /**
     * Return the name of the column requested.
     * @param columnIndex Index of column.
     * @return name of specified column.
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
            return columnNames[columnIndex];
        }
        return "Unknown";
    }


    /**
     * Get the name of the model.
     * @return model name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of rows in the model.
     * @return the number of Units.
     */
    public int getRowCount() {

        return getUnitNumber();
    }

    /**
     * Is this model already ordered according to some external criteria.
     * @return FALSE as the Units have no natural order.
     */
    public boolean getOrdered() {
        return false;
    }

    /**
     * Get the unit at the specified row.
     * @param index Index of the row.
     * @return Unit matching row
     */
    protected Unit getUnit(int index) {
        if(index > (getRowCount()-1)) throw new IllegalStateException("Invalid index " + index + " for " + getRowCount() + " rows");
        int idx = -1;
        Iterator<Unit> it = units.iterator();
        while(it.hasNext()){
            idx++;
            if(idx == index){
                return it.next();
            }
            it.next();
        }
        throw new IllegalStateException("Could not find an index " + index);
//	Object [] array = units.toArray();
//        return (Unit)array[index];
    }
    
    /**
     * Gets the index of the row a given unit is at.
     * @param unit the unit to find.
     * @return the row index or -1 if not in table model.
     */
    protected int getUnitIndex(Unit unit) {
    	if ((units != null) && units.contains(unit)) 
    	    return getIndex(unit);
    	else 
    	    return -1;
    }

    /**
     * Get the unit at the specified row.
     * @param row Indexes of Unit to retrieve.
     * @return Unit at specified position.
     */
    public Object getObject(int row) {
        return getUnit(row);
//    	Object array[] = units.toArray();
//        return array[row];
    }
    
    /**
     * Gets the model count string.
     */
    public String getCountString() {
    	return getUnitNumber() + statusSuffix;
    }
    
    /**
     * Prepares the model for deletion.
     */
    public void destroy() {
    	clear();
    	units = null;
    }
    
    @Override
    public boolean equals(Object o) {
    	boolean result = true;
    	
    	if (o instanceof UnitTableModel) {
    		UnitTableModel oModel = (UnitTableModel) o;
    		
    		if (!units.equals(oModel.units)) result = false;
    		
    		if (!name.equals(oModel.name)) result = false;
    		
    		if (!statusSuffix.equals(oModel.statusSuffix)) result = false;
    	}
    	else result = false;
    	
    	return result;
    }
    
    /**
     * Inner class for updating table after removing units.
     */
    private class RemoveUnitTableUpdater implements Runnable {
    	
    	private int index;
    	
    	private RemoveUnitTableUpdater(int index) {
    		this.index = index;
    	}
    	
    	public void run() {
    		fireTableRowsDeleted(index, index);
    	}
    }
}