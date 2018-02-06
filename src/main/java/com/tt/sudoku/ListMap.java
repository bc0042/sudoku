package com.tt.sudoku;

import com.sun.org.apache.xerces.internal.xs.StringList;

import java.util.*;

/**
 * Created by BC on 2/6/18.
 */
public class ListMap {
    private static Map<Integer, Collection<StrongLink>> map = new HashMap<>();
    private int size = 0;

    public void add(int num, Collection<StrongLink> c) {
        size += c.size();
        Collection<StrongLink> set2 = map.get(num);
        if (set2 == null) {
            map.put(num, c);
        } else {
            set2.addAll(c);
        }
    }

    public Set<Map.Entry<Integer, Collection<StrongLink>>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public int size() {
        return size;
    }

    public Collection<StrongLink> get(int num) {
        return map.get(num);
    }

    public List<StrongLink> getOverlap(LinkNode node) {
        // todo
        List<StrongLink> list = new ArrayList<>();
        if(!node.isSingle()) return list;

        for (Map.Entry<Integer, Collection<StrongLink>> entry : entrySet()) {
            for (StrongLink link : entry.getValue()) {
                if ((link.node1.isSingle() && node.overlap(link.node1))
                        || (link.node2.isSingle() && node.overlap(link.node2))) {
                    list.add(link);
                }
            }
        }
        return list;
    }
}
