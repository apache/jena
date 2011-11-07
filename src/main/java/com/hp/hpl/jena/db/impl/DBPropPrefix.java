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
 * @version $Revision: 1.1 $
 */
public class DBPropPrefix extends DBProp {

	public static Node_URI prefixValue = (Node_URI)DB.prefixValue.asNode();
	public static Node_URI prefixURI = (Node_URI)DB.prefixURI.asNode();
	
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
	

	public ExtendedIterator<Triple> listTriples() {
		return DBProp.listTriples(graph, self);
	}	
    @Override
    public String toString()
        { return "<[" + getValue() + "=" + getURI() + "]>" ; }
}
