package org.apache.clerezza.rdf.core.access;

import java.util.Comparator;

/**
 * Compares the WeightedTcManagementProviders, descending for weight and
 * ascending by name
 */
public class WeightedProviderComparator implements Comparator<WeightedTcProvider> {

	@Override
	public int compare(WeightedTcProvider o1, WeightedTcProvider o2) {
		int o1Weight = o1.getWeight();
		int o2Weight = o2.getWeight();
		if (o1Weight != o2Weight) {
			return o2Weight - o1Weight;
		}
		return o1.getClass().toString().compareTo(o2.getClass().toString());
	}
}
