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

public class PathVisitorBase implements PathVisitor
{
    @Override public void visit(P_Link pathNode)              {}
    @Override public void visit(P_ReverseLink pathNode)       {}
    
    @Override public void visit(P_NegPropSet pathNotOneOf)    {}

    @Override public void visit(P_Inverse inversePath)        {}
    @Override public void visit(P_Mod pathMod)                {}
    @Override public void visit(P_FixedLength pFixedLength)   {}
    @Override public void visit(P_Distinct pathDistinct)      {}
    @Override public void visit(P_Multi pathMulti)            {}
    @Override public void visit(P_Shortest pathShortest)      {}
    @Override public void visit(P_ZeroOrOne path)             {}
    
    @Override public void visit(P_ZeroOrMore1 path)           {}
    @Override public void visit(P_ZeroOrMoreN path)           {}
    
    @Override public void visit(P_OneOrMore1 path)            {}
    @Override public void visit(P_OneOrMoreN path)            {}

    @Override public void visit(P_Alt pathAlt)                {}
    @Override public void visit(P_Seq pathSeq)                {}
}
