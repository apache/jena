/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */
package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * @author Jeremy J. Carroll
 *
 */
public interface Constants {


	static final int BadXSD = -10;
	static final int BadOWL = -11;
	static final int BadRDF = -12;

	final static int RemoveTriple = (1<<5);
	static final int FirstOfOne = 8;
	static final int FirstOfTwo = 16;
	static final int SecondOfTwo = 24;
	static final int ObjectAction = 2;
	static final int SubjectAction = 4;
	static final int DL = 1;
	/*
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
	*/
	static final int Failure = -1;

	//static final int ActionMask = (1 << ActionShift) - 1;
/*
	static final int W = 16;
	//static final int WW = 9;
	static final int M = (1 << W) - 1;
	static final int MM = (1<<WW) - 1;
	*/
}

/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


