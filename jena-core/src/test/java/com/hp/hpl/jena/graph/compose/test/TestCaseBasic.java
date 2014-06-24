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

package com.hp.hpl.jena.graph.compose.test;

import java.lang.reflect.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestCaseBasic extends com.hp.hpl.jena.regression.TestCaseBasic 
	{
    private Class<? extends Graph> graphClass;

    public TestCaseBasic(String name, Class<? extends Graph> graphClass) 
    	{
        super(name);
        this.graphClass = graphClass;
    	}
    
    private Graph newGraph( Constructor< ? extends Graph> cons ) throws Exception
    	{
    	return cons.newInstance
            ( Factory.createGraphMem(), Factory.createGraphMem() );
    	}
    	
    @Override public void setUp() throws Exception
    	{
		Constructor< ? extends Graph> constructor = graphClass.getConstructor
            (new Class [] { Graph.class, Graph.class } );
    	m1 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m2 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m3 = ModelFactory.createModelForGraph( newGraph( constructor ) );
    	m4 = ModelFactory.createModelForGraph( newGraph( constructor ) );
		}
	}
