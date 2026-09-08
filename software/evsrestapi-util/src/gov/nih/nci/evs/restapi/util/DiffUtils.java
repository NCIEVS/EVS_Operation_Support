package gov.nih.nci.evs.restapi.util;
import java.util.*;

public class DiffUtils {
	public static String DELETE = "Delete";
	public static String ADD = "Add";
    /**
     * Compare hash set.
     *
     * @param firstSet  the first set
     * @param secondSet the second set
     * @return the hash set< string>
     */

    public static HashSet<String> compareHashSet(final HashSet<String> firstSet,
                                           final HashSet<String> secondSet) {
		return setDifference(firstSet, secondSet);
    }

    /**
     * Compare map.
     *
     * @param firstMap  the first map
     * @param secondMap the second map
     * @return the hash map< string, string>
     */
    public static HashMap<String, String> compareMap(
           final HashMap<String, String> firstMap,
            final HashMap<String, String> secondMap) {
        final HashMap<String, String> extraValues = new HashMap<String, String>();
        final TreeSet<String> c = new TreeSet<String>(firstMap.keySet());
        final Iterator<String> iter = c.iterator();
        String key;
        while (iter.hasNext()) {
            key = iter.next();
            if (!secondMap.containsKey(key)) {
                extraValues.put(key, firstMap.get(key));
            }
        }
        return extraValues;
    }

    /**
     * Compare set.
     *
     * @param firstSet  the first set
     * @param secondSet the second set
     * @return the hash set< string>
     */
    public static HashSet<String> compareSet(final Set<String> firstSet,
                                       final Set<String> secondSet) {
		/*
        final HashSet<String> extraValues = new HashSet<String>();
        if ((firstSet != null)) {
            final Iterator<String> firstIt = firstSet.iterator();
            String item;
            while (firstIt.hasNext()) {
                item = firstIt.next();
                if (!secondSet.contains(item)) {
                    extraValues.add(item);
                }

            }
        }
        return extraValues;
        */
        return setDifference(new HashSet<>(firstSet), new HashSet<>(secondSet));
    }

    /**
     * Compare set.
     *
     * @param firstSet  the first set
     * @param secondSet the second set
     * @return the hash set< string>
     */
    public static Vector<String> compareSet(final Vector<String> firstSet,
                                      final Vector<String> secondSet) {

		HashSet set1 = Utils.vector2HashSet(firstSet);
		HashSet set2 = Utils.vector2HashSet(secondSet);
        return Utils.hashSet2Vector(setDifference(set1, set2));
        /*
        final Vector<String> extraValues = new Vector<String>();
        if (firstSet != null && secondSet != null) {

            final Iterator<String> firstIt = firstSet.iterator();
            String item;
            while (firstIt.hasNext()) {
                item = firstIt.next();
                if (!secondSet.contains(item)) {
                    extraValues.add(item);
                }

            }
        }
        return extraValues;
        */
    }

    public static HashMap<String, String> compareStringMap(
            final HashMap<String, String> firstMap,
            final HashMap<String, String> secondMap) {
        final HashMap<String, String> extraValues = new HashMap<String, String>();
        final TreeSet<String> c = new TreeSet<String>(firstMap.keySet());
        final Iterator<String> iter = c.iterator();
        String key;
        while (iter.hasNext()) {
            key = iter.next();
            if (!secondMap.containsKey(key)) {
                extraValues.put(key, firstMap.get(key));
            }
        }
        return extraValues;
    }

    public static HashSet<String> compareStringSet(final Set<String> firstSet,
                                       final Set<String> secondSet) {
		return setDifference(new HashSet<>(firstSet), new HashSet<>(secondSet));
    }

    public static Vector<String> compareStringVector(final Vector<String> firstSet,
                                         final Vector<String> secondSet) {

        if (firstSet == null && secondSet == null) {
            return new Vector<String>();
        } else if (firstSet == null) {
            return secondSet;
        } else if (secondSet == null) {
            return firstSet;
        }
        Vector<String> firstTest = new Vector<String>(firstSet);
        firstTest.removeAll(secondSet);
        return firstTest;
    }

    public static String set2String(HashSet<String> hset) {
		String setAsString = hset.toString();
		return setAsString;
	}

	public void testSet2String() {
        HashSet<String> fruits = new HashSet<>();
        // Add elements to the HashSet
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Mango");
        fruits.add("Orange");
        String setAsString = set2String(fruits);
        System.out.println("HashSet: " + fruits);
        System.out.println("String representation: " + setAsString);
    }

//////////////////////////////////////////////////////////////////////////////////////
    public static HashSet setDifference(HashSet set1, HashSet set2) {
		set1.removeAll(set2);
		return set1;
	}

    public static HashSet setDifference(Vector v1, Vector v2) {
		HashSet set1 = Utils.vector2HashSet(v1);
		HashSet set2 = Utils.vector2HashSet(v2);
        return setDifference(set1, set2);
	}

	public static Vector dumpHashSet(String label, String action, HashSet set) {
		Vector w = new Vector();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			String s = label + "\t" + action + "\t" + t;
			w.add(s);
		}
		return w;
	}

    public static Vector run(String label, String pathname1, String pathname2) {
		System.out.println("\nComputing differences between:");
		System.out.println("\t" + pathname1);
		System.out.println("\t" + pathname2);

		Vector w = new Vector();
		w.add("Data Type\tEdit Action\tData Value");
		Vector v1 = Utils.readFile(pathname1);
		Vector v2 = Utils.readFile(pathname2);
		HashSet set1 = Utils.vector2HashSet(v1);
		HashSet set2 = Utils.vector2HashSet(v2);

		HashSet clonedSet1 = new HashSet<>(set1);
		HashSet clonedSet2 = new HashSet<>(set2);
		HashSet set_diff = setDifference(clonedSet1, clonedSet2);
		Vector v = dumpHashSet(label, DELETE, set_diff);
		w.addAll(v);

		clonedSet1 = new HashSet<>(set1);
		clonedSet2 = new HashSet<>(set2);
		set_diff = setDifference(clonedSet2, clonedSet1);
		v = dumpHashSet(label, ADD, set_diff);
		w.addAll(v);
		set1.clear();
		set2.clear();
		System.out.println("\t" + label + "_diff: " + w.size());
		return w;
	}

    public static void main(String[] args) {
		new DiffUtils().testSet2String();
	}
}

