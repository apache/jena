/*
 * Created on 24-Nov-2003
 *
 */
package com.hp.hpl.jena.ontology.tidy.impl;
import java.io.*;
import com.hp.hpl.jena.shared.*;
import java.util.*;


import com.hp.hpl.jena.reasoner.rulesys.Util;
/**
 * @author Jeremy J. Carroll
 *
 */
class LookupTable implements Constants, Lookup {


	static final int key[];
	static final int value[];
	static final byte action[];
	
static {

	try {
	//	long t = System.currentTimeMillis();
		InputStream istream = Util.openResourceFileAsStream(DATAFILE);
		
		if ( istream == null )
		  throw new BrokenException("Failed to find compiled table.");
		ObjectInputStream p = new ObjectInputStream(istream);
		key = (int[]) p.readObject();
	//	System.err.println("keys: "+(System.currentTimeMillis()-t));
	//	t = System.currentTimeMillis();
		
		value = (int[]) p.readObject();
		//System.err.println("values: "+(System.currentTimeMillis()-t));
		//t = System.currentTimeMillis();
		action = (byte[])p.readObject();
		//System.err.println("actions: "+(System.currentTimeMillis()-t));
		//t = System.currentTimeMillis();
		Vector v = (Vector) p.readObject();
		//System.err.println("cats: "+(System.currentTimeMillis()-t));
		//t = System.currentTimeMillis();
		Iterator it = v.iterator();
		while (it.hasNext()) {
			((CategorySet) it.next()).restore();
		}
		//System.err.println("catsini: "+(System.currentTimeMillis()-t));
		
		istream.close();
	} catch (IOException e) {
		throw new BrokenException(e);
	} catch (ClassNotFoundException e) {
		throw new BrokenException(e);
	}
	CategorySet.closed = true;
}

public int qrefine(int s, int p, int o) {
	int k = s<<(2*WW)|p<<WW|o;
	int rslt = Arrays.binarySearch(key, k);
	if (rslt < 0)
		return Failure;
	else
		return rslt;
}

 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @param subj The old subcategory for the subject.
  * @return The new subcategory for the subject.
  */
 public int subject(int refinement) {
	 return (int) (value[refinement] >> (2 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @param prop The old subcategory for the property.
  * @return The new subcategory for the property.
  */
 public int prop(int refinement) {
	 return (int) (value[refinement] >> (1 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @param obj The old subcategory for the object.
  * @return The new subcategory for the object.
  */
 public int object(int refinement) {
	 return (int) (value[refinement] >> (0 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @return An integer reflecting an action needed in response to this triple.
  */
 public int action(int k) {
	 return  action[k] & ~(DL | ObjectAction|SubjectAction|RemoveTriple);
 }
 /**
 * 
 * @param refinement The result of {@link #refineTriple(int,int,int)}
 * @return True if this triple is <em>the</em> triple for the blank node object.
 */
 public boolean tripleForObject(int k) {
	 return (action[k] & ObjectAction) == ObjectAction;
 }
 public boolean tripleForSubject(int k) {
	 return (action[k] & SubjectAction) == SubjectAction;
 }
 public boolean removeTriple(int k) {
	 return //false;
	 (action[k] & RemoveTriple) == RemoveTriple;
 }	
 /**
 *@param refinement The result of {@link #refineTriple(int,int,int)}
 * @return Is this triple in DL?.
 */
public boolean dl(int k) {
	return (action[k] & DL) == DL;
}
 /**
  * @param k
  * @return
  */
 public byte allActions(int k) {
	 return action[k];
 }
 
 public void done(int k){
 }


 static final int[] intersection(int a[], int b[]) {
	 int rslt0[] = new int[a.length];
	 int k = 0;
	 for (int i = 0; i < a.length; i++)
		 if (Arrays.binarySearch(b, a[i]) >= 0)
			 rslt0[k++] = a[i];
	 int rslt1[] = new int[k];
	 System.arraycopy(rslt0, 0, rslt1, 0, k);
	 return rslt1;
 }
 /* (non-Javadoc)
  * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#meet(int, int)
  */
 public int meet(int c0, int c1) {
	 int cc0[] = CategorySet.getSet(c0);
	 int cc1[] = CategorySet.getSet(c1);
		
	 return CategorySet.find(intersection(cc0,cc1),true);
 }

}
