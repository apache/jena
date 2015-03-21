package com.hp.hpl.jena.sparql.lang.sparql_11_plus;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.lang.SPARQLParser;
import com.hp.hpl.jena.sparql.lang.SPARQLParserFactory;
import com.hp.hpl.jena.sparql.lang.SPARQLParserRegistry;
import com.hp.hpl.jena.util.PrintUtil;

public class SPARQL_11_PLUS {
	public static final Syntax syntaxSPARQL_11_PLUS = new SyntaxPlus
			("http://jena.hpl.hp.com/2003/07/query/SPARQL_11_PLUS");;

	public static void init() {
		Syntax.querySyntaxNames.put("sparql_11_plus", syntaxSPARQL_11_PLUS);
		SPARQLParserRegistry.addFactory(syntaxSPARQL_11_PLUS,
				new SPARQLParserFactory() {
					@Override
					public boolean accept(Syntax syntax) {
						return syntaxSPARQL_11_PLUS.equals(syntax);
					}

					@Override
					public SPARQLParser create(Syntax syntax) {
						return new ParserSPARQL11Plus();
					}
				});

	}
	public static void main (String [] args){
	
		// initialize new SPARQL language
		SPARQL_11_PLUS.init();
		
		// create testing data
		Model model  =ModelFactory.createDefaultModel();
		Resource s  =model.createResource("http://eg.com/s");
		Property p = model.createProperty("http://eg.com/p");
		Resource o  =model.createResource("http://eg.com/o");
		model.add(s,p,o);	
		
		// construct named graph
		String queryString = "CONSTRUCT GRAPH <http://eg.com/g> {<http://eg.com/s1> <http://eg.com/p1> ?o} WHERE{<http://eg.com/s> <http://eg.com/p> ?o}";
		Query query = QueryFactory.create(queryString,syntaxSPARQL_11_PLUS) ;
		QueryExecutionPlus qexec = QueryExecutionPlusFactory.create(query, model) ;
		Dataset resultDataset = qexec.execConstructDataset();
		PrintUtil.printOut(resultDataset.getNamedModel("http://eg.com/g").listStatements());
		qexec.close() ;
		
		// construct default graphs
		queryString = "CONSTRUCT {<http://eg.com/s1> <http://eg.com/p1> ?o} WHERE{<http://eg.com/s> <http://eg.com/p> ?o}";
		query = QueryFactory.create(queryString,syntaxSPARQL_11_PLUS) ;
		qexec = QueryExecutionPlusFactory.create(query, model) ;
		resultDataset = qexec.execConstructDataset();
		PrintUtil.printOut(resultDataset.getDefaultModel().listStatements());
		qexec.close() ;
		
		
	}
}
