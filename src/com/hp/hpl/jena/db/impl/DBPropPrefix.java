/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DB;

/**
 *
 * A wrapper to assist in getting and setting DB information from 
 * a persistent store.
 * 
 * This is written in the style of enhanced nodes - no state is
 * stored in the DBStoreDesc, instead all state is in the
 * underlying graph and this is just provided as a convenience.
 * 
 * (We don't use enhanced nodes because, since we control everything
 * in the persistent store system description, we can avoid any
 * need to handle polymorhphism).
 * 
 * @since Jena 2.0
 * 
 * @author csayers
 * @version $Revision: 1.6 $
 */
public class DBPropPrefix extends DBProp {

	public static Node_URI prefixValue = (Node_URI)DB.prefixValue.getNode();
	public static Node_URI prefixURI = (Node_URI)DB.prefixURI.getNode();
	
	public DBPropPrefix( SpecializedGraph g, String value, String uri) {
		super( g, Node.createAnon());
		putPropString(prefixValue, value);
		putPropString(prefixURI, uri);
	}
	
	public DBPropPrefix( SpecializedGraph g, Node n) {
		super(g,n);
	}	
		
	public String getValue() { return getPropString( prefixValue); }
	public String getURI() { return getPropString( prefixURI); }
	

	public ExtendedIterator listTriples() {
		return DBProp.listTriples(graph, self);
	}	
    public String toString()
        { return "<[" + getValue() + "=" + getURI() + "]>" ; }
}

/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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