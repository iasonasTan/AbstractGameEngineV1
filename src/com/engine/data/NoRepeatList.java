package com.engine.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is a modification of {@link ArrayList} which rejects consecutive duplicates.
 * @param <T> type of items.
 */
public class NoRepeatList<T> extends ArrayList<T> {
    /**
     * Appends the specified element to the end of this list.
     *
     * @param t element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(T t) {
        if(isEmpty())
            return super.add(t);
        else if (!t.equals(getLast()))
            return super.add(t);
        else
            return false;
    }

    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public void add(int index, T element) {
        if(isEmpty()) {
            super.add(index, element);
        }
        if(!get(index-1).equals(element)) {
            if(index==size()-1)
                super.add(index, element);
            else if (!get(index+1).equals(element))
                super.add(index, element);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param element
     * @since 21
     */
    @Override
    public void addFirst(T element) {
        if(isEmpty())
            super.add(element);
        if(!getFirst().equals(element))
            super.addFirst(element);
    }

    /**
     * {@inheritDoc}
     *
     * @param element
     * @since 21
     */
    @Override
    public void addLast(T element) {
        if(isEmpty())
            super.add(element);
        if(!getLast().equals(element))
            super.addLast(element);
    }
}
