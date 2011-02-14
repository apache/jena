/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.RDFNode ;


public class ReportRemoteVirtuoso
{
    static { Log.setLog4j() ; }
    
    public static void main(String[] args) {
        
        String service = "http://pubmed.bio2rdf.org/sparql";
        String querystring = "PREFIX dc:<http://purl.org/dc/terms/> \n"
            + "PREFIX pub:<http://bio2rdf.org/pubmed:> \n"
            + "PREFIX pubres:<http://bio2rdf.org/pubmed_resource:> \n"
            + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
            + "select ?title ?mesh ?last ?first \n"
            + "where { \n"
            + "pub:18231773 dc:title ?title . \n"
            + "pub:18231773 pubres:subject_headings ?mesh . \n"
            + "pub:18231773 pubres:author ?authorid . \n"
            + "?authorid foaf:lastName ?last . \n"
            + "?authorid foaf:firstName ?first . \n"
            + "} ";

        ResultSet results = remoteSelectQuery(service, querystring);
        String[] kws = {"title", "mesh", "author"};
        
//        long count = ResultSetFormatter.consume(results) ;
//        System.out.println("Count = "+count) ;
        
        printResults(results, kws);
    }

    private static ResultSet remoteSelectQuery(String service, String querystring) {
        System.out.println(querystring);
        
        if ( true )
            ARQ.getContext().setTrue(ARQ.useSAX) ;
        
        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, querystring);
        
        try {
            return qexec.execSelect();
        } finally {
            //****** TOO EARLY
            qexec.close();
        }
    }

    private static void printResults(ResultSet results, String[] strings) {
        int line = 0 ; 
        while (results.hasNext()) {
            line++ ;
            System.out.printf("%03d: ", line) ;
            QuerySolution soln = results.nextSolution();
            for (String s : strings) {
                RDFNode x = soln.get(s);       // Get a result variable by name.
                if (x != null) {
                    System.out.println(s + ": " + x.toString());
                }
            }
        }
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */