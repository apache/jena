/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples;


// The ARQ application API.
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;

/** Example 1 : Execute a simple SELECT query on a model
 *  to find the DC titles contained in a model. 
 * 
 * @author Andy Seaborne
 */

public class Ex1
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
        
        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
        // Or QueryExecutionFactory.create(queryString, model) ;

        System.out.println("Titles: ") ;
        
        try {
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
        finally
        {
            // QueryExecution objects should be closed to free any system resources 
            qexec.close() ;
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


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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