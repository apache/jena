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
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.shared.JenaException;

/**
	@author hedgehog
*/

	public class VarDesc {
		
		Node_Variable	var;        // variable
		boolean			isArgVar;	// true if variable is bound to an argument
		boolean			isBoundToCol; // true once variable is bound to a column
		int				resIx;     	// index of var in result list (isArgVar==false)
		int				mapIx;     	// index into arg mapping (isArgVar==true)
		int				alias;		// alias of table that binds var
		char			column;		// column id of table that bind var 
		
		public VarDesc ( Node v, int mapix, int resix ) {
			var = (Node_Variable) v;
			isArgVar = true;
			mapIx = mapix;
			resIx = resix;
			isBoundToCol = false;
		}
		
		public VarDesc ( Node v, int resix ) {
			var = (Node_Variable) v;
			isArgVar = false;
			mapIx = -1;
			resIx = resix;
			isBoundToCol = false;
		}

		public void bindToVarMap ( Mapping varMap ) {
			if ( isArgVar )
				throw new JenaException("Variable bound twice to argument");
			mapIx = varMap.newIndex(var);
			return;
		}

	
	public void bindToCol ( int tblAlias, char colId ) {
		if ( isBoundToCol )
			throw new JenaException("Variable bound twice to column");
		isBoundToCol = true;
		alias = tblAlias;
		column = colId;
		return;
	}
	
	public boolean isBoundToCol() { return isBoundToCol; }	
	public boolean isArgVar() { return isArgVar; }	
}
