package com.ocs.dynamo.filter;

import java.util.Collection;
import java.util.List;

/**
 * Interface for objects that
 * 
 * @author bas.rutten
 */
public interface Filter {

    /**
     * Evaluates an object against the filter
     * 
     * @param that
     *            the object to evaluate
     * @return
     */
    boolean evaluate(Object that);

    /**
     * Applies the filter to a collection of objects, and retunrs the objects that match the filter
     * 
     * @param collection
     * @return
     */
    <T> List<T> applyFilter(Collection<T> collection);

}
