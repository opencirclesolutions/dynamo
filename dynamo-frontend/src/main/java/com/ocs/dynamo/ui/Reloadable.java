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
 * Marker interface to indicate that a component can be reloaded. Reloading the
 * component will cause the data grid to be refreshed to make sure it is up to
 * date with the database
 * 
 * @author bas.rutten
 */
@FunctionalInterface
public interface Reloadable {

    /**
     * Reloads
     */
    void reload();
}
