package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesPGSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class TestPgSQLGraph extends AbstractTestGraph {

	public TestPgSQLGraph(String arg0) {
		super(arg0);
	}
	
	@Override
	public Graph getGraph()
	{
		JDBC.loadDriverPGSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:postgresql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesPGSQL store = new StoreTriplesNodesPGSQL(sdb);
		
		return SDBFactory.connectModel(store).asGraph();
	}
}
