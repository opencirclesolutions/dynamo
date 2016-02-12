package nl.ocs.domain.comparator;

import java.util.Comparator;

import nl.ocs.domain.AbstractEntity;

/**
 * A comparator for comparing entities based on their IDs
 * 
 * @author bas.rutten
 * 
 */
public class IdComparator implements Comparator<AbstractEntity<Integer>> {

	@Override
	public int compare(AbstractEntity<Integer> o1, AbstractEntity<Integer> o2) {
		if (o1.getId() == null) {
			return -1;
		}
		if (o2.getId() == null) {
			return 1;
		}
		return o1.getId().compareTo(o2.getId());
	}

}
