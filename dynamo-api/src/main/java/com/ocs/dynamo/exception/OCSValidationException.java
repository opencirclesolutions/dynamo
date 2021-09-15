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
package com.ocs.dynamo.exception;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * An exception indicating that one or more validation errors have occurred
 * 
 * @author bas.rutten
 */
public class OCSValidationException extends OCSRuntimeException {

	private static final long serialVersionUID = 8242893962990806889L;

	private final List<String> errors;

	public OCSValidationException(String error) {
		this.errors = Lists.newArrayList(error);
	}

	public OCSValidationException(List<String> errors) {
		this.errors = errors;
	}

	public List<String> getErrors() {
		return errors;
	}

	@Override
	public String getMessage() {
		return errors != null && !errors.isEmpty() ? errors.get(0) : super.getMessage();
	}

}
