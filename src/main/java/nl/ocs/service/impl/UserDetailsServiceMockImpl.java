package nl.ocs.service.impl;

import nl.ocs.service.UserDetailsService;

/**
 * Mock implementation of the user details service. Can be used when you don't
 * actually have a currently logged in user, e.g. in integration tests
 * 
 * @author bas.rutten
 * 
 */
public class UserDetailsServiceMockImpl implements UserDetailsService {

	@Override
	public String getCurrentUserName() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public boolean isUserInRole(String... roles) {
		return false;
	}

}
