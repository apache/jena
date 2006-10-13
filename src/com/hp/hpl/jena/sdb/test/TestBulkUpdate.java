package com.hp.hpl.jena.sdb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.ModelSDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesPGSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

@RunWith(Parameterized.class)
public class TestBulkUpdate {
	
	protected ModelSDB model;
	
	@Parameters public static Collection models()
	{
		Collection<Object[]> models = new ArrayList<Object[]>();
		
		models.add(new Object[] { getMySQL() } );
		models.add(new Object[] { getHSQL() });
		models.add(new Object[] { getPgSQL() });
		
		return models;
	}
	
	public static Model getMySQL()
	{
		JDBC.loadDriverMySQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesMySQL store = new StoreTriplesNodesMySQL(sdb);
		
		store.getTableFormatter().format();
		
		return SDBFactory.connectModel(store);
	}
	
	public static Model getHSQL()
	{
		JDBC.loadDriverHSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:hsqldb:mem:aname", "sa", "");
		
		StoreTriplesNodesHSQL store = new StoreTriplesNodesHSQL(sdb);
		
		store.getTableFormatter().format();
		
		return SDBFactory.connectModel(store);
	}
	
	public static Model getPgSQL()
	{
		JDBC.loadDriverPGSQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:postgresql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesPGSQL store = new StoreTriplesNodesPGSQL(sdb);
		
		return SDBFactory.connectModel(store);
	}
	
	public TestBulkUpdate(ModelSDB model)
	{
		this.model = model;
	}
	
	@Test public void loadOne()
	{
		model.removeAll(RDF.type, RDF.type, null);
		long size = model.size();
		model.add(RDF.type, RDF.type, "FOO");
		assertTrue("It's in there", model.contains(RDF.type, RDF.type, "FOO"));
		assertEquals("Added one triple", size + 1, model.size());
		model.remove(RDF.type, RDF.type, model.createLiteral("FOO"));
		assertEquals("Back to the start", size, model.size());
	}
	
	@Test public void loadFile()
	{
		Model toLoadAndRemove = FileManager.get().loadModel("testing/Data/data.ttl");
		
		long size = model.size();
		
		model.add(toLoadAndRemove);
		
		assertEquals("Added all", size + 13, model.size());
		
		model.add(RDF.type, RDF.type, RDF.type);
		
		assertTrue("Model contains <type,type,type>", model.contains(RDF.type, RDF.type, RDF.type));
		
		assertEquals("And another one", size + 14, model.size());
		
		model.remove(toLoadAndRemove);
		
		assertTrue("Model contains <type,type,type>", model.contains(RDF.type, RDF.type, RDF.type));
		
		assertEquals("Removed file", size + 1, model.size());
		
		model.remove(RDF.type, RDF.type, RDF.type);
		
		assertEquals("All removed", size, model.size());
	}
	
	@Test public void remove()
	{
		long size = model.size();
		
		model.add(RDF.nil, RDF.type, "ONE");
		model.add(RDF.nil, RDF.type, "TWO");
		model.add(RDF.nil, RDF.type, RDF.Alt);
		model.add(RDF.nil, RDF.type, RDF.first);
		
		assertEquals("All added ok", size + 4, model.size());
		
		model.removeAll(RDF.nil, RDF.type, null);
		
		assertEquals("Wild card removed all", size, model.size());
	}
	
	@Before public void format()
	{
		model.removeAll();
	}
	
}
