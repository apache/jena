/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/**
 * @author jjc
 *
 */
class CBlank extends OneTwoImpl implements Blank {
	public void addObjectTriple(Triple t) {
		check(2, t);
	}
	final static public Implementation factory = new Implementation() {
	public EnhNode wrap(Node n, EnhGraph eg) {
					return new CBlank(n, eg);
	}
    public boolean canWrap( Node n, EnhGraph eg )
        { return true; }
	};
	
	/**
	 * Constructor for CBlank.
	 * @param n
	 * @param eg
	 */
	public CBlank(Node n, EnhGraph eg) {
		super(n, eg);
		if ( getCategories() == -1 )
		      setCategories(Grammar.blank,false);
	}
	
	int getCyclicState() {
		return getIntAttribute(Vocab.cyclicState, Undefined);
	}
	static final int Checking = 1;
	static final int Undefined = 0;
	static final int IsCyclic = 2;
	static final int NonCyclic = 3;
    void setCyclicState(int st) {
    	setIntAttribute(Vocab.cyclicState,st);
    }
}


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
