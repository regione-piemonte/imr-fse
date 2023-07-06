/*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */
package eu.o3c.o3e.dpacs.services.utils.sorting;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;

public class SortingCriteria implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private EnumSet<SortingType> unusedSortingTypes;
    private LinkedList<SortingType> usedSortingTypes;
    private EnumMap<SortingType, SortingDirection> sortingDirections;

    public SortingCriteria() {
        unusedSortingTypes = EnumSet.allOf(SortingType.class);
        usedSortingTypes = new LinkedList<SortingType>();
        sortingDirections = new EnumMap<SortingType, SortingDirection>(SortingType.class);
        for (SortingType st : unusedSortingTypes) {
            setSortingDirection(st, SortingDirection.DESCENDING);
        }
//        useCriteria(SortingType.Z_AXIS);
//        setSortingDirection(SortingType.Z_AXIS, SortingDirection.DESCENDING);
    }

    public void useCriteria(SortingType criteria) {
        if (!usedSortingTypes.contains(criteria)) {
            usedSortingTypes.addLast(criteria);
            unusedSortingTypes.remove(criteria);
        }
    }

    public void doNotUseCriteria(SortingType criteria) {
        if (usedSortingTypes.contains(criteria)) {
            unusedSortingTypes.add(criteria);
            usedSortingTypes.remove(criteria);
        }
    }

    public void useNone() {
        unusedSortingTypes = EnumSet.allOf(SortingType.class);
        usedSortingTypes = new LinkedList<SortingType>();
    }

    public SortingDirection getSortingDirection(SortingType type) {
        return sortingDirections.get(type);
    }

    public final void setSortingDirection(SortingType type, SortingDirection direction) {
        sortingDirections.put(type, direction);
    }

    public void increasePriority(SortingType type) {
        if (usedSortingTypes.contains(type)) {
            int index = usedSortingTypes.indexOf(type);
            if (index >= 1) {
                SortingType previous = usedSortingTypes.get(index - 1);
                usedSortingTypes.set(index - 1, type);
                usedSortingTypes.set(index, previous);
            }
        }
    }

    public void decreasePriority(SortingType type) {
        if (usedSortingTypes.contains(type)) {
            int index = usedSortingTypes.indexOf(type);
            if (index + 1 < usedSortingTypes.size()) {
                SortingType next = usedSortingTypes.get(index + 1);
                usedSortingTypes.set(index + 1, type);
                usedSortingTypes.set(index, next);
            }
        }
    }

    public LinkedList<SortingType> getUsedSortingTypes() {
        return usedSortingTypes;
    }

    public LinkedList<SortingType> getUnusedSortingTypes() {
        return new LinkedList<SortingType>(unusedSortingTypes);
    }

    public EnumMap<SortingType, SortingDirection> getSortingDirections() {
        return sortingDirections;
    }

    public void setSortingDirections(EnumMap<SortingType, SortingDirection> sortingDirections) {
        this.sortingDirections = sortingDirections;
    }

    public void setUnusedSortingTypes(EnumSet<SortingType> unusedSortingTypes) {
        this.unusedSortingTypes = unusedSortingTypes;
    }

    public static void main(String[] args) {
        SortingCriteria sortingCriteria = new SortingCriteria();
        System.out.println(sortingCriteria.getSortingDirection(SortingType.X_AXIS));
    }
}
