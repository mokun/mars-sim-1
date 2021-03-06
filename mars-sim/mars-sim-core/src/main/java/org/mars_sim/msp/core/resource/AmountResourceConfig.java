/**
 * Mars Simulation Project
 * AmountResourceConfig.java
 * @version 3.07 2015-02-24
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Provides configuration information about amount resources. Uses a DOM document to get the information.
 */
public class AmountResourceConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 123L;

	// Element names
	private static final String RESOURCE = "resource";
	private static final String NAME = "name";
	private static final String PHASE = "phase";
	private static final String LIFE_SUPPORT = "life-support";

	// 2014-11-25 Added edible
	private static final String EDIBLE = "edible";

	// Data members.
	private Set<AmountResource> resources = new TreeSet<AmountResource>();

	/**
	 * Constructor
	 * @param amountResourceDoc the amount resource XML document.
	 * @throws Exception if error reading XML document
	 */
	public AmountResourceConfig(Document amountResourceDoc) {
		loadAmountResources(amountResourceDoc);
	}

	/**
	 * Loads amount resources from the resources.xml config document.
	 * @param amountResourceDoc the configuration XML document.
	 * @throws Exception if error loading amount resources.
	 */
	@SuppressWarnings("unchecked")
	private void loadAmountResources(Document amountResourceDoc) {
		Element root = amountResourceDoc.getRootElement();
		List<Element> resourceNodes = root.getChildren(RESOURCE);
		for (Element resourceElement : resourceNodes) {
			String name = "";

			// Get name.
			// 2015-02-24 Added toLowerCase() just in case
			name = resourceElement.getAttributeValue(NAME).toLowerCase();
			String description = resourceElement.getText();

			// Get phase.
			String phaseString = resourceElement.getAttributeValue(PHASE).toUpperCase();
			Phase phase = Phase.valueOf(phaseString);

			// Get life support
			Boolean lifeSupport = Boolean.parseBoolean(resourceElement.getAttributeValue(LIFE_SUPPORT));

			// 2014-11-25 Added edible
			Boolean edible = Boolean.parseBoolean(resourceElement.getAttributeValue(EDIBLE));

			// Create new amount resource.
			// 2014-11-25 Added edible
			AmountResource resource = new AmountResource(name, description, phase, lifeSupport, edible);
			resources.add(resource);
		}
	}

	/**
	 * Gets a set of all amount resources.
	 * @return set of resources.
	 */
	public Set<AmountResource> getAmountResources() {
		return resources;
	}

	/**
	 * an alphabetically ordered map of all resources.
	 * @return {@link Map}<{@link String},{@link AmountResource}>
	 */
	public Map<String,AmountResource> getAmountResourcesMap() {
		Map<String,AmountResource> map = new TreeMap<String,AmountResource>();
		for (AmountResource resource : resources) {
			map.put(resource.getName(),resource);
		}
		return map;

	}
}