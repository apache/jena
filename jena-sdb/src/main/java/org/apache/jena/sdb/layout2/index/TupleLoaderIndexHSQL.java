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

public class TupleLoaderIndexHSQL extends TupleLoaderIndexBase {

	public TupleLoaderIndexHSQL(SDBConnection connection, TableDesc tableDesc,
			int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	@Override
    public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "VARCHAR", "VARCHAR(10)", "VARCHAR("+ TableDescNodes.DatatypeUriLength+ ")", "INT"};
	}
	
	@Override
    public String getTupleColType() {
		return "BIGINT";
	}
	
	@Override
    public String[] getCreateTempTable() {
		return new String[] { "CREATE TEMPORARY TABLE" , "ON COMMIT DELETE ROWS" };
	}
	
	@Override
	public boolean clearsOnCommit() { return true; }
}
