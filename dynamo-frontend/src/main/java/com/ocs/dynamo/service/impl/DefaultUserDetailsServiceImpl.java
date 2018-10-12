/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ocs.dynamo.service.UserDetailsService;

/**
 * User details service - used for retrieving the user's current roles and name
 * 
 * @author bas.rutten
 *
 */
public class DefaultUserDetailsServiceImpl implements UserDetailsService {

	private static final String SYSTEM = "system";

	@Override
	public String getCurrentUserName() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
					.getRequest();
			return request.getUserPrincipal().getName();
		} catch (Exception ex) {
			// ignore - no request available during integration test
			return SYSTEM;
		}
	}

	@Override
	public boolean isUserInRole(String role) {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
					.getRequest();

			return request.isUserInRole(role);
		} catch (Exception ex) {
			return false;
		}
	}

	@Override
	public boolean isUserInRole(String... roles) {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
					.getRequest();
			for (String r : roles) {
				if (request.isUserInRole(r)) {
					return true;
				}
			}
			return false;
		} catch (Exception ex) {
			return false;
		}
	}
}
