package dev.pldms;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesPGSQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class Scratch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Model model = getMySQL();
		
		System.out.println("Model size: " + model.size());
		
		Model toLoadAndRemove = FileManager.get().loadModel("testing/Data/data.ttl");
		
		model.add(toLoadAndRemove);
		
		model.add(RDF.type, RDF.type, RDF.type);
		
		System.out.println("Model size: " + model.size());
		
		model.remove(toLoadAndRemove);
		
		System.out.println("Model size: " + model.size());
		
		model.remove(RDF.type, RDF.type, RDF.type);
		
		System.out.println("Model size: " + model.size());
		
		model.read("file:testing/Data/data.ttl", "N3");
		
		model.removeAll(null, null, null);
		
		System.out.println("Model size: " + model.size());
		
		model.close();
		
	}
	
	public static Model getMySQL()
	{
		JDBC.loadDriverMySQL();
		
		SDBConnection sdb = SDBFactory.createConnection("jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesMySQL store = new StoreTriplesNodesMySQL(sdb);
		
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
}
