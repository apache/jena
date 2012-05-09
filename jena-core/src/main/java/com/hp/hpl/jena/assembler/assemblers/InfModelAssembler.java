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

package com.hp.hpl.jena.assembler.assemblers;


import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;

public class InfModelAssembler extends ModelAssembler
    {
    @Override
    protected Model openEmptyModel( Assembler a, Resource root, Mode mode )
        {
        checkType( root, JA.InfModel );
        Model base = getBase( a, root, mode );
        Reasoner reasoner = getReasoner( a, root );
        InfModel result = ModelFactory.createInfModel( reasoner, base );
        return result;
        }

    protected Model getBase( Assembler a, Resource root, Mode mode )
        {
        Resource base = getUniqueResource( root, JA.baseModel );
        return base == null ? ModelFactory.createDefaultModel() : a.openModel( base, mode );
        }

    protected Reasoner getReasoner( Assembler a, Resource root )
        { return getReasonerFactory( a, root ).create( root ); }
    
    protected ReasonerFactory getReasonerFactory( Assembler a, Resource root )
        { 
        Resource factory = getUniqueResource( root, JA.reasoner );
        return factory == null
            ? GenericRuleReasonerFactory.theInstance()
            : (ReasonerFactory) a.open( factory )
            ;        
        }
    }
