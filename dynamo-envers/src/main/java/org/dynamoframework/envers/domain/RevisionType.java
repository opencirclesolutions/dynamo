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
//package org.dynamoframework.envers.domain;
//
///**
// * Revision types for auditing
// *
// * @author bas.rutten
// */
//public enum RevisionType {
//
//	ADD, MOD, DEL;
//
//	/**
//	 * Translates the Envers revision type to the Dynamo revision type
//	 *
//	 * @param type the revision type to translate
//	 * @return the resulting revision type
//	 */
//	public static RevisionType fromInternal(org.hibernate.envers.RevisionType type) {
//		return switch (type) {
//			case ADD -> ADD;
//			case MOD -> MOD;
//			case DEL -> DEL;
//			default -> null;
//		};
//	}
//}
