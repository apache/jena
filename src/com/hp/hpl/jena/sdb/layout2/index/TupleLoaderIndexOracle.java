/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderIndexOracle extends TupleLoaderIndexBase {
	
	private static Log log = LogFactory.getLog(TupleLoaderIndexOracle.class);
	
	public TupleLoaderIndexOracle(SDBConnection connection, TableDesc tableDesc,
			int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	public String[] getNodeColTypes() {
		return new String[] {"NUMBER(20)", "NCLOB", "NVARCHAR2(10)", "NVARCHAR2("+TableDescNodes.DatatypeUriLength+")", "INT"};
	}
	
	public String getTupleColType() {
		return "NUMBER(20)";
	}
	
	public String[] getCreateTempTable() {
		return new String[] { "CREATE GLOBAL TEMPORARY TABLE" , "ON COMMIT DELETE ROWS" };
	}
	
	@Override
	public String getClearTempNodes() {
		return null;
	}
	
	@Override
	public String getClearTempTuples() {
		return null;
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
			stmt.append("JOIN Nodes NI").append(i).append(" ON ("); // No 'AS' in oracle
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
	public String getLoadNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO Nodes \nSELECT nodeid.nextval , "); // Autoindex thingy
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append(getNodeLoader()).append(".").append("n").append(i);
		}
		stmt.append("\nFROM ").append(getNodeLoader()).append(" LEFT JOIN Nodes ON (");
		stmt.append(getNodeLoader()).append(".n0=Nodes.hash) \nWHERE Nodes.hash IS NULL"); 
		return stmt.toString();
	}
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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