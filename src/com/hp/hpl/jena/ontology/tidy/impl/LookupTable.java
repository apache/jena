/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: LookupTable.java,v 1.6 2003-12-04 10:49:15 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;
import java.util.*;
import com.hp.hpl.jena.shared.*;
import java.io.*;
import com.hp.hpl.jena.reasoner.rulesys.Util;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class LookupTable
	extends AbsLookup
	implements LookupLimits, Serializable {
		static final long serialVersionUID = -6759062485496972733L;


	/**
	 * To be called by the OWL syntax compiler only,
	 * the arguments are precisely the data needed by 
	 * this class.
	 * @param propId  Converts a category-set number into a byte usable in the propertiesUsedWithObject parameter.
	 * @param propertiesUsedWithObject
	 *     For each category-set number lists the propIds that {@link #qrefine}(?,p,o)
	 *    may return non-Failure.
	 * @param keysByObjectAndPropertyIndex
	 *   The entry in this table corresponding to an entry in propertiesUsedWithObject
	 *   is a list of subject categories and the result to be returned
	 *   by {@link #qrefine}.
	 * @param refinedSubject
	 *   This table gives a refined category set, indexed by old category set
	 *  and the new subject field from any entry in the keysByObjectAndPropertyIndex
	 *  table.
	 * @param refinedProperty
	 *   This table gives a refined category set, indexed by old category set
	 *  and the new property field from any entry in the keysByObjectAndPropertyIndex
	 *  table. For each i, either refinedProperty[i]==null or refinedProperty[i][0] == -1.
	 * @param refinedObject
	 *   This table gives a refined category set, indexed by old category set
	 *  and the new subject field from any entry in the keysByObjectAndPropertyIndex
	 *  table.
	 */
	public LookupTable(
		byte propId[],
		byte propertiesUsedWithObject[][],
		int keysByObjectAndPropertyIndex[][][],
		int refinedSubject[][],
		int refinedProperty[][],
		int refinedObject[][]) {
		this.propId = propId;
		this.propertiesUsedWithObject = propertiesUsedWithObject;
		this.keysByObjectAndPropertyIndex = keysByObjectAndPropertyIndex;
		this.refinedSubject = refinedSubject;
		this.refinedProperty = refinedProperty;
		this.refinedObject = refinedObject;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#done(int)
	 */
	public void done(int key) {

	}

	final private byte propertiesUsedWithObject[][];
	final private int keysByObjectAndPropertyIndex[][][];
	final private byte propId[];
	final private int refinedSubject[][];
	final private int refinedProperty[][];
	final private int refinedObject[][];

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#qrefine(int, int, int)
	 */
	public int qrefine(int s, int p, int o) {
		byte props[] = propertiesUsedWithObject[o];
		byte propID = propId[p];
		//	if ( propID == 0 )  redundant 
		//	  return Failure;
		if (props==null)
		  return Failure;
		int propIx = Arrays.binarySearch(props, propID);
		if (propIx < 0) // The prop is not in the table
			return Failure;

		int keyTable[] = keysByObjectAndPropertyIndex[o][propIx];

		int subjSelector = s << SUBJSHIFT;
		int keyIx = Arrays.binarySearch(keyTable, subjSelector);

		if (keyIx >= 0)
			throw new BrokenException(
				"LookupTable malformed - value: " + subjSelector);

		keyIx = -keyIx - 1;
		// insertion point as in Arrays.binarySearch documentation.

		if (keyIx == keyTable.length)
			return Failure;
		if ((keyTable[keyIx] & SUBJMASK) == subjSelector)
			return keyTable[keyIx]; // The subj is in the table.
		return Failure;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#subject(int)
	 */
	public int subject(int old, int key) {
		if (old != key >>> SUBJSHIFT)
			throw new IllegalArgumentException("Internal error in OWL Syntax Checker");
		return refinedSubject[old][(key & NSUBJMASK) >> NSUBJSHIFT];
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#prop(int)
	 */
	public int prop(int old, int key) {
		return refinedProperty[old][(key & NPROPMASK) >> NPROPSHIFT];
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#object(int)
	 */
	public int object(int old, int key) {
		return refinedObject[old][(key & NOBJMASK) >> NOBJSHIFT];
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.ontology.tidy.impl.Lookup#allActions(int)
	 */
	public byte allActions(int key) {
		return (byte) ((key & ACTIONMASK) >> ACTIONSHIFT);
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

		int rslt = CategorySet.find(intersection(cc0, cc1), true);
		return Grammar.isPseudoCategory(rslt) ? Failure : rslt;
	}
	static private Lookup theInstance;
	static final private String DATAFILE = "etc/owl-syntax.ser";
	static private Lookup restore() {
		Lookup rslt;
		try {
			//  long t = System.currentTimeMillis();
			InputStream istream = Util.openResourceFileAsStream(DATAFILE);

			if (istream == null)
				throw new BrokenException("Failed to find compiled table.");
			ObjectInputStream p = new ObjectInputStream(istream);
			rslt = (Lookup) p.readObject();
			//  System.err.println("table: "+(System.currentTimeMillis()-t));
			//  t = System.currentTimeMillis();
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
		return rslt;
	}
	public void save() {
		try {
			FileOutputStream ostream = new FileOutputStream(DATAFILE);
			ObjectOutputStream p = new ObjectOutputStream(ostream);
			p.writeObject(this);
			p.writeObject(CategorySet.unsorted);
			p.flush();
			ostream.close();
		} catch (IOException e) {
			throw new BrokenException(e);
		}

	}
	/**
	 * @return
	 */
	public static Lookup get() {
		if (theInstance == null) {
			theInstance = restore();
		}
		return theInstance;
	}

}

/*
	(c) Copyright Hewlett-Packard Company 2003
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/