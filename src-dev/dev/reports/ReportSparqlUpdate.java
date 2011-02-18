package dev.reports;

import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.sdb.SDBFactory ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.sql.JDBC ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

//@Ignore
public class ReportSparqlUpdate {
	
//	@Test
//	public void testForSDBBug() throws Exception {
    public static void main(String...argv) throws Exception {    
		String driver = "org.postgresql.Driver";
		String url = "jdbc:postgresql:test";
		String username = "user";
		String password = "password";
		Integer maxConnections = 8;
		String sdbLayout = "layout2/hash";
		String dbType = "postgresql";
		
		JDBC.loadDriverPGSQL() ;
		
//		JDBC.loadDriverHSQL() ;
//		url = "jdbc:hsqldb:mem" ;
//		username = "sa" ;
//		password = "" ;
//		dbType = "hsqldb" ;

		StoreDesc storeDesc = new StoreDesc(sdbLayout, dbType);
		SDBConnection connection = SDBFactory.createConnection(url, username, password) ;
		Store store = SDBFactory.connectStore(connection, storeDesc);
		Dataset dataset = SDBFactory.connectDataset(store);
		
		//if ( dbType.equals("hsqldb"))
		    store.getTableFormatter().create() ;
		
		

		    if ( true )
		    {
		        Dataset ds = SDBFactory.connectDataset(store) ;
		        GraphStore graphStore = GraphStoreFactory.create(ds) ; //using the dataset
		        final String SparqlUpdate= 
		            StrUtils.strjoinNL(
		                               "PREFIX OntologyUserInterest: <http://www.semanticweb.org/ontologies/2010/11/OntologyUserInterest.owl#>",
		                               "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
		                               "INSERT DATA { <http://example/s> <http://example/p> <http://example/o> }",   
		            "") ;

		        UpdateRequest request = UpdateFactory.create(SparqlUpdate) ; 
		        System.out.println(request) ;

		        try {
		            connection.getTransactionHandler().begin() ;
		            // And perform the operations.
		            UpdateAction.execute(request, graphStore) ;
		            System.out.println("Sparql update success!");
		            connection.getTransactionHandler().commit() ;

		        }
		        catch (Exception e){
		            System.out.println("Sparql Update failed!");
		        }
		    }

        if ( false )
		{	          
		    String queryString = "SELECT * WHERE { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } } LIMIT 5";
		    Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		    QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
		    //qexec.getContext().set(SDB.unionDefaultGraph, true);
		    QueryExecUtils.executeQuery(query, qexec) ;
		}        

        

        
        System.out.println("DONE") ;

	}
//	
//	private static BasicDataSource configureDataSource(String driver,
//			String url, String username, String password, Integer maxConnections) {
//		BasicDataSource dataSource = new BasicDataSource();
//		dataSource.setDriverClassName(driver);
//		dataSource.setUrl(url);
//		dataSource.setUsername(username);
//		dataSource.setValidationQuery("SELECT 1 AS test");
//		dataSource.setTestOnBorrow(true);
//		dataSource.setTestOnReturn(true);
//		dataSource.setMaxActive(maxConnections);
//		dataSource.setMaxIdle(maxConnections);
//		if (password != null) {
//			dataSource.setPassword(password);
//		}
//		return dataSource;
//	}

}
