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

package org.apache.jena.sdb.layout2.index;

import org.apache.jena.sdb.layout2.TableDescNodes ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.store.TableDesc ;

public class TupleLoaderIndexMySQL extends TupleLoaderIndexBase {

	public TupleLoaderIndexMySQL(SDBConnection connection, TableDesc tableDesc,
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
			stmt.append("NI").append(i).append(".id");
		}
		stmt.append("\nFROM ").append(getTupleLoader()).append(" ");
		for (int i = 0; i < this.getTableWidth(); i++) {
			stmt.append("JOIN Nodes AS NI").append(i).append(" ON (");
			stmt.append(getTupleLoader()).append(".t").append(i).append("=NI").append(i).append(".hash)\n");
		}
		
		return stmt.toString();
	}

        // MYSQL bug, doesn't use indexes for IN. = works
        @Override
        public String getDeleteTuples() {
            StringBuilder stmt = new StringBuilder();

            stmt.append("DELETE FROM ").append(this.getTableName()).append(" \nWHERE\n");
            for (int i = 0; i < this.getTableWidth(); i++) {
                if (i != 0) {
                    stmt.append(" AND\n");
                }
                stmt.append(getTableDesc().getColNames().get(i)).append(" = (SELECT id FROM Nodes WHERE hash = ?) ");
            }

            return stmt.toString();
	}

        // See above
        @Override
	public String getDeleteAllTuples() {
            StringBuilder stmt = new StringBuilder();

            stmt.append("DELETE FROM ").append(this.getTableName());
            if (this.getTableWidth() != 3) { // not a triple table, delete based on first column
                stmt.append(" \nWHERE\n");
                stmt.append(getTableDesc().getColNames().get(0));
                stmt.append(" = (SELECT id FROM Nodes WHERE hash = ?) ");
            }

            return stmt.toString();
	}
}
