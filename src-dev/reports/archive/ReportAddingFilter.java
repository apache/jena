/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports.archive;

import java.text.ParseException ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.ontology.OntModel ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.expr.E_Equals ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDateTime ;
import com.hp.hpl.jena.sparql.syntax.ElementFilter ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor ;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase ;

public class ReportAddingFilter {

    // Locations of files used
//    private static final String ONTOLOGY_FILE_URL = "file:C:\\Users\\Cihan\\Desktop\\Cihan_Yedek\\SRDC\\SRDC\\IKS\\Tests\\test\\jenaDevTestOnt.owl";
//    private static final String QUERY_FILE_LOCATION = "C:\\Users\\Cihan\\Desktop\\Cihan_Yedek\\SRDC\\SRDC\\IKS\\Tests\\test\\jenaDevTestQuery";
    
    private static final String ONTOLOGY_FILE_URL = "file:Reports/AddingFilter/D.rdf" ;
    private static final String QUERY_FILE_LOCATION = "Reports/AddingFilter/Q.rq";

    
    private static final String DATETIME_FORMAT = "yyyy-mm-dd HH:mm:ss:SSSZ";
    private static final String DATETIME_INPUT = "2010-01-19 16:00:00:000-0000";

    public static void main(String[] args) throws Exception {
        ReportAddingFilter test = new ReportAddingFilter();
        test.run();
    }

    public void run() throws Exception {

        // Create and Load ontology
        OntModel ont = ModelFactory.createOntologyModel();
        ont.read(ONTOLOGY_FILE_URL);

        // Create query
//        Query query = QueryFactory.create(FileReader.getInstance().readFile(
//                QUERY_FILE_LOCATION));
        Query query = QueryFactory.read(QUERY_FILE_LOCATION) ;

        // Execute And Print Result
        executeQueryOnOntology(ont, query);
        // Add FILTER element to the query
        addElement(query);
        // Execute the new query
        executeQueryOnOntology(ont, query);

    }

    private void addElement(Query query) throws ParseException {

        // Define date format
        SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
        // Create a date object
        Date date = format.parse(DATETIME_INPUT);

        // Create variable expression
        Expr exprVar = new ExprVar("Date");
        // Create dateTime expression
        Expr exprDate = new NodeValueDateTime(dateToXSDDateTime(date));
        // Create is equal expression
        Expr exprEqual = new E_Equals(exprVar, exprDate);
        // Create FILTER
        final ElementFilter filter = new ElementFilter(exprEqual);
        // Define an element visitor to add FILTER element
        FilterAdder visitor = new FilterAdder(filter);
        // Visit query pattern so visitor will add the element filter
        query.getQueryPattern().visit(visitor);
    }

    private static XSDDateTime dateToXSDDateTime(Date date) {
        // Get a calendar
        Calendar cal = Calendar.getInstance();
        // Set time of the calendar to specified date
        cal.setTime(date);
        // return the newly created object using calendar
        return new XSDDateTime(cal);
    }

    private static void executeQueryOnOntology(OntModel ont, Query query) {
        // Print query
        System.out.println(query.serialize());
        // Create execution
        QueryExecution exec = QueryExecutionFactory.create(query, ont);
        // Print results
        System.out.println(ResultSetFormatter.asText(exec.execSelect()));

    }

    public class FilterAdder extends ElementVisitorBase implements
            ElementVisitor {
        //Filter to be added to query 
        private ElementFilter filter;

        //Public constructor
        public FilterAdder(ElementFilter elFilter) {
            filter = elFilter;
        }

        //Override this function so that it adds the desired filter
        @Override
        public void visit(ElementGroup el) {
            el.addElementFilter(filter);
        }
    }

}



/*
 * (c) Copyright 2010 Talis Systems Ltd.
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