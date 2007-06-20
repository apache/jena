package com.hp.hpl.jena.sdb.layout2.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderIndexDerby extends TupleLoaderIndexBase {
	
	private static Log log = LogFactory.getLog(TupleLoaderIndexDerby.class);
	
	public TupleLoaderIndexDerby(SDBConnection connection, TableDesc tableDesc,
			int chunkSize) {
		super(connection, tableDesc, chunkSize);
	}
	
	// A compromise. Derby's temporary tables are limited, but cleaning up afterwards is worse
	public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "VARCHAR (32672)", "VARCHAR(1024)", "VARCHAR(1024)", "INT"};
	}
	
	public String getTupleColType() {
		return "BIGINT";
	}
	
	public String[] getCreateTempTable() {
		return new String[] { "DECLARE GLOBAL TEMPORARY TABLE" , "ON COMMIT DELETE ROWS NOT LOGGED" };
	}
	
	// We have to qualify the temporary table names
	@Override
	public String getNodeLoader() {
		return "SESSION." + super.getNodeLoader();
	}
	
	@Override
	public String getTupleLoader() {
		return "SESSION." + super.getTupleLoader();
	}
	
	@Override
	public String getClearTempNodes() {
		return null;
	}
	
	@Override
	public String getClearTempTuples() {
		return null;
	}
}
