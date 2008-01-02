/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: ReificationCacheMap.java,v 1.3 2008-01-02 12:08:23 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.JenaException;

/**
    Reification cache map, extracted from SpecialisedGraphReifier_RDB.
*/
public class ReificationCacheMap {
	/**
    	Comment for <code>reifier_RDB</code>
    */
    private final SpecializedGraphReifier_RDB reifier_RDB;
    protected int cacheSize = 1;
	protected ReificationCache[] cache;
	protected boolean[] inUse;

	ReificationCacheMap( SpecializedGraphReifier_RDB reifier_RDB, int size) {
		this.reifier_RDB = reifier_RDB;
		inUse = new boolean[size];
		cache = new ReificationCache[size];
		for (int i = 0; i < size; i++) inUse[i] = false;
	}

	ReificationCache lookup(Node stmtURI) {
		for (int i = 0; i < cache.length; i++) {
			if (inUse[i] && (cache[i].getStmtURI().equals( stmtURI )))
				return cache[i];
		}
		return null;
	}

	public void flushAll() {
		for (int i = 0; i < cache.length; i++)
			inUse[i] = false;
	}

	public void flush(ReificationCache entry) {
		flushAll(); // optimize later
	}

	public ReificationCache load(Node stmtURI) {
		ReificationCache entry = lookup(stmtURI);
		if (entry != null)
			return entry;
		return load(stmtURI, null, null, null);
	}
		

	public ReificationCache load(Node stmtURI, Triple s, ReificationStatementMask sm, ReificationStatementMask dm ) {
		flushAll();
		ReificationStatementMask m = new ReificationStatementMask();
		Triple t;
		boolean  hasSubj, hasPred, hasObj, hasType;
		boolean  checkSame = sm != null;
		int cnt = 0;
		ResultSetReifIterator it = this.reifier_RDB.m_reif.findReifStmt(stmtURI,false,this.reifier_RDB.my_GID, false);
		while (it.hasNext()) {
			cnt++;
			Triple db = (Triple) it.next();				
			ReificationStatementMask n = new ReificationStatementMask();
			hasSubj = !db.getSubject().equals(Node.NULL);
			if ( hasSubj && checkSame )
				if ( db.getSubject().equals(s.getSubject()) )
					sm.setHasSubj();
				else
					dm.setHasSubj();
			hasPred = !db.getPredicate().equals(Node.NULL);
			if ( hasPred && checkSame )
				if ( db.getPredicate().equals(s.getPredicate()) )
					sm.setHasPred();
				else
					dm.setHasPred();
			hasObj = !db.getObject().equals(Node.NULL);
			if ( hasObj && checkSame )
				if ( db.getObject().equals(s.getObject()) )
					sm.setHasObj();
				else
					dm.setHasObj();
				
			hasType = it.getHasType();

			n.setMask( hasSubj, hasPred, hasObj, hasType );
			if ( n.hasNada() ) throw new JenaException("Fragment has no data");
			m.setMerge(n);
		}
		if ( cnt == 0 )
			return null; // no fragments for subject
			
		if (m.hasSPOT() && (cnt == 1))
			m.setIsStmt();

		inUse[0] = true;
		cache[0] = new ReificationCache(stmtURI, m, cnt);
		return cache[0];
	}

}

/*
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development
 * Company, LP All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */