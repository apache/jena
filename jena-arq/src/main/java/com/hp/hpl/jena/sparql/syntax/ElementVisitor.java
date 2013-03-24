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

package com.hp.hpl.jena.sparql.syntax;

public interface ElementVisitor
{
    public void visit(ElementTriplesBlock el) ;
    public void visit(ElementPathBlock el) ;
    public void visit(ElementFilter el) ;
    public void visit(ElementAssign el) ;
    public void visit(ElementBind el) ;
    public void visit(ElementData el) ;
    public void visit(ElementUnion el) ;
    public void visit(ElementOptional el) ;
    public void visit(ElementGroup el) ;
    public void visit(ElementDataset el) ;
    public void visit(ElementNamedGraph el) ;
    public void visit(ElementExists el) ;
    public void visit(ElementNotExists el) ;
    public void visit(ElementMinus el) ;
    public void visit(ElementService el) ;
    public void visit(ElementSubQuery el) ;
}
