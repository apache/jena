/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples;


// The ARQ application API.
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.vocabulary.DC;

/** Example : Build a query with a filter programmatically.
 *  Note: it is often better to build and execute an algebra expression.  See other examples.
 * 
 * @author Andy Seaborne
 */

public class ExProg2
{
    static public final String NL = System.getProperty("line.separator") ; 
    
    public static void main(String[] args)
    {
        Model model = createModel() ;
        
        Query query = QueryFactory.make() ;
        query.setQueryType(Query.QueryTypeSelect) ;
        
        // See also ExProg1
        
        ElementGroup elg = new ElementGroup() ;
        
        Var varTitle = Var.alloc("title") ;
        Var varX = Var.alloc("x") ;
        
        Triple t1 = new Triple(varX, DC.title.asNode(),  varTitle) ;
        elg.addTriplePattern(t1) ;
        
        // Adds a filter.  Need to wrap variable in a NodeVar.
        Expr expr = new E_Regex(new ExprVar(varTitle), "sparql", "i") ;
        ElementFilter filter = new  ElementFilter(expr) ;
        elg.addElementFilter(filter) ;
        
        // Attach the group to query.  
        query.setQueryPattern(elg) ;
        
        // Choose what we want - SELECT ?title
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