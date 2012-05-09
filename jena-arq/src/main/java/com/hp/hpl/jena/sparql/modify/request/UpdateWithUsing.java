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

package com.hp.hpl.jena.sparql.modify.request;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.update.Update ;

public abstract class UpdateWithUsing extends Update
{
    private Node withIRI = null ;
    private List<Node> using = new ArrayList<Node>() ;
    private List<Node> usingNamed = new ArrayList<Node>() ;

    private List<Node> usingView = Collections.unmodifiableList(using) ;
    private List<Node> usingNamedView = Collections.unmodifiableList(usingNamed) ;
    
    public UpdateWithUsing() {}

    public void addUsing(Node node)         { using.add(node) ; }
    public void addUsingNamed(Node node)    { usingNamed.add(node) ; }
    
    public List<Node> getUsing()            { return usingView ; }
    public List<Node> getUsingNamed()       { return usingNamedView ; }
    
    public Node getWithIRI()                { return withIRI ; }
    public void setWithIRI(Node node)       { this.withIRI = node ; } 
}
