/**
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

package com.hp.hpl.jena.sparql.core;

import java.util.Collection ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** Useful operations involving Var */
public class Vars {
    
    public static void addVarsFromTriple(Collection<Var> acc, Triple t) {
        addVar(acc, t.getSubject()) ;
        addVar(acc, t.getPredicate()) ;
        addVar(acc, t.getObject()) ;
    }

    public static void addVarsFromQuad(Collection<Var> acc, Quad q) {
        addVar(acc, q.getSubject()) ;
        addVar(acc, q.getPredicate()) ;
        addVar(acc, q.getObject()) ;
        addVar(acc, q.getGraph()) ;
    }

    public static void addVar(Collection<Var> acc, Node n) {
        if (n == null)
            return ;

        if (n.isVariable())
            acc.add(Var.alloc(n)) ;
    }

}

