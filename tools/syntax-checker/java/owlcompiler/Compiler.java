/*
 * Created on 22-Nov-2003
 * 
 * 
 * Perf notes:
 * 
 * Running WG tests
 *  - no checking whatsoever (just parsing and imports)
 *    12.5 sec
 *  - checking with qrefine 18.5 (down to 17.3)
 *  - checking with old refineTriple 28
 *   (down to 20 with reduced grammar)
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package owlcompiler;

import com.hp.hpl.jena.ontology.tidy.impl.CategorySet;
import com.hp.hpl.jena.ontology.tidy.impl.LookupTable;
import com.hp.hpl.jena.ontology.tidy.impl.LookupLimits;
//import com.hp.hpl.jena.ontology.tidy.impl.Grammar;
//import com.hp.hpl.jena.ontology.tidy.impl.Q;
import com.hp.hpl.jena.shared.*;
import java.util.*;
import java.io.*;
/**
 * @author jjc
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class Compiler implements Constants, LookupLimits {

	final private String SAVEFILE = "tmp/huge.ser";
	static private long lookup[][];
/*
	boolean validate() {
		if (prop(qrefine(14, 80, 89)) != 80) {
			System.err.println("gggg");
			return false;
		}
		Iterator it = huge.keySet().iterator();
		while (it.hasNext()) {
			Long l = (Long) it.next();
			long ll = l.longValue();
			int s = subject(ll);
			int p = prop(ll);
			int o = object(ll);
			long lk = qrefine(s, p, o);
			int ik = LookupTable.qrefine(s, p, o);
			if (subject(lk) != LookupTable.subject(ik))
				return false;

			if (prop(lk) != LookupTable.prop(ik))
				return false;
			if (object(lk) != LookupTable.object(ik))
				return false;
			if (allActions(lk) != LookupTable.allActions(ik))
				return false;
		}
		return true;
	}
	*/
	private void saveResults() {
		final int NCATS = CategorySet.unsorted.size();
		byte propId[] = new byte[NCATS];
		byte propertiesUsedWithObject[][] = new byte[NCATS][];
		int keysByObjectAndPropertyIndex[][][] = new int[NCATS][][];
		
		Map objectValues[] = new Map[NCATS];
		
		int refinedSubject[][] = new int[NCATS][];
		int refinedProperty[][] = new int[NCATS][];
		int refinedObject[][] = new int[NCATS][];
		
		subjInfo.save(refinedSubject,false,NSUBJMAX);
		propInfo.save(refinedProperty,true,NPROPMAX);
		objInfo.save(refinedObject,false,NOBJMAX);
		
		propInfo.compressBefore(propId);
		
		Iterator it = huge.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry)it.next();
			short key[] = expand(((Long)ent.getKey()).longValue());
			short val[] = expand(((Long)ent.getValue()).longValue());
			Map vals = objectValues[key[2]];
			if (vals == null) {
				vals = new TreeMap();
				objectValues[key[2]] = vals;
			}
			if (key[0]>SUBJMAX)
			  throw new BrokenException("Field width limit for SUBJECT exceeded");
			if (val[3]>ACTIONMAX)
  			throw new BrokenException("Field width limit for ACTION exceeded");
  	  Byte pId = new Byte(propId[key[1]]);
  		Set refine = (Set)vals.get(pId);
  		if ( refine == null ) {
  			refine = new HashSet();
  			vals.put(pId,refine);
  		}
			refine.add(new Integer(
			  ( key[0] << SUBJSHIFT )
			  | (subjInfo.newId(key[0],val[0])<<NSUBJSHIFT)
			  | (propInfo.newId(key[1],val[1])<<NPROPSHIFT)
			  | (objInfo.newId(key[2],val[2])<<NOBJSHIFT)
			  | (val[3]<<ACTIONSHIFT)
			  ));
		}
		for (int i=0;i<NCATS;i++) {
			if (objectValues[i]!=null) {
				Map vals = objectValues[i];
				propertiesUsedWithObject[i] = new byte[vals.size()];
				keysByObjectAndPropertyIndex[i] = new int[vals.size()][];
				int ix = 0;
				Iterator mit = vals.entrySet().iterator();
				while (mit.hasNext()) {
					Map.Entry ent = (Map.Entry)mit.next();
					propertiesUsedWithObject[i][ix] = ((Byte)ent.getKey()).byteValue();
					Set refine = (Set)ent.getValue();
					int a[] = new int[refine.size()];
					Iterator rit = refine.iterator();
					int aix = 0;
					while (rit.hasNext())
					  a[aix++] = ((Integer)rit.next()).intValue();
					Arrays.sort(a);
					keysByObjectAndPropertyIndex[i][ix] = intern(a);
					ix++;
				}
			}
		}
		new LookupTable(
		propId,
		propertiesUsedWithObject,
		keysByObjectAndPropertyIndex,
		refinedSubject,
		refinedProperty,
		refinedObject).save();
		
		System.err.println(internedCalled + " calls to intern.");
		System.err.println(internedCalledLength + " total array length.");
		
		System.err.println(internedIntArrays.size() + " interned arrays.");
		System.err.println(internedAddedLength + " interned array length.");
	}

	private void initLookup() {
		lookup = new long[huge.size()][2];
		Iterator it = huge.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			lookup[i][0] = ((Long) ent.getKey()).longValue();
			lookup[i][1] = ((Long) ent.getValue()).longValue();
			i++;
		}
	}

	static private Comparator comp = new Comparator() {

		public int compare(Object o1, Object o2) {

			long rslt = ((long[]) o1)[0] - ((long[]) o2)[0];
			if (rslt < 0)
				return -1;
			if (rslt > 0)
				return 1;
			return 0;
		};
	};
	/*
	static {
		Compiler c = new Compiler();
		c.restore();
		c.initLookup();
		for (int i = 1; i < lookup.length; i++)
			if (comp.compare(lookup[i - 1], lookup[i]) >= 0) {
				throw new BrokenException("lookup init");
			}
	}
	*/
	static long qrefine(int s, int p, int o) {
		long key[] = { SubCategorize.toLong(s, p, o), 0 };
		int rslt = Arrays.binarySearch(lookup, key, comp);
		if (rslt < 0)
			return Failure;
		else
			return lookup[rslt][1];
	}
/*
	private boolean restore() {
		try {
			FileInputStream istream = new FileInputStream(SAVEFILE);
			ObjectInputStream p = new ObjectInputStream(istream);
			huge = (SortedMap) p.readObject();
			possible = (SortedSet) p.readObject();
			Vector v = (Vector) p.readObject();
			Iterator it = v.iterator();
			while (it.hasNext()) {
				((CategorySet) it.next()).restore();
			}
			istream.close();
		} catch (FileNotFoundException ee) {
			return false;
		} catch (IOException e) {
			throw new BrokenException(e);
		} catch (ClassNotFoundException e) {
			throw new BrokenException(e);
		}
		return true;
	}
	private void save() {
		try {
			FileOutputStream ostream = new FileOutputStream(SAVEFILE);
			ObjectOutputStream p = new ObjectOutputStream(ostream);
			p.writeObject(huge);
			p.writeObject(possible);
			p.writeObject(CategorySet.unsorted);
			p.flush();
			ostream.close();
		} catch (IOException e) {
			throw new BrokenException(e);
		}
	}
	*/
	class Info {
		final String name;
		Info(String n) {
			name = n;
		}
		/**
		 * {@link save} must be called before this can be used.
		 * @param oldCat
		 * @param newCat
		 * @return
		 */
		int newId(short oldCat, short newCat) {
			int a[] = (int[])before.get(new Integer(oldCat));
			int rslt = Arrays.binarySearch(a,newCat);
			if (rslt < 0)
			  throw new IllegalArgumentException("compiler error");
			return rslt;
		}
		/**
		 * gives a mapping from the category set number
		 * to a new number, or -1 if category not in before
		 * @param propId
		 */
		void compressBefore(byte[] propId) {
			int i;
			for (i=0;i<propId.length;i++)
			  propId[i] = -1;
			int v = 1;
			Iterator it = before.keySet().iterator();
			while (it.hasNext()) {
				propId[((Integer)it.next()).intValue()] = (byte)v++;
			}
			if (v>127)
			  throw new BrokenException("Width limit exceeded - propId?");
		}
		Map before = new HashMap();
		Set after = new HashSet();
		void add(Integer from, Integer to ) {
			after.add(to);
			Set bb = (Set)before.get(from);
			if ( bb == null ) {
				bb = new HashSet();
				before.put(from,bb);
			}
			bb.add(to);
		}
		void dump() {
			System.out.println("** "+name+" **" );
			System.out.println(before.size() + " in total.");
			System.out.println(after.size()+ " after.");
			int sz[] = new int[after.size()+1];
			Iterator i = before.values().iterator();
			while ( i.hasNext())
			  sz[((Set)i.next()).size()]++;
			for (int j=0; j<sz.length;j++)
			  if (sz[j]!=0)
			     System.out.println(j+"\t"+sz[j]);
			Set s = new HashSet(possible);
			s.removeAll(before.keySet());
			if ( s.size() > before.keySet().size()) {
				System.out.println("Including:");
				s = before.keySet();
			} else {
				System.out.println("Excluding:");
			}
			Iterator it = s.iterator();
			while (it.hasNext()) {
				System.out.println(CategorySet.catString(((Integer)it.next()).intValue()));
			}
		}
		/**
		 * This is destructive.
		 * It can only be called once.
		 * It must be called before {@link #newId}.
		 * @param refine  An array to be initialized
		 * @param isProp  Is this the property field 
		 * @param max     Max that must be checked.
		 */
		void save(int refine[][],boolean isProp, int max) {
			Iterator it = before.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry ent = (Map.Entry)it.next();
				int k = ((Integer)ent.getKey()).intValue();
				Set values = (Set)ent.getValue();
				int a[] = new int[values.size()+(isProp?1:0)];
				if (a.length>max)
				  throw new BrokenException("Field width limit exceeded");
				int ix = 0;
				if ( isProp )
				  a[ix++]=-1;
				Iterator vit = values.iterator();
				while (vit.hasNext()) {
					a[ix++] = ((Integer)vit.next()).intValue();
				}
				Arrays.sort(a);
				refine[k] = intern(a);
				ent.setValue(refine[k]);
			}
		}
	}
	
	int internedCalled = 0;
	long internedCalledLength = 0;
	long internedAddedLength = 0;
	Map internedIntArrays = new TreeMap(new Comparator(){

		public int compare(Object o1, Object o2) {
			int a1[] = (int[])o1;
			int a2[] = (int[])o2;
			for (int i=0; true; i++) {
				if (i==a1.length)
				  return i==a2.length?0:-1;
				if (i==a2.length)
				  return 1;
				int diff = a1[i] - a2[i];
				if ( diff != 0)
				  return diff;
			}
		}
	});
	int[] intern(int a[]) {
		internedCalled++;
		internedCalledLength+=a.length;
		int r[] = (int[])internedIntArrays.get(a);
		if ( r==null ) {
			r = a;
			internedIntArrays.put(a,a);
			internedAddedLength += a.length;
		}
		return r;
	}
	Info dummyInfo = new Info("dummy");
	Info subjInfo = new Info("subject");
	Info propInfo = new Info("property");
	Info objInfo = new Info("object");
	SortedSet possible = new TreeSet();
	SortedMap huge = new TreeMap();
	SortedMap moreThan = new TreeMap();
	SortedMap lessThan = new TreeMap();
	SortedMap comparablePairs = new TreeMap();
	Map morePossible = new HashMap();
	Map oldMorePossible = null;
	void add(Info info, int i, int was) {
		Integer ii = new Integer(i);
		Integer ww =  new Integer(was);
		if ((!possible.contains(ii)) && !morePossible.containsKey(ii))
			morePossible.put(ii,ww);
	  info.add(ww,ii);
	}
	void add(int i) {
		add(dummyInfo, i, -1);
	}
	Long toLong(int s2, int p2, int o2) {
		return new Long(
			((((long) s2) << (2 * W))
				| (((long) p2) << (1 * W))
				| (((long) o2) << (0 * W))));
	}
	void spo(int s, int p, int o) {
		//long r = SubCategorize.refineTriple(s, p, o);
		long r1;
		if (oldMorePossible != null) {
			boolean sOld, pOld, oOld;
			int s1, p1, o1;
			Integer is0 = (Integer) oldMorePossible.get(new Integer(s));
			if (is0 != null) {
				sOld = true;
				s1 = is0.intValue();
			} else {
				sOld = false;
				s1 = s;
			}
			Integer ip0 = (Integer) oldMorePossible.get(new Integer(p));
			if (ip0 != null) {
				pOld = true;
				p1 = ip0.intValue();
			} else {
				pOld = false;
				p1 = p;
			}
			Integer io0 = (Integer) oldMorePossible.get(new Integer(o));
			if (io0 != null) {
				oOld = true;
				o1 = io0.intValue();
			} else {
				oOld = false;
				o1 = o;
			}
			Long rold = toLong(s1, p1, o1);
			Long old = (Long) huge.get(rold);
			if (old == null) {
				//				if (Grammar.Failure != r) {
				//					if (Grammar.Failure != r)
				//						System.err.println("E2");
				//					System.err.println(
				//						CategorySet.toString(toLong(s1, p1, o1)));
				//					System.err.println(CategorySet.toString(toLong(s, p, o)));
				//					System.err.println(CategorySet.toString(r));
				//				}
				return;
			}
			long r0 = old.longValue();
			//			if ( SubCategorize.spo(r0)==rold.longValue()) 
			//			  r1 = r0;
			//			else
			r1 =
				SubCategorize.refineTriple(
					sOld ? s : subject(r0),
					pOld ? p : prop(r0),
					oOld ? o : object(r0));
			//			if (r1 != r) {
			//				System.err.println(CategorySet.toString(toLong(s0, p0, o0)));
			//				System.err.println(CategorySet.toString(toLong(s, p, o)));
			//				System.err.println(CategorySet.toString(r0));
			//				System.err.println(CategorySet.toString(r));
			//				System.err.println(CategorySet.toString(r1));
			//
			//			}

		} else {
			r1 = SubCategorize.refineTriple(s, p, o);
			;
		}

		if (r1 != Failure //	 && !SubCategorize.dl(r)
		) {
			huge.put(toLong(s, p, o), new Long(r1));
			add(subjInfo, subject(r1), s);
			add(propInfo, prop(r1), p);
			add(objInfo, object(r1), o);
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
		Iterator it = morePossible.keySet().iterator();
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
	void compute() {
		initPossible();
		while (!morePossible.isEmpty()) {
			possible.addAll(morePossible.keySet());
			Iterator it1 = morePossible.entrySet().iterator();
			morePossible = new HashMap();
			int c = 0;
			while (it1.hasNext()) {
				if (c++ % 20 == 0)
					log("G", c);
				Map.Entry ent = (Map.Entry) it1.next();
				int n1 = ((Integer) ent.getKey()).intValue();
				//	int old1 = ((Integer) ent.getValue()).intValue();
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
			oldMorePossible = morePossible;
		}
	//	System.err.println("Saving");
	//	save();
		log("GX", 0);
		subjInfo.dump();
		propInfo.dump();
		objInfo.dump();
	}
	/*
	private void go2() {
		if (restore())
			System.err.println("Restore successful");
		else {
			System.err.println("Restore unsuccessful: recomputing");
			compute();
		}
		System.err.println(validate() ? "Good" : "Bad");
	}
	*/
	private void go() {
	//	if (restore())
	//		System.err.println("Restore successful");
	//	else {
	//		System.err.println("Restore unsuccessful: recomputing");
			compute();
	//	}
		log("GX", 0);
		makeLessThan();
		//	roy();
		//makeMeet();
		//makeJoin();
		//	pairs();
		//	System.err.println("XXX=" + evalPairs());
		findUseless();
		System.err.println("Saving results");
		saveResults();
	//	System.err.println("Saving data");
	//	save();
	}
/*
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
	*/
	private Integer compare(int i, int j) {
		return compare(new Pair(i, j));
	}
	private Integer compare(Pair p) {
		return (Integer) comparablePairs.get(p);
	}
	private Integer compare(Integer i, Integer j) {
		return compare(i.intValue(), j.intValue());
	}

	/*
	 * from *>* to;  where *>* is the partial order
	 * defined by category sets.   
	 */
	private void makeLessThan(int from, int to) {
		//if (from == to)
		//		return;
		Pair p = new Pair(from, to);
		if (!comparablePairs.containsKey(p)) {
			comparablePairs.put(p, new Integer(to));
			Set s = (Set) lessThan.get(new Integer(from));
			if (s == null) {
				s = new HashSet();
				lessThan.put(new Integer(from), s);
			}
			s.add(new Integer(to));
			s = (Set) moreThan.get(new Integer(to));
			if (s == null) {
				s = new HashSet();
				moreThan.put(new Integer(to), s);
			}
			s.add(new Integer(from));
		}
	}
	/*
	void lessThan() {
		int i = 0;
		Iterator it = huge.entrySet().iterator();
		Map.Entry ent;
		while (it.hasNext()) {
			if (i++ % 200000 == 0)
				log("LT", i);
			ent = (Map.Entry) it.next();
			long k = ((Long) ent.getKey()).longValue();
			long v = ((Long) ent.getValue()).longValue();
			lessThan(SubCategorize.subject(k), SubCategorize.subject(v));
			lessThan(SubCategorize.prop(k), SubCategorize.prop(v));
			lessThan(SubCategorize.object(k), SubCategorize.object(v));
		}
	}
	*/
	private void makeLessThan() {
		Iterator it1 = possible.iterator();
		int c = 0;
		while (it1.hasNext()) {
			Integer ni1 = (Integer) it1.next();
			int i1 = ni1.intValue();
			int c1[] = CategorySet.getSet(i1);
			Iterator it2 = possible.iterator();
			//		it2.next();
			while (it2.hasNext()) {
				if (c++ % 200000 == 0)
					log("LT", c);
				Integer ni2 = (Integer) it2.next();
				int i2 = ni2.intValue();
				int c2[] = CategorySet.getSet(i2);

				if (isLessThan(c1, c2))
					makeLessThan(i2, i1);
			}

		}
		log("LTX", 0);

	}
	static private boolean isLessThan(int small[], int big[]) {
		if (small.length > big.length)
			return false;
		int i = 0;
		int j = 0;
		while (true) {
			if (i == small.length)
				return true;
			if (j == big.length)
				return false;
			if (small[i] == big[j]) {
				i++;
				j++;
			} else {
				if (small[i] < big[j])
					return false;
				j++;
			}
		}
	}
	private void log(String m, int c) {
		System
			.err
			.println(
				m
				+ ": "
				+ c
				+ " "
				+ morePossible.size()
				+ "/"
				+ possible.size()
				+ "/"
				+ huge.size()
				+ "/"
				+ comparablePairs.size()
				+ "/"
		/*
		+ meet.size()
		+ "/"
		+ join.size()
		+ "{"
		+ pairs[0].size()
		+ ","
		+ pairs[1].size()
		+ ","
		+ pairs[2].size()
		+ "}" */
		+ (System.currentTimeMillis() - start) / 1000);
	}
	private Iterator supers(int x) {
		return ((Set) moreThan.get(new Integer(x))).iterator();
	}
	private void markSupers(long k, int act) {
		short spo[] = expand(k);
		boolean ok = false;
		Iterator s = supers(spo[0]);
		while (s.hasNext()) {
			Integer ss = (Integer) s.next();
			Iterator p = supers(spo[1]);
			while (p.hasNext()) {
				Integer pp = (Integer) p.next();
				Iterator o = supers(spo[2]);
				while (o.hasNext()) {
					Integer oo = (Integer) o.next();
					Long ll = toLong(ss, pp, oo);
					if (allActions(((Long) huge.get(ll)).longValue()) == act)
						plusPlus(ll);
					ok = ok || (ll.longValue() == k);
				}
			}
		}
		if (!ok)
			throw new BrokenException("impossible");
	}
	Map count = new HashMap();
	/**
	 * @param long1
	 */
	private void plusPlus(Long k) {
		int c[] = (int[]) count.get(k);
		if (c == null) {
			c = new int[] { 0 };
			count.put(k, c);
		}
		c[0]++;
	}

	/**
	 * @param ss
	 * @param pp
	 * @param oo
	 */
	private Long toLong(Integer ss, Integer pp, Integer oo) {
		return toLong(ss.intValue(), pp.intValue(), oo.intValue());
	}

	private void findUseless() {
		int cnt = 0;
		//int bad = 0;
		Iterator it = huge.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			long k = ((Long) ent.getKey()).longValue();
			long v = ((Long) ent.getValue()).longValue();
			if ( (k | allActions(v)) == v )
			    markSupers(k,allActions(v));
		}
		it = huge.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry ent = (Map.Entry) it.next();
			long val = ((Long) ent.getValue()).longValue();
			if (isUseless(new Long(val&((1L<<(3*W))-1)))) {
				cnt++;
				long newv = (long) RemoveTriple << (3 * W) | val;
					ent.setValue(new Long(newv));
				
			}

		}
		System.err.println(cnt + " marked as remove");
	}
	private boolean foo(int a, int b) {
		boolean rslt = a==b;
		if (!rslt)
		System.err.println(rslt + " : " + a + " == " + b);
		return rslt;
	}
	/**
	 * @param l
	 * @return
	 */
	static private int cycles[] =
		{ //Grammar.cyclic, 
			Grammar.cyclicRest, Grammar.cyclicFirst };
	private boolean isUseless(Long l) {
		short spo[] = expand(l.longValue());
		int cnt[] = ((int[]) count.get(l));
		return cnt != null && cnt[0] == nLessThan(spo[0]) * nLessThan(spo[1]) * nLessThan(spo[2]);
		/*
		if (cnt != nLessThan(spo[0]) * nLessThan(spo[1]) * nLessThan(spo[2]))
			return false;
		spo = expand(((Long) huge.get(l)).longValue());
		if (Q.intersect(CategorySet.getSet(spo[0]), cycles))
			return false;
		if (Q.intersect(CategorySet.getSet(spo[2]), cycles))
			return false;
		return true; */
	}

	/**
	 * @param s
	 */
	private int nLessThan(int s) {
		return ((Set) lessThan.get(new Integer(s))).size();

	}

	private short[] expand(long k) {
		return new short[] {
			(short) subject(k),
			(short) prop(k),
			(short) object(k),
			(short) allActions(k)};
	}
	public static void main(String[] args) {
		Compiler c = new Compiler();
		c.go();
	}

	private static short allActions(long k) {
		return (short) (k >> (long) (3 * W));
	} /**
					* 
					* @param refinement The result of {@link #refineTriple(int,int,int)}
					* @param subj The old subcategory for the subject.
					* @return The new subcategory for the subject.
					*/
	static private int subject(long refinement) {
		return (int) (refinement >> (2 * W)) & M;
	}
	/**
		* 
		* @param refinement The result of {@link #refineTriple(int,int,int)}
		* @param prop The old subcategory for the property.
		* @return The new subcategory for the property.
		*/
	static private int prop(long refinement) {
		return (int) (refinement >> (1 * W)) & M;
	}
	/**
		* 
		* @param refinement The result of {@link #refineTriple(int,int,int)}
		* @param obj The old subcategory for the object.
		* @return The new subcategory for the object.
		*/
	static private int object(long refinement) {
		return (int) (refinement >> (0 * W)) & M;
	}
	/**
		* @param r0
		* @return
		*/
	private static String toString(long r0) {
		if (r0 == -1)
			return "F";
		return "S"
			+ CategorySet.catString(subject(r0))
			+ " P"
			+ CategorySet.catString(prop(r0))
			+ " O"
			+ CategorySet.catString(object(r0));
	}
	/**
		* @param long1
		* @return
		*/
	private static String toString(Long long1) {
		return toString(long1.longValue());
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
		} /*
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
