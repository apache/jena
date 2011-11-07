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
 * 
 * @author csayers
* @version $Revision: 1.1 $
 */
public class DBPropPSet extends DBProp {

	/**
	 * @since Jena 2.0
	 */

	public static Node_URI pSetName = (Node_URI)DB.pSetName.asNode();
	public static Node_URI pSetType = (Node_URI)DB.pSetType.asNode();
	public static Node_URI pSetTable = (Node_URI)DB.pSetTable.asNode();

	
	public DBPropPSet( SpecializedGraph g, String type, String tableName) {
		super( g);
		putPropString(pSetName, DBProp.generateUniqueID());
		putPropString(pSetType, type);
		putPropString(pSetTable, tableName);
	}
	
	public DBPropPSet( SpecializedGraph g, Node n) {
		super(g,n);
	}	
	
	public String getName() { return getPropString( pSetName); }
	public String getType() { return getPropString( pSetType); }
	public String getTable() { return getPropString( pSetTable); }
}
