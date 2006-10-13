package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.AbstractTestModel;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesPGSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class TestPgSQLModel extends AbstractTestModel {

	public TestPgSQLModel(String arg0) {
		super(arg0);
	}

	@Override
	public Model getModel() {
		JDBC.loadDriverPGSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:postgresql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesPGSQL store = new StoreTriplesNodesPGSQL(sdb);
		
		return SDBFactory.connectModel(store);
	}

}
