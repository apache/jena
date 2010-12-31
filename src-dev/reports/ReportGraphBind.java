package reports;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ReportGraphBind {

    /* execute a construct query on a model, and return the resulting model */
    private static Model runQuery(String queryStr, Model model){
        /* create and execute query */
        Query query = QueryFactory.create(queryStr);
        DataSource dsource = DatasetFactory.create();
        dsource.addNamedModel("http://input/", model);
        QueryExecution qexec = QueryExecutionFactory.create(query, dsource);
        Model resultModel = qexec.execConstruct() ;
        qexec.close();
        return resultModel;
    }

    /* execute a construct query on a model, and return the resulting model */
    private static void runQuery2(String queryStr, Model model){
        /* create and execute query */

        Query query = QueryFactory.create(queryStr);
        System.out.println(query);

        DataSource dsource = DatasetFactory.create();
        dsource.addNamedModel("http://input/", model);
        QueryExecution qexec = QueryExecutionFactory.create(query, dsource);
        Model resultModel = qexec.execConstruct() ;
        qexec.close();
        System.out.println("--------") ;
        resultModel.write(System.out, "N-TRIPLES") ;
        System.out.println("--------") ;
        System.out.println();
    }


    public static void main(String[] args){
        /** 
         * I would expect all of these queries to be equivalent 
         */

        /* BIND inside the GRAPH{} section, and do regular pattern matching */
        String bind_URI_in =  "CONSTRUCT { ?s ?p ?o } \n " +
        " WHERE {  \n" +
        " GRAPH<http://input/> { \n" +
        "   BIND(<http://some/uri> AS ?s) \n" +
        "   ?s ?p ?o. \n "+
        " } \n "+
        "} \n ";

        /* BIND outside the GRAPH{} section, and do regular pattern matching */
        String bind_URI_out =  "CONSTRUCT { ?s ?p ?o } \n " +
        " WHERE {  \n" +
        " BIND(<http://some/uri> AS ?s) \n" +
        " GRAPH<http://input/> { \n" +
        "   ?s ?p ?o. \n " +
        " } \n "+
        "} \n ";

        /* BIND inside the GRAPH{} section, and FILTER inside */
        String bind_URI_in_filter =  "CONSTRUCT { ?s ?p ?o } \n " +
        " WHERE {  \n" +
        " GRAPH<http://input/> { \n" +
        "   BIND(<http://some/uri> AS ?bound) \n" +
        "   ?s ?p ?o. \n "+
        "   FILTER(?s = ?bound ) \n"+
        " } \n "+
        "} \n ";

        /* BIND outside the GRAPH{} section, and FILTER inside 
         *  
         * DOES NOT WORK AS EXPECTED
         */
        String bind_URI_out_filter =  "CONSTRUCT { ?s ?p ?o } \n " +
        " WHERE {  \n" +
        " BIND(<http://some/uri> AS ?bound) \n" +
        " GRAPH<http://input/> { \n" +
        "   ?s ?p ?o. \n " +
        "   FILTER(?s = ?bound ) \n"+
        " } \n "+
        "} \n ";

        Model model = runQuery("CONSTRUCT { <http://some/uri> <http://some/prop> <http://some/uri2> } WHERE {} ", ModelFactory.createDefaultModel());

        runQuery2(bind_URI_in,  model) ; 
        runQuery2(bind_URI_out, model) ; 
        runQuery2(bind_URI_in_filter,  model) ; 
        runQuery2(bind_URI_out_filter, model) ; 

        //    assert(runQuery(bind_URI_in,  model).size()==1); 
        //    assert(runQuery(bind_URI_out, model).size()==1); 
        //    assert(runQuery(bind_URI_in_filter,  model).size()==1); 
        //    assert(runQuery(bind_URI_out_filter, model).size()==1) : "A filter bug? "; 



    }
}

