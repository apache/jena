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

import com.hp.hpl.jena.sdb.layout2.TupleLoaderBase;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class TupleLoaderHashBase extends TupleLoaderBase {

	public TupleLoaderHashBase(SDBConnection connection,
			TableDesc tableDesc, int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	@Override
    public String getLoadTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(this.getTableName()).append(" \nSELECT DISTINCT ");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append(getTupleLoader()).append(".").append("t").append(i);
		}
		stmt.append("\nFROM ").append(getTupleLoader()).append(" LEFT JOIN ").append(this.getTableName()).append(" ON \n (");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" AND ");
			stmt.append("t").append(i);
			stmt.append("=").append(this.getTableName()).append(".").append(this.getTableDesc().getColNames().get(i));
		}
		stmt.append(")\nWHERE\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" OR\n");
			stmt.append(this.getTableName()).append(".").append(this.getTableDesc().getColNames().get(i)).append(" IS NULL");
		}
		
		return stmt.toString();
	}
	
	@Override
    public String getDeleteTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("DELETE FROM ").append(this.getTableName()).append(" \nWHERE\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" AND\n");
			stmt.append(this.getTableDesc().getColNames().get(i)).append(" = ?");
		}
		
		return stmt.toString();
	}
	
	@Override
    public String getDeleteAllTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("DELETE FROM ").append(this.getTableName());
		if (this.getTableWidth() != 3) { // not a triple table, delete based on first column
			stmt.append(" \nWHERE\n");
			stmt.append(getTableDesc().getColNames().get(0));
			stmt.append(" = ? ");
		}
		
		return stmt.toString();
	}
}
