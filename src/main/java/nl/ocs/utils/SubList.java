package nl.ocs.utils;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Copy of AbstractList.SubList without concurrency check. Use to delegate to a
 * subset of another list.
 *
 */
public class SubList<E> extends AbstractList<E> {

	protected class ListIteratorImpl implements ListIterator<E> {

		private ListIterator<E> i;

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

	protected final List<E> l;

	protected final int offset;

	protected int size;

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
		l = list;
		offset = fromIndex;
		size = toIndex - fromIndex;
	}

	@Override
	public E set(int index, E element) {
		rangeCheck(index);
		return l.set(index + offset, element);
	}

	@Override
	public E get(int index) {
		rangeCheck(index);
		return l.get(index + offset);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void add(int index, E element) {
		rangeCheckForAdd(index);
		l.add(index + offset, element);
		size++;
	}

	@Override
	public E remove(int index) {
		rangeCheck(index);
		E result = l.remove(index + offset);
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

		l.addAll(offset + index, c);
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
		return new ListIteratorImpl(l.listIterator(index + offset));
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
}
