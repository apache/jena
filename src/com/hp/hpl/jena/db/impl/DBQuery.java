/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: DBQuery.java,v 1.1 2003-08-11 02:41:52 wkw Exp $
*/

package com.hp.hpl.jena.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.shared.JenaException;

/**
	@author hedgehog
*/

public class DBQuery 
	{
	int argCnt;         // number of arguments to query
	String argType;     // list of argument types
	List argIndex;		// index of argument in input
	int varCnt;         // number of variables in query
	int aliasCnt;        // number of tables aliases (scans) in from clause
	String stmt;        // query string
	Var[] binding;  // list of DBQueryVar
	int graphId;        // id of graph to query
	String table;   // name of table to query
	IPSet pset;		// pset to be queried
	IRDBDriver driver;  // driver for store
	boolean queryFullReifStmt; // if true, ignore partially reified statements
	DriverRDB.GenSQLAnd ga;

	boolean isMultiModel;   // true if graph is multi-model
	boolean isSingleValued; // true if property table is single-valued
	boolean isCacheable;    // true if it is safe to cache compiled query
	boolean isReifier;      // true if query is over a reifier specialized graph

	
	public DBQuery ( SpecializedGraph sg, List resVar, Mapping mapVar,
		boolean qryFullReif ) {
		pset = sg.getPSet();
		argCnt = 0;
		argType = "";
		argIndex = new ArrayList();	
		aliasCnt = 0;
		stmt = "";
		graphId = sg.getGraphId();
		table = pset.getTblName();
		isMultiModel = true;  // for now
		isSingleValued = false;  // for now
		isCacheable = true;
		isReifier = sg instanceof SpecializedGraphReifier;
		driver = pset.driver();
		ga = new IRDBDriver.GenSQLAnd();
		queryFullReifStmt = qryFullReif;
		// add result variables to mapping
		binding = new Var[resVar.size()];
		for ( varCnt=0; varCnt<resVar.size(); varCnt++ ) {
			Node_Variable v = (Node_Variable) resVar.get(varCnt);
			if ( mapVar.hasBound(v) )
				throw new JenaException("Free variable is bound");
			int mix = mapVar.newIndex(v);
			binding[varCnt] = new Var(v,varCnt,mix);
		}

	}
	
	public class Var {
		Node_Variable	var;        // variable
		boolean			isBound;    // true if variable is bound to column
		int				map_ix;     // index into varmap
		int				bind_ix;	// index into binding
		int				alias;      // table alias
		char			column;     // column id
		
		public Var ( Node v, int bix, int mix ) {
			var = (Node_Variable) v; isBound = false; bind_ix = bix; map_ix = mix;
		}
	}
	
	public boolean isBound ( int i ) {
		return binding[i].isBound;
	}

	public Var getBinding ( int i ) {
		return binding[i];
	}
	
	public void bindVar ( Var b, int alias, char c ) {
		if ( b.isBound )
			throw new JenaException("Variable bound twice");
		b.isBound = true;
		b.alias = alias;
		b.column = c;
		return;
	}
	
	public void newAlias() {
		aliasCnt++;
	}
	
}		

/*
    (c) Copyright Hewlett-Packard Company 2002
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
