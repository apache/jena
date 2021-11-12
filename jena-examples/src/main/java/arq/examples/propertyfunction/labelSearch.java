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

package arq.examples.propertyfunction;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.TableFactory ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpFilter ;
import org.apache.jena.sparql.algebra.op.OpJoin ;
import org.apache.jena.sparql.algebra.op.OpTable ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.expr.E_Regex ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropertyFunction ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.syntax.ElementFilter ;
import org.apache.jena.sparql.syntax.ElementGroup ;
import org.apache.jena.sparql.syntax.ElementTriplesBlock ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.vocabulary.RDFS ;

/** Example extension or property function to show rewriting part of a query.
 *  A simpler, more direct way to implement property functions is to extend
 *  one of the helper classes and have the custom code called on each solution from the
 *  the previous query stage.
 *  
 *  See examples {@link localname} for a general predicate that allows for any of
 *  subject or object to be a variable of bound value, or see {@link uppercase} for a simple
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
 *  indexing or external structure. */ 

public class labelSearch implements PropertyFunction
{
    @Override
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

    @Override
    public QueryIterator exec(QueryIterator input, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    {
        // No real need to check the pattern arguments because
        // the replacement triple pattern and regex will cope
        // but we illustrate testing here.

        Node nodeVar = argSubject.getArg() ;
        String pattern = NodeUtils.stringLiteral(argObject.getArg()) ;
        if ( pattern == null )
        {
            Log.warn(this, "Pattern must be a plain literal or xsd:string: "+argObject.getArg()) ;
            return QueryIterNullIterator.create(execCxt) ;
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
        try (QueryExecution qExec = QueryExecutionFactory.create(query, model)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
        }
        
        // Or register it.
        PropertyFunctionRegistry.get().put("http://example/f#search", labelSearch.class) ;
        prologue = "PREFIX ext: <http://example/f#>\n" ;
        qs = prologue+"SELECT * { ?x ext:search 'EF' }" ;
        query = QueryFactory.create(qs) ;
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, model) ) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.out(rs) ;
        }
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
