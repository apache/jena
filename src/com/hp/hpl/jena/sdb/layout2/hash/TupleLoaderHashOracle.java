package com.hp.hpl.jena.sdb.layout2.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderHashOracle extends TupleLoaderHashBase {
	
	private static Log log = LogFactory.getLog(TupleLoaderHashOracle.class);
	
	public TupleLoaderHashOracle(SDBConnection connection, TableDesc tableDesc,
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
}
