package dev.pldms;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class Scratch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JDBC.loadDriverMySQL();

		SDBConnection sdb = SDBFactory.createConnection("jdbc:mysql://localhost/sdb_test", "jena", "swara");
		
		StoreTriplesNodesMySQL store = new StoreTriplesNodesMySQL(sdb);
		
		Model model = SDBFactory.connectModel(store);
		
		System.out.println("Model size: " + model.size());
		
		Model toLoadAndRemove = FileManager.get().loadModel("testing/manifest-sdb.ttl");
		
		model.add(toLoadAndRemove);
		
		model.add(RDF.type, RDF.type, RDF.type);
		
		System.out.println("Model size: " + model.size());
		
		model.remove(toLoadAndRemove);
		
		System.out.println("Model size: " + model.size());
		
		model.close();
	}

}
