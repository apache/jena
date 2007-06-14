package com.hp.hpl.jena.sdb.layout2;

public interface TupleLoaderBasics {
	public String[] getNodeColTypes();
	public String getTupleColType();
	public String[] getCreateTempTable();
	public String getLoadTuples();
	public String getDeleteTuples();
	public String getClearTempNodes();
	public String getClearTempTuples();
}
