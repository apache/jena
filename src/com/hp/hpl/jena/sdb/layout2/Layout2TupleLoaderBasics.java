package com.hp.hpl.jena.sdb.layout2;

public interface Layout2TupleLoaderBasics {
	public String[] getNodeColTypes();
	public String getTupleColType();
	public String[] getCreateTempTable();
	public String getLoadTuples();
}
