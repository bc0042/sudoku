package com.tt.sudoku;

import java.util.*;

/**
 * Created by BC on 2/6/18.
 */
public class ListMap {
    private static Map<Integer, Set<StrongLink>> map = new HashMap<>();
    private int size = 0;

    public void add(int num, Set<StrongLink> c) {
        size += c.size();
        Set<StrongLink> set2 = map.get(num);
        if (set2 == null) {
            map.put(num, c);
        } else {
            set2.addAll(c);
        }
    }

    public Set<Map.Entry<Integer, Set<StrongLink>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public int size() {
        return size;
    }

    public Set<StrongLink> get(int num) {
        // new hashset to avoid ConcurrentModificationException
        return map.get(num);
    }

    public Set<StrongLink> getOverlap(LinkNode node) {
        Set<StrongLink> set = new HashSet<>();
        if (!node.isSingle()) return set;

        for (Map.Entry<Integer, Set<StrongLink>> entry : entrySet()) {
            for (StrongLink link : entry.getValue()) {
                if (link.node1.singleOverlap(node) || link.node2.singleOverlap(node)) {
                    set.add(link);
                }
            }
        }
        return set;
    }

    public void clear() {
        size = 0;
        map.clear();
    }
}
