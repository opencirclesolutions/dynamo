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
package com.ocs.dynamo.domain.converter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * For converting a ZonedDatetime to a java.sql.Timestamp
 * 
 * @author bas.rutten
 *
 */
@Converter(autoApply = true)
public class ZonedDatetimeAttributeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(ZonedDateTime zdt) {
		if (zdt == null) {
			return null;
		}
		return Timestamp.from(zdt.toInstant());
	}

	@Override
	public ZonedDateTime convertToEntityAttribute(Timestamp dbData) {
		return ZonedDateTime.from(dbData.toInstant());
	}

}
