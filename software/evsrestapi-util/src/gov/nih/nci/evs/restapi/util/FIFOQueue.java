package gov.nih.nci.evs.restapi.util;
import java.util.*;
import java.util.Queue;

class FIFOQueue {
    private List<String> data;
    private int p_start;

    public FIFOQueue() {
        data = new ArrayList<String>();
        p_start = 0;
    }
    public boolean enQueue(String x) {
        data.add(x);
        return true;
    };

    public boolean deQueue() {
        if (isEmpty() == true) {
            return false;
        }
        p_start++;
        return true;
    }

    public String Front() {
        return data.get(p_start);
    }

    public boolean isEmpty() {
        return p_start >= data.size();
    }

    public static void main(String[] args) {
        FIFOQueue q = new FIFOQueue();
        q.enQueue("String 1");
        q.enQueue("String 2");
        q.enQueue("String 3");
        q.enQueue("String 4");
        q.enQueue("String 5");

        while (!q.isEmpty()) {
			String s = q.Front();
			q.deQueue();
			System.out.println(s);
		}
    }
};