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
package com.ocs.dynamo.ui.utils;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Copy of AbstractList.SubList without concurrency check. Use to delegate to a
 * subset of another list.
 */
public class SubList<E> extends AbstractList<E> {

	protected class ListIteratorImpl implements ListIterator<E> {

		private ListIterator<E> i;

		/**
		 * Constructor
		 * 
		 * @param i
		 */
		protected ListIteratorImpl(ListIterator<E> i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return nextIndex() < size;
		}

		@Override
		public E next() {
			if (hasNext()) {
				return i.next();
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public boolean hasPrevious() {
			return previousIndex() >= 0;
		}

		@Override
		public E previous() {
			if (hasPrevious()) {
				return i.previous();
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex() {
			return i.nextIndex() - offset;
		}

		@Override
		public int previousIndex() {
			return i.previousIndex() - offset;
		}

		@Override
		public void remove() {
			i.remove();
			size--;
		}

		@Override
		public void set(E e) {
			i.set(e);
		}

		@Override
		public void add(E e) {
			i.add(e);
			size++;
		}
	}

	private final List<E> list;

	private final int offset;

	private int size;

	public SubList(List<E> list, int fromIndex, int toIndex) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		}
		if (toIndex > list.size()) {
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		}
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex
					+ ")");
		}
		this.list = list;
		offset = fromIndex;
		size = toIndex - fromIndex;
	}

	@Override
	public E set(int index, E element) {
		rangeCheck(index);
		return list.set(index + offset, element);
	}

	@Override
	public E get(int index) {
		rangeCheck(index);
		return list.get(index + offset);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void add(int index, E element) {
		rangeCheckForAdd(index);
		list.add(index + offset, element);
		size++;
	}

	@Override
	public E remove(int index) {
		rangeCheck(index);
		E result = list.remove(index + offset);
		size--;
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return addAll(size, c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		rangeCheckForAdd(index);
		int cSize = c.size();
		if (cSize == 0) {
			return false;
		}

		list.addAll(offset + index, c);
		size += cSize;
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		rangeCheckForAdd(index);
		return new ListIteratorImpl(list.listIterator(index + offset));
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList<>(this, fromIndex, toIndex);
	}

	protected void rangeCheck(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}
	}

	protected void rangeCheckForAdd(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
		}
	}

	protected String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size;
	}

	public List<E> getList() {
		return list;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getOffset() {
		return offset;
	}

}
