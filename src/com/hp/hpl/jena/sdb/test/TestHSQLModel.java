package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.AbstractTestModel;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class TestHSQLModel extends AbstractTestModel {

	public TestHSQLModel(String arg0) {
		super(arg0);
	}

	@Override
	public Model getModel() {
		JDBC.loadDriverHSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:hsqldb:mem:aname", "sa", "");
		
		StoreTriplesNodesHSQL store = new StoreTriplesNodesHSQL(sdb);
		
		store.getTableFormatter().format();
		
		return SDBFactory.connectModel(store);
	}

}
