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

import com.ocs.dynamo.domain.AbstractEntity;

/**
 * A subject (also known as an Observable) according to the well-known pattern
 * 
 * @author bas.rutten
 * @param <T>
 *            the type of the entity being observed
 */
public interface Subject<T extends AbstractEntity<?>> {

    /**
     * Registers an observer
     * 
     * @param observer
     *            the observer to register
     */
    void register(Observer<T> observer);

    /**
     * Unregisters an observer
     * 
     * @param observer
     *            the observer to unregister
     */
    void unregister(Observer<T> observer);

    /**
     * Unregisters all observers
     */
    void unregisterAll();

    /**
     * Notifies all observers
     * 
     * @param entity
     *            the entity that has changed
     */
    void notifyObservers(T entity);
}
