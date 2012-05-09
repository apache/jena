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

package com.hp.hpl.jena.sdb.layout2.index;

import com.hp.hpl.jena.sdb.layout2.TupleLoaderBase;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class TupleLoaderIndexBase extends TupleLoaderBase {

	public TupleLoaderIndexBase(SDBConnection connection,
			TableDesc tableDesc, int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	@Override
    public String getLoadTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(this.getTableName()).append(" \nSELECT DISTINCT ");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("NI").append(i).append(".id");
		}
		stmt.append("\nFROM ").append(getTupleLoader()).append("\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			stmt.append("JOIN Nodes AS NI").append(i).append(" ON (");
			stmt.append(getTupleLoader()).append(".t").append(i).append("=NI").append(i).append(".hash)\n");
		}
		stmt.append("LEFT JOIN ").append(getTableName()).append(" ON (");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" AND ");
			stmt.append("NI").append(i).append(".id");
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
			stmt.append(getTableDesc().getColNames().get(i)).append(" IN (SELECT id FROM Nodes WHERE hash = ?) ");
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
			stmt.append(" IN (SELECT id FROM Nodes WHERE hash = ?) ");
		}
		
		return stmt.toString();
	}

}
