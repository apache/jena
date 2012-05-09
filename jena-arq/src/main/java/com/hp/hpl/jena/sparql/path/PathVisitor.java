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

package com.hp.hpl.jena.sparql.path;

public interface PathVisitor
{
    public void visit(P_Link pathNode) ;
    public void visit(P_ReverseLink pathNode) ;
    
    public void visit(P_NegPropSet pathNotOneOf) ;

    public void visit(P_Inverse inversePath) ;
    public void visit(P_Mod pathMod) ;
    public void visit(P_FixedLength pFixedLength) ;
    public void visit(P_Distinct pathDistinct) ;
    public void visit(P_Multi pathMulti) ;
    public void visit(P_Shortest pathShortest) ;
    public void visit(P_ZeroOrOne path) ;
    
    public void visit(P_ZeroOrMore1 path) ;
    public void visit(P_ZeroOrMoreN path) ;
    
    public void visit(P_OneOrMore1 path) ;
    public void visit(P_OneOrMoreN path) ;

    public void visit(P_Alt pathAlt) ;
    public void visit(P_Seq pathSeq) ;
}
