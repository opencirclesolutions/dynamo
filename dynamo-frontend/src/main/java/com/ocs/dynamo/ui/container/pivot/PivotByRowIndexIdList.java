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
package com.ocs.dynamo.ui.container.pivot;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Patrick Deenen
 */
public class PivotByRowIndexIdList extends AbstractList<Integer> implements Serializable {

	/**
	 * Default Serial Version ID.
	 */
	private static final long serialVersionUID = -4923685883134180928L;

	private PivotByRowIndexContainer container;

	/**
	 * Constructs a Id list by delegating to a given container.
	 * 
	 * @param container
	 *            Mandatory reference to pivot container
	 */
	public PivotByRowIndexIdList(PivotByRowIndexContainer container) {
		if (container == null) {
			throw new AssertionError("Container is mandatory");
		}
		this.container = container;
	}

	@Override
	public Integer get(int index) {
		if (index < 0 || index >= container.size()) {
			throw new IndexOutOfBoundsException();
		}
		return index;
	}

	@Override
	public int indexOf(Object id) {
		if (id == null) {
			throw new AssertionError("Id is mandatory");
		}
		if (id instanceof Number) {
			return ((Number) id).intValue();
		}
		return -1;
	}

	@Override
	public int size() {
		return this.container.size();
	}

	@Override
	public List<Integer> subList(int fromIndex, int toIndex) {
		List<Integer> sl = new ArrayList<>();
		for (int i = fromIndex; i < toIndex; i++) {
			sl.add(i);
		}
		return sl;
	}

}
