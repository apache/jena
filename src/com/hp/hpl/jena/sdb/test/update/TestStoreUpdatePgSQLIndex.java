package com.hp.hpl.jena.sdb.test.update;

import junit.framework.JUnit4TestAdapter;

import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestStoreUpdatePgSQLIndex extends TestStoreUpdateBase {

	public static junit.framework.Test suite() { 
	    return new JUnit4TestAdapter(TestStoreUpdatePgSQLIndex.class); 
	}
	
	@Override
	Store getStore() {
		return StoreCreator.getIndexPgSQL();
	}

}
