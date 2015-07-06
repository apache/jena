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

package org.apache.jena.sparql.util;

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Set ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;

public class VarUtils
{
    public static Set<Var> getVars(Triple triple)
    {
        Set<Var> x = new HashSet<>() ;
        addVarsFromTriple(x, triple) ;
        return x ;
    }
    
    public static void addVarsFromTriple(Collection<Var> acc, Triple t)
    {
        addVar(acc, t.getSubject()) ;
        addVar(acc, t.getPredicate()) ;
        addVar(acc, t.getObject()) ;
    }

    public static void addVarsFromTriplePath(Collection<Var> acc, TriplePath tpath)
    {
        addVar(acc, tpath.getSubject()) ;
        addVar(acc, tpath.getObject()) ;
    }

    public static void addVar(Collection<Var> acc, Node n)
    {
        if ( n == null )
            return ;

        if ( n.isVariable() )
            acc.add(Var.alloc(n)) ;
    }

    public static void addVars(Collection<Var> acc, BasicPattern pattern)
    {
        addVars(acc, pattern.getList()) ;
    }

    public static void addVars(Collection<Var> acc, Collection<Triple> triples)
    {
        for ( Triple triple : triples )
            addVarsFromTriple(acc, triple) ;
    }

}
