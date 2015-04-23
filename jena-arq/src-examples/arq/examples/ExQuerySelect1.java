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
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.vocabulary.DC ;

/** Example 1 : Execute a simple SELECT query on a model
 *  to find the DC titles contained in a model. */

public class ExQuerySelect1
{
    static public final String NL = System.getProperty("line.separator") ; 
    
    public static void main(String[] args)
    {
        // Create the data.
        // This wil be the background (unnamed) graph in the dataset.
        Model model = createModel() ;
        
        // First part or the query string 
        String prolog = "PREFIX dc: <"+DC.getURI()+">" ;
        
        // Query string.
        String queryString = prolog + NL +
            "SELECT ?title WHERE {?x dc:title ?title}" ; 
        
        Query query = QueryFactory.create(queryString) ;
        // Print with line numbers
        query.serialize(new IndentedWriter(System.out,true)) ;
        System.out.println() ;
        
        // Create a single execution of this query, apply to a model
        // which is wrapped up as a Dataset
        
        try(QueryExecution qexec = QueryExecutionFactory.create(query, model)){
            // Or QueryExecutionFactory.create(queryString, model) ;

            System.out.println("Titles: ") ;

            // Assumption: it's a SELECT query.
            ResultSet rs = qexec.execSelect() ;

            // The order of results is undefined. 
            for ( ; rs.hasNext() ; )
            {
                QuerySolution rb = rs.nextSolution() ;

                // Get title - variable names do not include the '?' (or '$')
                RDFNode x = rb.get("title") ;

                // Check the type of the result value
                if ( x.isLiteral() )
                {
                    Literal titleStr = (Literal)x  ;
                    System.out.println("    "+titleStr) ;
                }
                else
                    System.out.println("Strange - not a literal: "+x) ;

            }
        }
    }
    
    public static Model createModel()
    {
        Model m = ModelFactory.createDefaultModel() ;
        
        Resource r1 = m.createResource("http://example.org/book#1") ;
        Resource r2 = m.createResource("http://example.org/book#2") ;
        
        r1.addProperty(DC.title, "SPARQL - the book")
          .addProperty(DC.description, "A book about SPARQL") ;
        
        r2.addProperty(DC.title, "Advanced techniques for SPARQL") ;
        
        return m ;
    }
}
