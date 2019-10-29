package com.ocs.dynamo.filter;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.function.SerializablePredicate;

/**
 * A predicate that joins multiple other predicates together
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity to filter
 */
public abstract class CompositePredicate<T> implements SerializablePredicate<T> {

	private static final long serialVersionUID = 8690339909486826760L;

	private final List<SerializablePredicate<T>> operands = new ArrayList<>();

	public List<SerializablePredicate<T>> getOperands() {
		return operands;
	}
}
