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
package com.ocs.dynamo.ui;

/**
 * Interface for components that have to be built after class instantiation by calling the build
 * method. This is to support bean like creation of components without the need to build them
 * directly from the constructor.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
@FunctionalInterface
public interface Buildable {

    /**
     * Call this method when the UI component has to be created. Preferable this method is only
     * called once in a user vaadin session, e.g. data may be refreshed and UI components may be
     * enabled/disabled or displayed/hidden during a session opposed to building and instantiating
     * all UI components for every interaction.
     */
    void build();
}
