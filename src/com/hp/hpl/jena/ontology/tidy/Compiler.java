/*
 * Created on 22-Nov-2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package com.hp.hpl.jena.ontology.tidy;

import java.util.*;

/**
 * @author jjc
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Compiler {
	final private int W = SubCategorize.W;
	SortedSet possible = new TreeSet();
	SortedMap huge = new TreeMap();
	SortedMap lessThan = new TreeMap();
	SortedMap meet = new TreeMap();
	SortedMap comparablePairs = new TreeMap();
	Set morePossible = new TreeSet();
	void add(int i) {
		Integer ii = new Integer(i);
		if (!possible.contains(ii))
			morePossible.add(ii);
	}
	Long toLong(int s2, int p2, int o2) {
		return new Long(
			((((long) s2) << (2 * W))
				| (((long) p2) << (1 * W))
				| (((long) o2) << (0 * W))));
	}
	void spo(int s, int p, int o) {
		long r = SubCategorize.refineTriple(s, p, o);

		if (r != Grammar.Failure //	 && !SubCategorize.dl(r)
		) {
			huge.put(toLong(s, p, o), new Long(r));
			add(SubCategorize.subject(r));
			add(SubCategorize.prop(r));
			add(SubCategorize.object(r));
		}

	}
	void add(int a, int b, int c) {
		spo(a, b, c);
		if (a != b)
			spo(b, a, c);
		if (a != c)
			spo(b, c, a);

		if (b != c) {
			spo(a, c, b);
			if (a != c)
				spo(c, a, b);
			if (a != b)
				spo(c, b, a);
		}
	}
	void initPossible() {
		add(Grammar.blank);
		add(Grammar.classOnly);
		add(Grammar.propertyOnly);
		add(Grammar.literal);
		add(Grammar.liteInteger);
		add(Grammar.dlInteger);
		add(Grammar.userTypedLiteral);
		add(Grammar.userID);
		Set ignore = new HashSet();
		ignore.add(new Integer(0));
		Iterator it = morePossible.iterator();
		while (it.hasNext()) {
			int c = ((Integer) it.next()).intValue();
			int cat[] = CategorySet.getSet(c);
			for (int i = 0; i < cat.length; i++)
				ignore.add(new Integer(cat[i]));
		}
		for (int i = 0; i < CategorySet.unsorted.size(); i++)
			if (!ignore.contains(new Integer(i)))
				add(i);
	}
	static long start = System.currentTimeMillis();
	void go() {
		initPossible();
		while (!morePossible.isEmpty()) {
			possible.addAll(morePossible);
			Iterator it1 = morePossible.iterator();
			morePossible = new HashSet();
			int c = 0;
			while (it1.hasNext()) {
				log("G",c++);
				int n1 = ((Integer) it1.next()).intValue();
				Iterator it2 = possible.iterator();
				while (it2.hasNext()) {
					Integer ni2 = (Integer) it2.next();
					Iterator it3 = possible.tailSet(ni2).iterator();
					while (it3.hasNext())
						add(
							n1,
							ni2.intValue(),
							((Integer) it3.next()).intValue());
				}
			}
		}
		log("GX",0);
		lessThan();
		makeMeet();
	}

	static final int[] intersection(int a[], int b[]) {
		int rslt0[] = new int[a.length];
		int k = 0;
		for (int i = 0; i < a.length; i++)
			if (Q.member(a[i], b))
				rslt0[k++] = a[i];
		int rslt1[] = new int[k];
		System.arraycopy(rslt0, 0, rslt1, 0, k);
		return rslt1;
	}
	void makeMeet() {
		Iterator it1 = possible.iterator();
		int c = 0;
		while (it1.hasNext()) {
			Integer ni1 = (Integer) it1.next();
			int i1 = ni1.intValue();
			int c1[] = CategorySet.getSet(i1);
			Iterator it2 = possible.tailSet(ni1).iterator();
			while (it2.hasNext()) {
				if (c++%1000==0)
					log("MM",c);
				Integer ni2 = (Integer) it2.next();
				int i2 = ni2.intValue();
				int c2[] = CategorySet.getSet(i2);
				Pair p = new Pair(i1, i2);
				Integer in = (Integer) comparablePairs.get(p);
				if (in != null) {
					meet.put(p, in);
				} else {
					int c3[] = intersection(c1, c2);
					if (c3.length != 0) {
						int gt = c3[0];
						for (int i = 1; i < c3.length; i++) {
							Pair p2 = new Pair(gt, c3[i]);
							Integer in2 = (Integer) comparablePairs.get(p2);
							if (in2 != null && in2.intValue() == gt) {
								gt = c3[i];
							}
						}
						for (int i = 0; i < c3.length; i++) {
							if (gt != c3[i]) {
								Pair p2 = new Pair(gt, c3[i]);
								Integer in2 = (Integer) comparablePairs.get(p2);
								if (in2 != null && in2.intValue() == gt) {
									System.err.println("Shouldn't happen");
								}
							}
						}
					}
				}

			}

		}
		log("MMX",0);
	}
	void lessThan(int from, int to) {
		Pair p = new Pair(from, to);
		if (!comparablePairs.containsKey(p)) {
			comparablePairs.put(p, new Integer(to));
			
			if (from != to) {
				Set s = (Set) lessThan.get(new Integer(from));
				if (s == null) {
					s = new HashSet();
					lessThan.put(new Integer(from), s);
				}
				s.add(new Integer(to));
			}
			
		}
	}
	void lessThan() {
		int i = 0;
		Iterator it = huge.entrySet().iterator();
		Map.Entry ent;
		while (it.hasNext()) {
			if (i++%1000==0) log("LT",i);
			ent = (Map.Entry) it.next();
			long k = ((Long) ent.getKey()).longValue();
			long v = ((Long) ent.getValue()).longValue();
			lessThan(SubCategorize.subject(k), SubCategorize.subject(v));
			lessThan(SubCategorize.prop(k), SubCategorize.prop(v));
			lessThan(SubCategorize.object(k), SubCategorize.object(v));
		}
	}
	private void log(String m,int c) {
		System.err.println(
				m +": " + c + " " +
			morePossible.size()
				+ "/"
				+ possible.size()
				+ "/"
				+ huge.size()
				+ "/"
				+ comparablePairs.size()
				+ "/"
				+ meet.size()
				+ "/"
				+ (System.currentTimeMillis() - start) / 1000);
	}
	public static void main(String[] args) {
		Compiler c = new Compiler();
		c.go();
	}
	static private class Pair implements Comparable {
		final int a, b;
		Pair(int A, int B) {
			if (A < B) {
				a = A;
				b = B;
			} else {
				a = B;
				b = A;
			}
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			Pair p = (Pair) o;
			int rslt = a - p.a;
			if (rslt == 0)
				rslt = b - p.b;
			return rslt;
		}
		public boolean equals(Object o) {
			return o instanceof Pair && compareTo(o) == 0;
		}
	}
}
