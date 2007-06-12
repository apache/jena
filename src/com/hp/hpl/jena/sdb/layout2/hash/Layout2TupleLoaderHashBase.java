package com.hp.hpl.jena.sdb.layout2.hash;

import com.hp.hpl.jena.sdb.layout2.Layout2TupleLoaderBase;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class Layout2TupleLoaderHashBase extends Layout2TupleLoaderBase {

	public Layout2TupleLoaderHashBase(SDBConnection connection,
			TableDesc tableDesc, int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
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

}
