/**
 * Mars Simulation Project
 * LivingQuartersPanel.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.ui.standard.monitor.PersonTableModel;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The LivingQuartersPanel class displays information about a settlement's
 * living conditions in the user interface.
 */
public class LivingQuartersPanel extends FacilityPanel implements MouseListener {

    // Data members
    private Settlement settlement;                  // The settlement
    private JLabel currentPopulationLabel;          // A label that displays the current population.
    private JList inhabitantList;                   // A list to display the names of the current inhabitants.

    // Update data cache
    private Person[] inhabitants;                     // An array of the current inhabitants.
    private int currentPopulation;                  // The current population number.

    /** Constructs a LivingQuartersPanel object
     *  @param settlement the settlement
     *  @param desktop the desktop pane
     */
    public LivingQuartersPanel(Settlement settlement, MainDesktopPane desktop) {

        // Use FacilityPanel's constructor
        super(desktop);

	// Initialize data members
        this.settlement = settlement;
	tabName = "Quarters";
	currentPopulation = settlement.getCurrentPopulationNum();

	// Set up components
	setLayout(new BorderLayout());

	// Prepare content pane
	JPanel contentPane = new JPanel(new BorderLayout(0, 5));
	add(contentPane, "North");

	// Prepare name label
	JLabel nameLabel = new JLabel("Living Quarters", JLabel.CENTER);
	contentPane.add(nameLabel, "North");

	// Prepare info pane
	JPanel infoPane = new JPanel(new BorderLayout(0, 5));
	contentPane.add(infoPane, "Center");

	// Prepare label pane
	JPanel labelPane = new JPanel(new GridLayout(2, 1));
	labelPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	infoPane.add(labelPane, "North");

	// Prepare population capacity label
	JLabel populationCapacityLabel = new JLabel("Population Capacity: " + settlement.getPopulationCapacity(), JLabel.CENTER);
	labelPane.add(populationCapacityLabel);

	// Prepare current population label
	currentPopulationLabel = new JLabel("Current Population: " + currentPopulation, JLabel.CENTER);
	labelPane.add(currentPopulationLabel);

	// Prepare inhabitant panel
	JPanel inhabitantPane = new JPanel();
	inhabitantPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	infoPane.add(inhabitantPane, "Center");

	// Prepare inner inhabitant panel
	JPanel innerInhabitantPane = new JPanel(new BorderLayout());
	innerInhabitantPane.setPreferredSize(new Dimension(175, 100));
	inhabitantPane.add(innerInhabitantPane);

	// Prepare inhabitant list
	inhabitants = settlement.getInhabitantArray();
	DefaultListModel inhabitantListModel = new DefaultListModel();
	PersonIterator people = settlement.getInhabitants().iterator();
	while (people.hasNext()) inhabitantListModel.addElement(people.next().getName());
	inhabitantList = new JList(inhabitantListModel);
	inhabitantList.setVisibleRowCount(6);
	inhabitantList.addMouseListener(this);
	innerInhabitantPane.add(new JScrollPane(inhabitantList), "Center");

        // Add monitor button
        JButton monitorButton = new JButton(new ImageIcon("images/Monitor.gif"));
        monitorButton.setMargin(new Insets(1, 1, 1, 1));
        monitorButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        monitorPopulation();

                    }
                });
        JPanel monitorPanel = new JPanel();
        monitorPanel.add(monitorButton);
        innerInhabitantPane.add(monitorPanel, "East");
    }

    private void monitorPopulation() {
        desktop.addModel(new PersonTableModel(settlement));
    }

    /** Updates the facility panel's information */
    public void updateInfo() {

	// Update current population
	int population = settlement.getCurrentPopulationNum();
	if (currentPopulation != population) {
            currentPopulation = population;
            currentPopulationLabel.setText("Current Population: " + currentPopulation);
	}

	// Update inhabitant list
	DefaultListModel model = (DefaultListModel) inhabitantList.getModel();
	inhabitants = settlement.getInhabitantArray();

	boolean match = false;

	// Check if inhabitant list matches settlement's population
	if (model.getSize() == population) {
            match = true;
	    for (int x=0; x < population; x++)
		if (!((String) model.getElementAt(x)).equals(inhabitants[x].getName())) match = false;
	}

	// If no match, update inhabitant list
	if (!match) {
 	    model.removeAllElements();
	    for (int x=0; x < population; x++) model.addElement(inhabitants[x].getName());
	    validate();
	}
    }

    // Implement MouseListener Methods
    public void mouseClicked(MouseEvent event) {

	if (event.getClickCount() >= 2) {
  	    int index = inhabitantList.locationToIndex(event.getPoint());
  	    if (index > -1) {
  	   	try {
                    UnitUIProxy proxy = desktop.getProxyManager().getUnitUIProxy(inhabitants[index]);
                    desktop.openUnitWindow(proxy);
                }
 		catch(NullPointerException e) {}
    	    }
	}
    }

    public void mousePressed(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
}