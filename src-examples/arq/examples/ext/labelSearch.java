/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.ext;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.core.ElementBasicGraphPattern;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine1.ExecutionContext;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.query.engine1.plan.PlanBasicGraphPattern;
import com.hp.hpl.jena.query.expr.E_Regex;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.expr.NodeVar;
import com.hp.hpl.jena.query.pfunction.PropFuncArg;
import com.hp.hpl.jena.query.pfunction.PropertyFunction;
import com.hp.hpl.jena.query.util.NodeUtils;
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
 *    ?x ext:search "something"
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
 * @version $Id: labelSearch.java,v 1.1 2006-08-18 11:55:08 andy_seaborne Exp $
 */ 

public class labelSearch implements PropertyFunction
{
    private static Log log = LogFactory.getLog(labelSearch.class) ;
    
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
        log.debug("labelSearch.exec") ;
        
        // No real need to check the pattern arguments because the 
        // replacement but we illustrate testing here.

        Node nodeVar = argSubject.getArg() ;
        String pattern = NodeUtils.stringLiteral(argObject.getArg()) ;
        if ( pattern == null )
        {
            log.warn("Pattern must be a plain literal or xsd:string: "+argObject.getArg()) ;
            return new QueryIterNullIterator(execCxt) ;
        }
        
        Node var2 = createNewVar() ;   // Hidden variable
        
        // Triple patterns for   ? rdfs:label ?hiddenVar
        ElementBasicGraphPattern elementBGP = new ElementBasicGraphPattern();
        Triple t = new Triple(nodeVar, RDFS.label.asNode(), var2) ;
        elementBGP.addTriple(t) ;

        // Regular expression for  regex(?hiddenVar, "pattern", "i") 
        Expr regex = new E_Regex(new NodeVar(var2.getName()), pattern, "i") ; 
        elementBGP.addConstraint(regex) ;
        
        // Turn into a query execute plan ... 
        PlanElement planBGP = PlanBasicGraphPattern.make(execCxt.getContext(), elementBGP);
        
        // System.out.print(planBGP.toString()) ;   // See what is generated
        return planBGP.build(input, execCxt) ;
    }

    static int hiddenVariableCount = 0 ; 

    // Create a new, hidden, variable.
    private static Node createNewVar()
    {
        hiddenVariableCount ++ ;
        return Node.createVariable("-search-"+hiddenVariableCount) ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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