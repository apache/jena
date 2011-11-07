/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
			Triple db = it.next();				
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
