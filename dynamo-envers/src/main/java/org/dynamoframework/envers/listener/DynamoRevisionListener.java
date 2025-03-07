///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package org.dynamoframework.envers.listener;
//
//import org.dynamoframework.ApplicationContextProvider;
//import org.hibernate.envers.RevisionListener;
//
//import org.dynamoframework.envers.domain.DynamoRevisionEntity;
//import org.dynamoframework.service.UserDetailsService;
//import org.dynamoframework.service.impl.ApplicationContextProvider;
//
///**
// * Custom Hibernate envers listener
// *
// * @author bas.rutten
// */
//public class DynamoRevisionListener implements RevisionListener {
//
//	private static final String UNKNOWN = "unknown";
//
//	@Override
//	public void newRevision(final Object o) {
//		final DynamoRevisionEntity entity = (DynamoRevisionEntity) o;
//		UserDetailsService uds = ApplicationContextProvider.getApplicationContext().getBean(UserDetailsService.class);
//		if (uds != null) {
//			entity.setUsername(uds.getCurrentUserName());
//		} else {
//			entity.setUsername(UNKNOWN);
//		}
//	}
//}
