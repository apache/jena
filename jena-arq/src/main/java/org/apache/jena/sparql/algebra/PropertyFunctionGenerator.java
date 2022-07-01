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

package org.apache.jena.sparql.algebra;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.algebra.op.OpPropFunc ;
import org.apache.jena.sparql.algebra.op.OpSequence ;
import org.apache.jena.sparql.algebra.op.OpTable ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.graph.GNode ;
import org.apache.jena.sparql.util.graph.GraphList ;

public class PropertyFunctionGenerator
{
    public static Op buildPropertyFunctions(PropertyFunctionRegistry registry, OpBGP opBGP, Context context)
    {
        if ( opBGP.getPattern().isEmpty() )
            return opBGP ;
        return compilePattern(registry, opBGP.getPattern(), context) ;
    }
    
    private static Op compilePattern(PropertyFunctionRegistry registry, BasicPattern pattern, Context context)
    {   
        // Split into triples and property functions.

        // 1/ Find property functions.
        //    Property functions may involve other triples (for list arguments)
        //    (but leave the property function triple in-place as a marker)
        // 2/ Find arguments for property functions
        //    (but leave the property function triple in-place as a marker)
        // 3/ For remaining triples, put into basic graph patterns,
        //    and string together the procedure calls and BGPs.
        
        List<Triple> propertyFunctionTriples = new ArrayList<>() ;    // Property functions seen
        BasicPattern triples = new BasicPattern(pattern) ;  // A copy of all triples (later, it is mutated)
        
        // Find the triples invoking property functions, and those not.
        findPropertyFunctions(context, pattern, registry, propertyFunctionTriples) ;
        
        if ( propertyFunctionTriples.size() == 0 )
            //No property functions.
            return new OpBGP(pattern) ;
        
        Map<Triple, PropertyFunctionInstance> pfInvocations = new HashMap<>() ;  // Map triple => property function instance
        // Removes triples of list arguments.  This mutates 'triples'
        findPropertyFunctionArgs(context, triples, propertyFunctionTriples, pfInvocations) ;
        
        // Now make the OpSequence structure.
        Op op = makeStages(triples, pfInvocations) ;
        return op ;
    }

    private static void findPropertyFunctions(Context context, 
                                              BasicPattern pattern,
                                              PropertyFunctionRegistry registry,
                                              List<Triple> propertyFunctionTriples)
    {
        // Step 1 : find property functions (if any); collect triples.
        // Not list arg triples at this point.
        for ( Triple t : pattern )
        {
            if ( isMagicProperty(registry, t) )
                propertyFunctionTriples.add(t) ;
        }
    }

    
    private static void findPropertyFunctionArgs(Context context, 
                                                 BasicPattern triples,
                                                 List<Triple> propertyFunctionTriples,
                                                 Map<Triple, PropertyFunctionInstance> pfInvocations)
    {
        // Step 2 : for each property function, remove associated triples in list arguments; 
        // Leave the propertyFunction triple itself.

        for ( Triple pf : propertyFunctionTriples )
        {
            PropertyFunctionInstance pfi = magicProperty( context, pf, triples );
            pfInvocations.put( pf, pfi );
        }
    }
    
    private static class PropertyFunctionInstance
    {
        Node predicate ;
        PropFuncArg subjArgs ;
        PropFuncArg objArgs ;
        
         PropertyFunctionInstance(PropFuncArg sArgs, Node predicate, PropFuncArg oArgs)
        {
            this.subjArgs = sArgs ;
            this.predicate = predicate ;
            this.objArgs = oArgs ;
        }
        
        ExprList argList()
        {
            ExprList exprList = new ExprList() ;
            argList(exprList, subjArgs) ;
            argList(exprList, objArgs) ;
            return exprList ;
        }
        
        PropFuncArg getSubjectArgList()     { return subjArgs ; }
        PropFuncArg getObjectArgList()         { return objArgs ; }

        private static void argList(ExprList exprList, PropFuncArg pfArg)
        {
            if ( pfArg.isNode() )
            {
                Node n = pfArg.getArg() ;
                Expr expr = ExprLib.nodeToExpr(n) ;
                exprList.add(expr) ;
                return ;
            }
            
            for (  Node n : pfArg.getArgList() )
            {
                Expr expr = ExprLib.nodeToExpr(n) ;
                exprList.add(expr) ;
            }
        }
    }

    private static Op makeStages(BasicPattern triples, Map<Triple, PropertyFunctionInstance> pfInvocations)
    {
        // Step 3 : Make the operation expression.
        //   For each property function, insert the implementation 
        //   For each block of non-property function triples, make a BGP.
        
        Op op = null; 
        BasicPattern pattern = null ;
        for ( Triple t : triples )
        {
            if ( pfInvocations.containsKey(t) )
            {
                op = flush(pattern, op) ;
                pattern = null ;
                PropertyFunctionInstance pfi = pfInvocations.get(t) ;
                OpPropFunc opPF =  new OpPropFunc(t.getPredicate(), pfi.getSubjectArgList(), pfi.getObjectArgList(), op) ;
                op = opPF ;
                continue ;
            }       
                
            // Regular triples - make sure there is a basic pattern in progress. 
            if ( pattern == null )
                pattern = new BasicPattern() ;
            pattern.add(t) ;
        }
        op = flush(pattern, op) ;
        return op ;
    }
    
    private static Op flush(BasicPattern pattern, Op op)
    {
        if ( pattern == null || pattern.isEmpty() )
        {
            if ( op == null )
                return OpTable.unit() ;
            return op ;
        }
        OpBGP opBGP = new OpBGP(pattern) ;
        return OpSequence.create(op, opBGP) ;
    }
    
    private static boolean isMagicProperty(PropertyFunctionRegistry registry, Triple pfTriple)
    {
        if ( ! pfTriple.getPredicate().isURI() ) 
            return false ;

        if ( registry.manages(pfTriple.getPredicate().getURI()) )
            return true ;
        
        return false ;
    }
    
    // Remove all triples associated with this magic property.
    // Make an instance record.
   private static PropertyFunctionInstance magicProperty(Context context,
                                                         Triple pfTriple,
                                                         BasicPattern triples)
    {
        List<Triple> listTriples = new ArrayList<>() ;

        GNode sGNode = new GNode(triples, pfTriple.getSubject()) ;
        GNode oGNode = new GNode(triples, pfTriple.getObject()) ;
        List<Node> sList = null ;
        List<Node> oList = null ;
        
        if ( GraphList.isListNode(sGNode) )
        {
            sList = GraphList.members(sGNode) ;
            GraphList.allTriples(sGNode, listTriples) ;
        }
        if ( GraphList.isListNode(oGNode) )
        {
            oList = GraphList.members(oGNode) ;
            GraphList.allTriples(oGNode, listTriples) ;
        }
        
        PropFuncArg subjArgs = new PropFuncArg(sList, pfTriple.getSubject()) ;
        PropFuncArg objArgs =  new PropFuncArg(oList, pfTriple.getObject()) ;
        
        // Confuses single arg with a list of one. 
        PropertyFunctionInstance pfi = new PropertyFunctionInstance(subjArgs, pfTriple.getPredicate(), objArgs) ;
        
        triples.getList().removeAll(listTriples) ;
        return pfi ;
    }

   
}
