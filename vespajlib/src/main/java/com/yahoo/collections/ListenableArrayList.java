// Copyright 2016 Yahoo Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An array list which notifies listeners after one or more items are added
 *
 * @author bratseth
 * @since 5.1.17
 */
@SuppressWarnings("serial")
public class ListenableArrayList<ITEM> extends ArrayList<ITEM> {

    private List<Runnable> listeners = null;

    public ListenableArrayList() {}

    public ListenableArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean add(ITEM e) {
        boolean result = super.add(e);
        notifyListeners();
        return result;
    }

    @Override
    public void add(int index, ITEM e) {
        super.add(index, e);
        notifyListeners();
    }

    @Override
    public boolean addAll(Collection<? extends ITEM> a) {
        boolean result = super.addAll(a);
        notifyListeners();
        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends ITEM> a) {
        boolean result = super.addAll(index, a);
        notifyListeners();
        return result;
    }

    @Override
    public ITEM set(int index, ITEM e) {
        ITEM result = super.set(index, e);
        notifyListeners();
        return result;
    }

    /**
     * Adds a listener which is invoked whenever labels are added to this.
     * This may not be invoked once for each added element.
     */
    public void addListener(Runnable listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(listener);
    }

    private void notifyListeners() {
        if (listeners == null) return;
        for (Runnable listener : listeners)
            listener.run();
    }

}
