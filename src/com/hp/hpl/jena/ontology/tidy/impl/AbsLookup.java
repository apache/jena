/*
   (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 [See end of file]
  $Id: AbsLookup.java,v 1.3 2005-02-21 12:08:15 andy_seaborne Exp $
*/
package com.hp.hpl.jena.ontology.tidy.impl;

/**
 * Impelements the methods for accessing the bits
 * within the ALL-ACTIONS part of a lookup result.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
abstract public class AbsLookup implements Lookup,Constants {
	public int action(int k) {
		return  allActions( k) & ~(DL | ObjectAction|SubjectAction|RemoveTriple);
	}

	public boolean tripleForObject(int k) {
		return (allActions( k) & ObjectAction) == ObjectAction;
	}
	public boolean tripleForSubject(int k) {
		return (allActions( k) & SubjectAction) == SubjectAction;
	}
	public boolean removeTriple(int k) {
		return (allActions( k) & RemoveTriple) == RemoveTriple;
	}	
   public boolean dl(int k) {
	   return (allActions( k) & DL) == DL;
   }
}

/*
   (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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