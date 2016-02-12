package nl.ocs.ui.grid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyValueGenerator;

/**
 * A property value generator for computing the average of a range of
 * BigDecimals
 * 
 * @author bas.rutten
 *
 */
public class BigDecimalAverageGenerator extends PropertyValueGenerator<BigDecimal> {

	private static final long serialVersionUID = -4000091542049888468L;

	private String propertyNameFilter;

	public BigDecimalAverageGenerator(String propertyNameFilter) {
		this.propertyNameFilter = propertyNameFilter;
	}

	@Override
	public BigDecimal getValue(Item item, Object itemId, Object propertyId) {
		BigDecimal sum = BigDecimal.ZERO;
		int count = 0;

		Collection<?> cols = item.getItemPropertyIds();
		for (Object o : cols) {
			if (o.toString().contains("_" + propertyNameFilter)) {
				Property<?> prop = item.getItemProperty(o);
				if (prop.getValue() != null && prop.getValue() instanceof BigDecimal) {
					BigDecimal v = (BigDecimal) prop.getValue();
					sum = sum.add(v);
					count++;
				}
			}
		}
		return count == 0 ? null : sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
	}

	@Override
	public Class<BigDecimal> getType() {
		return BigDecimal.class;
	}

}
