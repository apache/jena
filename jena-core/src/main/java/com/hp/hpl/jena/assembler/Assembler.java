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

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    An Assembler creates objects from their RDF descriptions. The root motivation
    is to create Models, but other objects are required as sub-components of
    those Models, so a general mechanism is available.
*/
public interface Assembler
    {
    /**
        The core operation: answer a new object constructed according to the
        object description hanging from <code>root</code>, using the assembler
        <code>a</code> for any sub-objects. Use <code>mode</code> to decide
        if persistent objects are to be re-used or created; this mode is passed down
        to all sub-object construction.
    */
    public Object open( Assembler a, Resource root, Mode mode );
    
    /**
        Answer <code>open( a, root, Mode.DEFAULT )</code>.
    */
    public Object open( Assembler a, Resource root );
    
    /**
        Answer <code>open( this, root, Mode.DEFAULT )</code>.
    */
    public Object open( Resource root );

    /**
        Answer <code>(Model) open( this, root, Mode.DEFAULT )</code>, unless
        the result cannot be or is not a Model, in which case throw an exception.
    */
    public Model openModel( Resource root );

    /**
        Answer <code>(Model) open( this, root, mode )</code>, unless
        the result cannot be or is not a Model, in which case throw an exception.
    */
    public Model openModel( Resource root, Mode mode );
    
    public static final Assembler defaultModel = new DefaultModelAssembler();
    
    public static final Assembler memoryModel = new MemoryModelAssembler();

    public static final Assembler infModel = new InfModelAssembler();

    public static final Assembler ontModel = new OntModelAssembler();

    public static final Assembler reasonerFactory = new ReasonerFactoryAssembler();

    public static final Assembler content = new ContentAssembler();
    
    public static final Assembler prefixMapping = new PrefixMappingAssembler();

    public static final Assembler fileModel = new FileModelAssembler();

    public static final Assembler unionModel = new UnionModelAssembler();

    public static final Assembler ontModelSpec = new OntModelSpecAssembler();
    
    public static final Assembler ruleSet = new RuleSetAssembler();
    
    public static final Assembler modelSource = new ModelSourceAssembler();
    
    public static final Assembler locationMapper = new LocationMapperAssembler();

    public static final Assembler fileManager = new FileManagerAssembler();

    public static final Assembler documentManager = new DocumentManagerAssembler();
    
    public static final AssemblerGroup general = AssemblerGroup.create()
        .implementWith( JA.DefaultModel, defaultModel )
        .implementWith( JA.MemoryModel, memoryModel )
        .implementWith( JA.InfModel, infModel )
        .implementWith( JA.ReasonerFactory, reasonerFactory )
        .implementWith( JA.ModelSource, modelSource )
        .implementWith( JA.Content, content )
        .implementWith( JA.ContentItem, content )
        .implementWith( JA.UnionModel, unionModel )
        .implementWith( JA.PrefixMapping, prefixMapping )
        .implementWith( JA.SinglePrefixMapping, prefixMapping )
        .implementWith( JA.FileModel, fileModel )
        .implementWith( JA.OntModel, ontModel )
        .implementWith( JA.OntModelSpec, ontModelSpec )
        .implementWith( JA.RuleSet, ruleSet )
        .implementWith( JA.LocationMapper, locationMapper )
        .implementWith( JA.FileManager, fileManager )
        .implementWith( JA.DocumentManager, documentManager )
        ;
    }
