package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.AbstractTestModel;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class TestMySQLModel extends AbstractTestModel {

	public TestMySQLModel(String arg0) {
		super(arg0);
	}

	@Override
	public Model getModel() {
		JDBC.loadDriverMySQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesMySQL store = new StoreTriplesNodesMySQL(sdb);
		
		store.getTableFormatter().format();
		
		return SDBFactory.connectModel(store);
	}

}
