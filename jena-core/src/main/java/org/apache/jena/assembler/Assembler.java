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

package org.apache.jena.assembler;

import org.apache.jena.assembler.assemblers.* ;
import org.apache.jena.rdf.model.* ;

/**
    An Assembler creates objects from their RDF descriptions. The root motivation
    is to create Models, but other objects are required as sub-components of
    those Models, so a general mechanism is available.

    @apiNote Methods accepting {@link Model} as a parameter treat appropriate values of {@code Model} as passing down
    to sub-assemblers a requirement to share for certain setups that work as tightly coordinated groups. It's a hint,
    not an instruction.

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

    // This slightly bizarre way to initial "constants" is because Assembler is
    // an interface. Interfaces inside class intialization processes do not have
    // their computer static fields initialized yet. To keep that, for
    // compatibility, initialization code must use the ConstAssembler functions
    // and not use constants here.  Assembler.general is the key field used
    // in ARQ.init (AssemblerUtils) and TDB.init (VocabTDB).

    public static final Assembler defaultModel = ConstAssembler.defaultModel() ;

    public static final Assembler memoryModel = ConstAssembler.memoryModel() ;

    public static final Assembler infModel = ConstAssembler.infModel();

    public static final Assembler ontModel = ConstAssembler.ontModel();

    public static final Assembler reasonerFactory = ConstAssembler.reasonerFactory();

    public static final Assembler content = ConstAssembler.content();

    public static final Assembler prefixMapping = ConstAssembler.prefixMapping();

    public static final Assembler unionModel = ConstAssembler.unionModel();

    public static final Assembler ontModelSpec = ConstAssembler.ontModelSpec();

    public static final Assembler ruleSet = ConstAssembler.ruleSet();

    public static final Assembler documentManager = ConstAssembler.documentManager();

    public static final AssemblerGroup general = ConstAssembler.general() ;
    }
