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

package com.hp.hpl.jena.sparql.path;


public abstract class PathVisitorByType implements PathVisitor
{
    public abstract void visitNegPS(P_NegPropSet path) ;
    public abstract void visit0(P_Path0 path) ;
    public abstract void visit1(P_Path1 path) ;
    public abstract void visit2(P_Path2 path) ;

    @Override
    public void visit(P_Link pathNode)              { visit0(pathNode) ; }

    @Override
    public void visit(P_ReverseLink pathNode)       { visit0(pathNode) ; }

    @Override
    public void visit(P_NegPropSet pathNotOneOf)    { visitNegPS(pathNotOneOf) ; }

    @Override
    public void visit(P_Inverse inversePath)        { visit1(inversePath) ; }

    @Override
    public void visit(P_Mod pathMod)                { visit1(pathMod) ; }
    
    @Override
    public void visit(P_Distinct path)              { visit1(path) ; }
    
    @Override
    public void visit(P_Multi pathMod)              { visit1(pathMod) ; }
    
    @Override
    public void visit(P_Shortest pathMod)           { visit1(pathMod) ; }
    
    @Override
    public void visit(P_FixedLength pFixedLength)   { visit1(pFixedLength) ; }

    @Override
    public void visit(P_ZeroOrOne path)             { visit1(path) ; }

    @Override
    public void visit(P_ZeroOrMore1 path)           { visit1(path) ; }

    @Override
    public void visit(P_ZeroOrMoreN path)           { visit1(path) ; }

    @Override
    public void visit(P_OneOrMore1 path)            { visit1(path) ; }

    @Override
    public void visit(P_OneOrMoreN path)            { visit1(path) ; }

    @Override
    public void visit(P_Alt pathAlt)                { visit2(pathAlt) ; }

    @Override
    public void visit(P_Seq pathSeq)                { visit2(pathSeq) ; }
}
