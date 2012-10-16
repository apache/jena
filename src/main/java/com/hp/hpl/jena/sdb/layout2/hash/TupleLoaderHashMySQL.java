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

package com.hp.hpl.jena.sdb.layout2.hash;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderHashMySQL extends TupleLoaderHashBase {

	public TupleLoaderHashMySQL(SDBConnection connection, TableDesc tableDesc,
			int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	@Override
    public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "LONGTEXT BINARY CHARACTER SET utf8", "VARCHAR(10) BINARY CHARACTER SET utf8",
				"VARCHAR("+ TableDescNodes.DatatypeUriLength+ ") BINARY CHARACTER SET utf8", "INT"};
	}
	
	@Override
    public String getTupleColType() {
		return "BIGINT";
	}
	
	@Override
    public String[] getCreateTempTable() {
		return new String[] { "CREATE TEMPORARY TABLE" , "ENGINE=MYISAM" };
	}
	
	@Override
	public String getLoadNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT IGNORE INTO Nodes (hash, lex, lang, datatype, type) \nSELECT ");
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("n").append(i);
		}
		stmt.append("\nFROM ").append(getNodeLoader()); 
		return stmt.toString();
	}
	
	@Override
	public String getLoadTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT IGNORE INTO ").append(this.getTableName()).append(" \nSELECT ");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("t").append(i);
		}
		stmt.append("\nFROM ").append(getTupleLoader());
		
		return stmt.toString();
	}
}
