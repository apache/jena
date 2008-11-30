/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.propertyfunction;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.vocabulary.RDFS;

/** Example extension or property function to show rewriting part of a query.
 *  A simpler, more driect way to implement property functions is to extends
 *  one of the helper classes and have the custom code called on each solution from the
 *  the previosu query stage.
 *  
 *  See examples {@link localname} for a general predicate that allows for any of
 *  subject or object to be a variable of boudn value, or see {@link uppercase} for a simple
 *  implementation that transforms on graph node into a new node. 
 *    
 *  This is a more complicated example which  uses the PropertyFunction interface directly.
 *  It takes the QueryIterator from the previous stage and inserts a new processing step.   
 *  It then calls that processing step to do the real work.  
 *  
 *  The approach here could be used to access an external index (e.g. Lucene) although here
 *  we just show looking for RDFS labels.
 *  
 *  <pre>
 *    ?x ext:labelSearch "something"
 *  </pre>
 *  as 
 *  <pre>
 *    ?x rdfs:label ?label . FILTER regex(?label, "something", "i")
 *  </pre>
 *  
 *  by simply doing a regex but could be used to add access to some other form of
 *  indexing or external structure.
 *  
 * @author Andy Seaborne
 */ 

public class labelSearch implements PropertyFunction
{
    List myArgs = null;
    
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        if ( argSubject.isList() || argObject.isList() )
            throw new QueryBuildException("List arguments to "+predicate.getURI()) ;
    }
    
    /* This be called once, with unevaluated arguments.
     * To do a rewrite of part of a query, we must use the fundamental PropertyFunction
     * interface to be called once with the input iterator.
     * Must not return null nor throw an exception.  Instead, return a QueryIterNullIterator
     * indicating no matches.  
     */

    public QueryIterator exec(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        // No real need to check the pattern arguments because
        // the replacement triple pattern and regex will cope
        // but we illustrate testing here.

        Node nodeVar = argSubject.getArg() ;
        String pattern = NodeUtils.stringLiteral(argObject.getArg()) ;
        if ( pattern == null )
        {
            ALog.warn(this, "Pattern must be a plain literal or xsd:string: "+argObject.getArg()) ;
            return new QueryIterNullIterator(execCxt) ;
        }

        if ( false )
            // Old (ARQ 1) way - not recommended.
            return buildSyntax(input, nodeVar, pattern, execCxt) ;
        
        // Better 
        // Build a SPARQL algebra expression
        Var var2 = createNewVar() ;                     // Hidden variable
        
        BasicPattern bp = new BasicPattern() ;
        Triple t = new Triple(nodeVar, RDFS.label.asNode(), var2) ;
        bp.add(t) ;
        OpBGP op = new OpBGP(bp) ;
        
        Expr regex = new E_Regex(new ExprVar(var2.getName()), pattern, "i") ;
        Op filter = OpFilter.filter(regex, op) ;

        // ---- Evaluation
        if ( true )
        {
            // Use the reference query engine
            // Create a table for the input stream (so it uses working memory at this point, 
            // which is why this is not the preferred way).  
            // Then join to expression for this stage.
            Table table = TableFactory.create(input) ;
            Op op2 = OpJoin.create(OpTable.create(table), filter) ;
            return Algebra.exec(op2, execCxt.getDataset()) ;
        }        
        
        // Use the default, optimizing query engine.
        return QC.execute(filter, input, execCxt) ;
    }

    
    // Build SPARQL syntax and compile it.
    // Not recommended.
    private QueryIterator buildSyntax(QueryIterator input, Node nodeVar, String pattern, ExecutionContext execCxt)
    {
        Var var2 = createNewVar() ; 
        // Triple patterns for   ?x rdfs:label ?hiddenVar
        ElementTriplesBlock elementBGP = new ElementTriplesBlock();
        Triple t = new Triple(nodeVar, RDFS.label.asNode(), var2) ;
        elementBGP.addTriple(t) ;
        
        // Regular expression for  regex(?hiddenVar, "pattern", "i") 
        Expr regex = new E_Regex(new ExprVar(var2.getName()), pattern, "i") ;
        
        ElementGroup elementGroup = new ElementGroup() ;
        elementGroup.addElement(elementBGP) ;
        elementGroup.addElement(new ElementFilter(regex)) ;
        // Compile it.
        // The better design is to build the Op structure programmatically,
        Op op = Algebra.compile(elementGroup) ;
        op = Algebra.optimize(op, execCxt.getContext()) ;
        return QC.execute(op, input, execCxt) ;
    }
    
    static int hiddenVariableCount = 0 ; 

    // Create a new, hidden, variable.
    private static Var createNewVar()
    {
        hiddenVariableCount ++ ;
        String varName = "-search-"+hiddenVariableCount ;
        return Var.alloc(varName) ;
    }
    
    // -------- Example usage
    
    public static void main(String[] argv)
    {
        // Call the function as java:arq.examples.ext.labelSearch or register it.
        String prologue = "PREFIX ext: <java:arq.examples.propertyfunction.>\n" ;

        String qs = prologue+"SELECT * { ?x ext:labelSearch 'EF' }" ;
        Query query = QueryFactory.create(qs) ;
        Model model = make() ;
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        try {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qExec.close() ; }
        
        // Or register it.
        PropertyFunctionRegistry.get().put("http://example/f#search", labelSearch.class) ;
        prologue = "PREFIX ext: <http://example/f#>\n" ;
        qs = prologue+"SELECT * { ?x ext:search 'EF' }" ;
        query = QueryFactory.create(qs) ;
        qExec = QueryExecutionFactory.create(query, model) ;
        try {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
        } finally { qExec.close() ; }
    }
    
    private static Model make()
    {
        String BASE = "http://example/" ;
        Model model = ModelFactory.createDefaultModel() ;
        model.setNsPrefix("", BASE) ;
        Resource r1 = model.createResource(BASE+"r1") ;
        Resource r2 = model.createResource(BASE+"r2") ;

        r1.addProperty(RDFS.label, "abc") ;
        r2.addProperty(RDFS.label, "def") ;

        return model  ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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