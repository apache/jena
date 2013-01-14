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

package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.*;

/**
	ReasonerTestBase - provide common base code for reasoner tests
*/

public class ReasonerTestBase extends ModelTestBase 
    {
	public ReasonerTestBase( String name ) 
        { super( name ); }
              
    /**
        Answer a new resource guaranteed to be in a different model from any other
        that might be around, so we make a new model for that purpose.
        
        @return a new [bnode] resource in a never-seen-before model
     */
    protected static Resource newResource() 
        { return ModelFactory.createDefaultModel().createResource(); }
    }
