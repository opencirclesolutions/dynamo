package com.ocs.dynamo.service;

/**
 * Service for retrieving the details of the logged in user
 * 
 * @author bas.rutten
 */
public interface UserDetailsService {

    /**
     * Retrieve the name of the currently logged in user
     */
    String getCurrentUserName();

    /**
     * Indicates whether the currently logged in user has a certain role
     * 
     * @param role
     * @return
     */
    boolean isUserInRole(String role);

    /**
     * Returns true when the user is in (at least one of) the provided roles
     * 
     * @param roles
     * @return
     */
    boolean isUserInRole(String... roles);

}
