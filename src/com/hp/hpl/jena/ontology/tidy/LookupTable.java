/*
 * Created on 24-Nov-2003
 *
 */
package com.hp.hpl.jena.ontology.tidy;
import java.io.*;
import com.hp.hpl.jena.shared.*;
import java.util.*;
/**
 * @author Jeremy J. Carroll
 *
 */
class LookupTable implements Constants {


	static final private String DATAFILE = "etc/owl-syntax.ser";
	static final int key[];
	static final int value[];
	static final byte action[];
	static final int WW = 9;
static {

	try {
		long t = System.currentTimeMillis();
		FileInputStream istream = new FileInputStream(DATAFILE);
		ObjectInputStream p = new ObjectInputStream(istream);
		key = (int[]) p.readObject();
		System.err.println("keys: "+(System.currentTimeMillis()-t));
		t = System.currentTimeMillis();
		
		value = (int[]) p.readObject();
		System.err.println("values: "+(System.currentTimeMillis()-t));
		t = System.currentTimeMillis();
		action = (byte[])p.readObject();
		System.err.println("actions: "+(System.currentTimeMillis()-t));
		t = System.currentTimeMillis();
		Vector v = (Vector) p.readObject();
		System.err.println("cats: "+(System.currentTimeMillis()-t));
		t = System.currentTimeMillis();
		Iterator it = v.iterator();
		while (it.hasNext()) {
			((CategorySet) it.next()).restore();
		}
		System.err.println("catsini: "+(System.currentTimeMillis()-t));
		
		istream.close();
	} catch (IOException e) {
		throw new BrokenException(e);
	} catch (ClassNotFoundException e) {
		throw new BrokenException(e);
	}
}

static int qrefine(int s, int p, int o) {
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
 static int subject(int refinement) {
	 return (int) (value[refinement] >> (2 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @param prop The old subcategory for the property.
  * @return The new subcategory for the property.
  */
 static int prop(int refinement) {
	 return (int) (value[refinement] >> (1 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @param obj The old subcategory for the object.
  * @return The new subcategory for the object.
  */
 static int object(int refinement) {
	 return (int) (value[refinement] >> (0 * WW)) & MM;
 }
 /**
  * 
  * @param refinement The result of {@link #refineTriple(int,int,int)}
  * @return An integer reflecting an action needed in response to this triple.
  */
 static int action(int k) {
	 return  action[k] & ~(DL | ObjectAction|SubjectAction|RemoveTriple);
 }
 /**
 * 
 * @param refinement The result of {@link #refineTriple(int,int,int)}
 * @return True if this triple is <em>the</em> triple for the blank node object.
 */
 static boolean tripleForObject(int k) {
	 return (action[k] & ObjectAction) == ObjectAction;
 }
 static boolean tripleForSubject(int k) {
	 return (action[k] & SubjectAction) == SubjectAction;
 }
 static boolean removeTriple(int k) {
	 return //false;
	 (action[k] & RemoveTriple) == RemoveTriple;
 }	
 /**
 *@param refinement The result of {@link #refineTriple(int,int,int)}
 * @return Is this triple in DL?.
 */
static boolean dl(int k) {
	return (action[k] & DL) == DL;
}
 /**
  * @param k
  * @return
  */
 public static byte allActions(int k) {
	 return action[k];
 }

}
