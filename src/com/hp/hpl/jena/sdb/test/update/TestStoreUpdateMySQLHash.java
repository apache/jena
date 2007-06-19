package com.hp.hpl.jena.sdb.test.update;

import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.test.StoreCreator;

public class TestStoreUpdateMySQLHash extends TestStoreUpdateBase {

	@Override
	Store getStore() {
		return StoreCreator.getHashMySQL();
	}

}
