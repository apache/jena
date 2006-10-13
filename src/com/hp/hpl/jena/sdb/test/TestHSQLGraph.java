package com.hp.hpl.jena.sdb.test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.AbstractTestGraph;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

public class TestHSQLGraph extends AbstractTestGraph {

	public TestHSQLGraph(String arg0) {
		super(arg0);
	}
	
	@Override
	public Graph getGraph()
	{
		JDBC.loadDriverHSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:hsqldb:mem:aname", "sa", "");
		
		StoreTriplesNodesHSQL store = new StoreTriplesNodesHSQL(sdb);
		
		store.getTableFormatter().format();
		
		return SDBFactory.connectModel(store).asGraph();
	}
}
