/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: VarDesc.java,v 1.2 2005-02-21 12:03:13 andy_seaborne Exp $
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

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
