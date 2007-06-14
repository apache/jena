package com.hp.hpl.jena.sdb.layout2.index;

import java.sql.SQLException;

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
	
	public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "CLOB", "LONG VARCHAR", "LONG VARCHAR", "INT"};
	}
	
	public String getTupleColType() {
		return "BIGINT";
	}
	
	public String[] getCreateTempTable() {
		return new String[] { "CREATE TABLE" , "" };
	}
	
	@Override
    public void close() {
		super.close();
    	try {
			connection().exec("DROP TABLE " + getNodeLoader());
			connection().exec("DROP TABLE " + getTupleLoader());
		} catch (SQLException e) {
			log.error("Error removing loader tables", e);
		}
    }
}
