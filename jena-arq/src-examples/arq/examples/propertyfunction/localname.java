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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.pfunction.PFuncSimple ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.util.iterator.ExtendedIterator ;

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
 * */ 

public class localname extends PFuncSimple
{

    @Override
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
            return QueryIterNullIterator.create(execCxt) ;

        // Subject is bound and a URI - get the localname as a Node 
        Node localname = NodeFactory.createLiteral(nodeURI.getLocalName()) ;
        
        // Object - unbound variable or a value? 
        if ( ! nodeLocalname.isVariable() )
        {
            // Object bound or a query constant.  Is it the same as the calculated value?
            if ( nodeLocalname.equals(localname) )
                // Same
                return QueryIterSingleton.create(binding, execCxt) ;
            // No - different - no match.
            return QueryIterNullIterator.create(execCxt) ;
        }
        
        // Object unbound variable - assign the localname to it.
        return QueryIterSingleton.create(binding, Var.alloc(nodeLocalname), localname, execCxt) ;
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
                return QueryIterNullIterator.create(execCxt) ;
        
            if( ! NodeUtils.isSimpleString(nodeLocalname) )
                return QueryIterNullIterator.create(execCxt) ;
        }
        
        //Set bindings = new HashSet() ;    // Use a Set if you want unique results. 
        List<Binding> bindings = new ArrayList<>() ;   // Use a list if you want counting results. 
        Graph graph = execCxt.getActiveGraph() ;
        
        ExtendedIterator<Triple>iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            slot(bindings, input, t.getSubject(),   subjVar, nodeLocalname) ;
            slot(bindings, input, t.getPredicate(), subjVar, nodeLocalname) ;
            slot(bindings, input, t.getObject(),    subjVar, nodeLocalname) ;
        }
        return new QueryIterPlainWrapper(bindings.iterator(), execCxt) ;
    }

    private void slot(Collection<Binding> bindings, Binding input, Node node, Var subjVar, Node nodeLocalname)
    {
        if ( ! node.isURI() ) return ;
        Node localname = NodeFactory.createLiteral(node.getLocalName()) ;
        if ( nodeLocalname.isVariable() )
        {
            // Object is an unbound variable.
            BindingMap b = BindingFactory.create(input) ;
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
        Binding b = BindingFactory.binding(input, subjVar, node) ; 
        bindings.add(b) ;
    }

}
