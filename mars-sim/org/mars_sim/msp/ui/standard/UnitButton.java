/**
 * Mars Simulation Project
 * UnitToolbar.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
// The UnitButton class is a UI button for a given unit.
// It is displayed in the unit tool bar.

import java.awt.*;
import javax.swing.*;

/**
 * The UnitButton class is a UI button for a given unit.
 * It is displayed in the unit tool bar.
 */

public class UnitButton extends JButton {
	
	private UnitUIProxy unitUIProxy;  // Unit UI proxy for button.

	public UnitButton(UnitUIProxy unitUIProxy) {
		
		// Use JButton constructor
		
		super(unitUIProxy.getUnit().getName(), unitUIProxy.getButtonIcon()); 
		
		// Initialize unit
		
		this.unitUIProxy = unitUIProxy;
		
		// Prepare default unit button values
		
		setFont(new Font("Helvetica", Font.BOLD, 9));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setAlignmentX(.5F);
		setAlignmentY(.5F);
	}
	
	/** Returns the button's unit proxy */
	
	public UnitUIProxy getUnitProxy() { return unitUIProxy; }
}
