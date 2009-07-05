package dev;


import arq.cmd.CmdUtils;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Report
{
    public static void main(String[] args){

        CmdUtils.setLog4j() ;
        String testNS = "http://test.com/test#";

        Model domain = ModelFactory.createDefaultModel();
        domain.read("file:tmp/domain.n3", "N3");

        Model instance = ModelFactory.createDefaultModel();
        instance.read("file:tmp/instance.n3", "N3");

        DataSource src = DatasetFactory.create();
        Model domain_instance = ModelFactory.createDefaultModel();
        domain_instance.add(domain);
        domain_instance.add(instance);

        src.setDefaultModel(domain_instance);
        executePathQuery(src);

        System.out.println("==== START TDB ====") ;
        
        TDB.getContext().setTrue(TDB.symUnionDefaultGraph); // Has no effect on Path evaluation?
        TDB.getContext().setTrue(TDB.symLogExec);
        //Dataset ds = TDBFactory.createDataset("/Users/Gyaan/eclipse_workspace_etr/EtrConfiguration/UserDatasetTest/users/");
        Dataset ds = TDBFactory.createDataset();
        Model tdbDomain = ds.getNamedModel("urn:test:domain");
        tdbDomain.add(domain);

        Model tdbInstance = ds.getNamedModel("urn:test:instance");
        tdbInstance.add(instance);

        TDB.sync(ds);

//        Query q = QueryFactory.create("SELECT * {?s ?p ?o}") ;
//        ResultSetFormatter.out(QueryExecutionFactory.create(q, ds).execSelect()) ;
        
        
        
        //Dataset tdbSrc = TDBFactory.createDataset("/Users/Gyaan/eclipse_workspace_etr/EtrConfiguration/UserDatasetTest/users/");
        executePathQuery(ds);
    }

    public static void executePathQuery(Dataset src){

        String[] sparql = {
            "PREFIX rdf: <"+RDF.getURI()+"> " ,
            "PREFIX rdfs: <"+RDFS.getURI()+"> " ,
            "SELECT ?typeUri ?type ",
            "WHERE { " ,
            " ?typeUri rdf:type/rdfs:subClassOf* ?type . ",
            "} ",
            //"ORDER BY ?typeUri ",
            //"LIMIT 10"
        };

        try{
            QueryExecution cqexec = null;
            Query q = QueryFactory.create(StringUtils.join("\n",sparql), Syntax.syntaxARQ) ;
//            Op op = Algebra.compile(q) ;
//            op = Algebra.optimize(op) ;
//            System.out.println(op) ;
            
            cqexec = QueryExecutionFactory.create( q,  src) ;
            ResultSet results = cqexec.execSelect();
            ResultSetFormatter.out(System.out, results);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }



}
