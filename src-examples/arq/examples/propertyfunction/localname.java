/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.examples.propertyfunction;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/** Example property function that creates the association between a URI and it's localname.
 *  See also splitIRI which is more general. This is just an example.
 * 
 *  If it is not a URI, then does not match.
 *  
 *  Use as: 
 *  
 *  <pre>
 *    ?uri ext:localname ?localname
 *  </pre>
 * 
 *  Depending on whether the subject/object are bound when called:
 *  <ul>
 *  <li>subject bound, object unbound => assign the local name to variable in object slot</li> 
 *  <li>subject bound, object bound => check the subject has the local name given by object</li>
 *  <li>subject unbound, object bound => find all URIs in the model (s, p or o) that have that local name</li>
 *  <li>subject unbound, object unbound => generate all localname for all URI resources in the model</li>
 *  </ul>
 *  The two searching forms (subject unbound) are expensive.

 *  Anything not a URI (subject) or string (object) causes no match.
 * 
 * 
 * @author Andy Seaborne
 */ 

public class localname extends PFuncSimple
{

    public QueryIterator execEvaluated(Binding binding, Node nodeURI, Node predicate, Node nodeLocalname, ExecutionContext execCxt)
    {
        if ( ! nodeURI.isVariable() )
            return execFixedSubject(nodeURI, nodeLocalname, binding, execCxt) ;
        else
            return execAllNodes(Var.alloc(nodeURI), nodeLocalname, binding, execCxt) ;
    }

    // Subject is bound : still two cases: object bound (do a check) and object unbound (assign the local name)
    private QueryIterator execFixedSubject(Node nodeURI, Node nodeLocalname, Binding binding, ExecutionContext execCxt)
    {
        if ( ! nodeURI.isURI() )
            // Subject bound but not a URI
            return new QueryIterNullIterator(execCxt) ;

        // Subject is bound and a URI - get the localname as a Node 
        Node localname = Node.createLiteral(nodeURI.getLocalName()) ;
        
        // Object - unbound variable or a value? 
        if ( ! nodeLocalname.isVariable() )
        {
            // Object bound or a query constant.  Is it the same as the calculated value?
            if ( nodeLocalname.equals(localname) )
                // Same
                return new QueryIterSingleton(binding, execCxt) ;
            // No - different - no match.
            return new QueryIterNullIterator(execCxt) ;
        }
        
        // Object unbound variable - assign the localname to it.
        Binding b = new Binding1(binding, Var.alloc(nodeLocalname), localname) ;
        
        // Return an iterator.
        return new QueryIterSingleton(b, execCxt) ;
    }
    
    // Unbound subject - work hard.
    // Still two cases: object bound (filter by localname) and object unbound (generate all localnames for all URIs)
    // Warning - will scan the entire graph (there is no localname index) but this example code. 

    private QueryIterator execAllNodes(Var subjVar, Node nodeLocalname,  Binding input, ExecutionContext execCxt)
    {
        if ( ! nodeLocalname.isVariable() )
        {
            if ( ! nodeLocalname.isLiteral() )
                // Not a variable, not a literal=> can't match
                return new QueryIterNullIterator(execCxt) ;
        
            if( ! NodeUtils.isStringLiteral(nodeLocalname) )
                // If a typed literal, must be XSD string.
                return new QueryIterNullIterator(execCxt) ;
        }
        
        //Set bindings = new HashSet() ;    // Use a Set if you want unique results. 
        List bindings = new ArrayList() ;   // Use a list if you want counting results. 
        Graph graph = execCxt.getActiveGraph() ;
        
        ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next() ;
            slot(bindings, input, t.getSubject(),   subjVar, nodeLocalname) ;
            slot(bindings, input, t.getPredicate(), subjVar, nodeLocalname) ;
            slot(bindings, input, t.getObject(),    subjVar, nodeLocalname) ;
        }
        return new QueryIterPlainWrapper(bindings.iterator(), execCxt) ;
    }

    private void slot(Collection bindings, Binding input, Node node, Var subjVar, Node nodeLocalname)
    {
        if ( ! node.isURI() ) return ;
        Node localname = Node.createLiteral(node.getLocalName()) ;
        if ( nodeLocalname.isVariable() )
        {
            // Object is an unbound variable.
            Binding b = new BindingMap(input) ;
            // Bind a pair for subject and object variables
            b.add(Var.alloc(subjVar), node) ;
            b.add(Var.alloc(nodeLocalname), localname) ;
            bindings.add(b) ;
            return ;
        }
        
        // Object is a value / bound variable.
        if ( ! nodeLocalname.sameValueAs(localname) )
            return ;
        // Bind subject to this node.
        Binding b = new Binding1(input, subjVar, node) ; 
        bindings.add(b) ;
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