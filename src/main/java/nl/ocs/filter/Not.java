package nl.ocs.filter;

/**
 * A filter that negates the value of another filter
 * 
 * @author bas.rutten
 * 
 */
public final class Not extends AbstractFilter {

	private Filter filter;

	/**
	 * Constructor
	 * 
	 * @param filter
	 */
	public Not(Filter filter) {
		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		return filter.equals(((Not) obj).getFilter());
	}

	@Override
	public int hashCode() {
		return filter.hashCode();
	}

	@Override
	public boolean evaluate(Object that) {
		return !this.filter.evaluate(that);
	}

	@Override
	public String toString() {
		return super.toString() + " " + getFilter();
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

}
