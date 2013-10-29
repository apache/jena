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

package com.hp.hpl.jena.sparql.engine.binding ;

import java.util.Iterator ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;

public class BindingUtils {
    /** Convert a query solution to a binding */
    public static Binding asBinding(QuerySolution qSolution) {
        if ( qSolution == null )
            return null ;
        if ( qSolution instanceof ResultBinding )
            // Only named variables.
            return new BindingProjectNamed(((ResultBinding)qSolution).getBinding()) ;
        BindingMap binding = BindingFactory.create() ;
        addToBinding(binding, qSolution) ;
        return binding ;
    }

    public static void addToBinding(BindingMap binding, QuerySolution qSolution) {
        if ( qSolution == null )
            return ;

        for ( Iterator<String> iter = qSolution.varNames() ; iter.hasNext() ; ) {
            String n = iter.next() ;

            RDFNode x = qSolution.get(n) ;
            if ( Var.isBlankNodeVarName(n) )
                continue ;
            try {
                binding.add(Var.alloc(n), x.asNode()) ;
            }
            catch (ARQInternalErrorException ex) {
                // bad binding attempt.
                Log.warn(BindingUtils.class, "Attempt to bind " + n + " when already bound") ;
            }
        }
    }

    public static void addAll(BindingMap dest, Binding src) {
        Iterator<Var> iter = src.vars() ;
        for ( ; iter.hasNext() ; ) {
            Var v = iter.next() ;
            Node n = src.get(v) ;
            dest.add(v, n) ;
        }
    }

    /** Merge two bindings, assuming they are compatible. */
    public static Binding merge(Binding bind1, Binding bind2) {
        //Create binding from LHS
        BindingMap b2 = BindingFactory.create(bind1) ;
        Iterator<Var> vIter = bind2.vars() ;
        // Add any variables from the RHS
        for ( ; vIter.hasNext() ; ) {
            Var v = vIter.next() ;
            if ( ! b2.contains(v) )
                b2.add(v, bind2.get(v)) ;
            else {
                // Checking!
                Node n1 = bind1.get(v) ;
                Node n2 = bind2.get(v) ;
                if ( ! n1.equals(n2) )
                    Log.warn(BindingUtils.class,  "merge: Mismatch : "+n1+" != "+n2);
            }
        }
        return b2 ;
    }

    public static boolean equals(Binding b1, Binding b2) {
        return BindingBase.equals(b1, b2) ;
    }
}
