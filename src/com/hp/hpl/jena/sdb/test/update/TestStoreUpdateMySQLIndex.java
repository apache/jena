package com.hp.hpl.jena.sdb.test.update;

import junit.framework.JUnit4TestAdapter;

import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestStoreUpdateMySQLIndex extends TestStoreUpdateBase {
	
	public static junit.framework.Test suite() { 
	    return new JUnit4TestAdapter(TestStoreUpdateMySQLIndex.class); 
	}
	
	@Override
	Store getStore() {
		return StoreCreator.getIndexMySQL();
	}

}
