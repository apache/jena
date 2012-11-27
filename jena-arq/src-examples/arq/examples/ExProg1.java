/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package arq.examples;


// The ARQ application API.
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.vocabulary.DC ;

/** Example : Build a query syntax programmatically.
 * 
 *  Note: it is often better to build and execute an algebra expression.  See other examples. */

public class ExProg1
{
    static public final String NL = System.getProperty("line.separator") ; 
    
    public static void main(String[] args)
    {
        Model model = createModel() ;
        
        Query query = QueryFactory.make() ;

        query.setQuerySelectType() ;
        
        // Build pattern
        
        ElementGroup elg = new ElementGroup() ;
        
        Var varTitle = Var.alloc("title") ;
        Var varX = Var.alloc("x") ;
        
        Triple t1 = new Triple(varX, DC.title.asNode(),  varTitle) ;
        elg.addTriplePattern(t1) ;
        
        // Don't use bNodes for anon variables.  The conversion is done in parsing.
        // BNodes here are assumed to be values from the target graph.
        Triple t2 = new Triple(varX, DC.description.asNode(), Var.alloc("desc")) ;
        elg.addTriplePattern(t2) ;
        
        // Attach the group to query.  
        query.setQueryPattern(elg) ;

        // Choose what we want - SELECT *
        //query.setQueryResultStar(true) ;
        query.addResultVar(varTitle) ;
        
        // Print query with line numbers
        // Prefix mapping just helps serialization
        query.getPrefixMapping().setNsPrefix("dc" , DC.getURI()) ;
        query.serialize(new IndentedWriter(System.out,true)) ;
        System.out.println() ;
        
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        
        try {
            // Assumption: it's a SELECT query.
            ResultSet rs = qexec.execSelect() ;
            
            // The order of results is undefined.
            System.out.println("Titles: ") ;
            for ( ; rs.hasNext() ; )
            {
                QuerySolution rb = rs.nextSolution() ;
                
                // Get title - variable names do not include the '?' (or '$')
                RDFNode x = rb.get("title") ;
                
                // Check the type of the result value
                if ( x instanceof Literal )
                {
                    Literal titleStr = (Literal)x  ;
                    System.out.println("    "+titleStr) ;
                }
                else
                    System.out.println("Strange - not a literal: "+x) ;
                    
            }
        }
        finally
        {
            // QueryExecution objects should be closed to free any system resources 
            qexec.close() ;
        }
    }
    
    public static Model createModel()
    {
        Model model = ModelFactory.createDefaultModel() ;
        
        Resource r1 = model.createResource("http://example.org/book#1") ;
        Resource r2 = model.createResource("http://example.org/book#2") ;
        Resource r3 = model.createResource("http://example.org/book#3") ;
        
        r1.addProperty(DC.title, "SPARQL - the book")
          .addProperty(DC.description, "A book about SPARQL") ;
        
        r2.addProperty(DC.title, "Advanced techniques for SPARQL") ;

        r3.addProperty(DC.title, "Jena - an RDF framework for Java")
          .addProperty(DC.description, "A book about Jena") ;

        return model ;
    }
}
