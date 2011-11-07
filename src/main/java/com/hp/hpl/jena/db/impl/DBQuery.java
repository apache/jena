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

import java.util.ArrayList;
import java.util.List;

/**
	@author hedgehog
*/

public class DBQuery 
	{
	int argCnt;         // number of arguments to query
	String argType;     // list of argument types
	List<Integer> argIndex;		// index of argument in input
	int varCnt;         // number of variables in query
	int aliasCnt;        // number of tables aliases (scans) in from clause
	String stmt;        // query string
	VarDesc[] vars;  // list of VarIndex, variables referenced in this query
	int[] resList;		// indexes of result columns in mapping
	int graphId;        // id of graph to query
	String table;   // name of table to query
	IPSet pset;		// pset to be queried
	IRDBDriver driver;  // driver for store
	boolean qryOnlyStmt; // if true, ignore reified statements
	boolean qryOnlyReif; // if true, ignore asserted statements
	boolean qryFullReif; // if true, ignore partially reified statements
	DriverRDB.GenSQLAnd sqlAnd;

	boolean isMultiModel;   // true if graph is multi-model
	boolean isSingleValued; // true if property table is single-valued
	boolean isCacheable;    // true if it is safe to cache compiled query
	boolean isReifier;      // true if query is over a reifier specialized graph
	boolean isEmpty;		// true if compiler determines query has no results

	
	public DBQuery ( SpecializedGraph sg, List<VarDesc> varList,
		boolean queryOnlyStmt,  boolean queryOnlyReif, boolean queryFullReif ) {

		argCnt = 0;
		argType = "";
		argIndex = new ArrayList<Integer>();	
		aliasCnt = 0;
		stmt = "";
		isMultiModel = true;  // for now
		isSingleValued = false;  // for now
		isCacheable = true;
		if ( sg != null ) {
			pset = sg.getPSet();			
			isReifier = sg instanceof SpecializedGraphReifier;			
			isEmpty = false;
			graphId = sg.getGraphId();
			table = pset.getTblName();
			driver = pset.driver();
		} else {
			pset = null;
			isReifier = false;
			isEmpty = true;
			driver = null;
		}
		sqlAnd = new IRDBDriver.GenSQLAnd();
		qryOnlyStmt = queryOnlyStmt;
		qryOnlyReif = queryOnlyReif;
		qryFullReif = queryFullReif;
		// array of variable bound by query
		vars = new VarDesc[varList.size()];
		for ( varCnt=0; varCnt<varList.size(); varCnt++ ) {
			vars[varCnt] = varList.get(varCnt);
		}

	}
	
	public VarDesc getBinding ( int i ) {
		return vars[i];
	}

	public VarDesc findBinding ( String v ) {
		int i;
		for ( i=0; i<vars.length; i++ ) {
			if ( vars[i].var.getName().equals(v) )
				return vars[i];
		}
		return null;
	}
		
	public void newAlias() {
		aliasCnt++;
	}
	
}
