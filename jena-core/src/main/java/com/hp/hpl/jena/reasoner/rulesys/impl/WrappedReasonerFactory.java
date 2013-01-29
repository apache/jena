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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.FileManager;

/**
    WrappedReasonerFactory - a wrapper round ReasonerFactories that
    accepts a Resource configuring initial rules, schemas, etc.
*/
public final class WrappedReasonerFactory implements ReasonerFactory
    {
    protected final ReasonerFactory factory;
    protected final Resource config;
    
    protected final Model schemaUnion = ModelFactory.createDefaultModel();
    
    public WrappedReasonerFactory( ReasonerFactory rrf, Resource config )
        { super();
        this.factory = rrf; 
        this.config = config;
        loadSchemas( schemaUnion, config ); }
    
    /**
         Answer a Reasoner created according to the underlying factory, and then 
         loaded with this Wrapper's rules (if the Reasoner is a RuleReasoner) and
         bound to this Wrapper's schemas (in an unspecified order).
     */
    @Override
    public Reasoner create( Resource ignored )
        { Reasoner result = factory.create( config );
        return schemaUnion.isEmpty() ? result : result.bindSchema( schemaUnion ); }

    public static final Property schemaURL = ResourceFactory.createProperty( "http://jena.hpl.hp.com/2003/08/jms#schemaURL" );
    
    private static Model loadSchemas( Model schema, Resource R )
        {
        StmtIterator schemas = R.listProperties( schemaURL );
        if (schemas.hasNext())
            {
            System.err.println( "WARNING: detected obsolete use of jms:schemaURL when wrapping a reasoner factory" );
            System.err.println( "  This will fail to work in the next release of Jena" );
            }
        while (schemas.hasNext())
            {
            Statement s = schemas.nextStatement();
            Resource sc = s.getResource();
            FileManager.get().readModel( schema, sc.getURI() );
            }
        return schema;
        }
    
    /**
         Answer the capabilities of the underlying ReasonerFactory.
    */
    @Override
    public Model getCapabilities()
        { return factory.getCapabilities(); }
    
    /**
         Answer the URI of the underlying ReasonerFactory. 
    */
    @Override
    public String getURI()
        { return factory.getURI(); }
    }
