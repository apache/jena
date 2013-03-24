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

/** A ElementVisitor that does nothing.  It saves writing lots of
 * empty visits when only interested in a few element types.  
 */

public class ElementVisitorBase implements ElementVisitor 
{
    @Override
    public void visit(ElementTriplesBlock el)   { }

    @Override
    public void visit(ElementFilter el)         { }
    
    @Override
    public void visit(ElementAssign el)         { }

    @Override
    public void visit(ElementBind el)           { }

    @Override
    public void visit(ElementData el)           { }

    @Override
    public void visit(ElementUnion el)          { }

    @Override
    public void visit(ElementDataset el)        { }

    @Override
    public void visit(ElementOptional el)       { }

    @Override
    public void visit(ElementGroup el)          { }

    @Override
    public void visit(ElementNamedGraph el)     { }

    @Override
    public void visit(ElementExists el)         { }
    
    @Override
    public void visit(ElementNotExists el)      { }
    
    @Override
    public void visit(ElementMinus el)          { }

    @Override
    public void visit(ElementService el)        { }
    
    @Override
    public void visit(ElementSubQuery el)       { }

    @Override
    public void visit(ElementPathBlock el)      { }
}
