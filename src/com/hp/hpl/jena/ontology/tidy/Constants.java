/*
 * Created on 24-Nov-2003
 *
 */
package com.hp.hpl.jena.ontology.tidy;

/**
 * @author Jeremy J. Carroll
 *
 */
interface Constants {
	final static int RemoveTriple = (1<<5);
	static final int FirstOfOne = 8;
	static final int FirstOfTwo = 16;
	static final int SecondOfTwo = 24;
	static final int ObjectAction = 2;
	static final int SubjectAction = 4;
	static final int DL = 1;
	static final int ActionShift = 6;
	static final int CategoryShift = 9;
	// S, P, O and A takes 33 bits 
	// In the triple table there are only 32 bits,
	// This is OK since Triples only uses 8 bits not 9
	// for S.
	static final int WW = CategoryShift;
	static final int BadXSD = 1<<WW;
	static final int BadOWL = 2<<WW;
	static final int BadRDF = 3<<WW;
	static final int DisallowedVocab = 4<<WW;
	static final int Failure = -1;

	static final int ActionMask = (1 << ActionShift) - 1;
	final String DATAFILE = "etc/owl-syntax.ser";

	static final int W = 16;
	//static final int WW = 9;
	static final int M = (1 << W) - 1;
	static final int MM = (1<<WW) - 1;
	
}
