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
package com.ocs.dynamo.ui.composite.form;

/**
 * TODO: is this still needed after we phased out the old form of validation?
 * Marker interface for a parent component that receives a signal
 * 
 * @author bas.rutten
 */
public interface ReceivesSignal {

	/**
	 * Receive a singal from a detail component whether all nested components are
	 * valid
	 * 
	 * @param component
	 * @param valid
	 */
	void signalDetailsComponentValid(SignalsParent component, boolean valid);
}
