package com.hp.hpl.jena.sdb.layout2.hash;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderHashMySQL extends Layout2TupleLoaderHashBase {

	public TupleLoaderHashMySQL(SDBConnection connection, TableDesc tableDesc,
			int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "TEXT BINARY CHARACTER SET utf8", "VARCHAR(10) BINARY CHARACTER SET utf8",
				"VARCHAR("+ TableDescNodes.DatatypeUriLength+ ") BINARY CHARACTER SET utf8", "INT"};
	}
	
	public String getTupleColType() {
		return "BIGINT";
	}
	
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
