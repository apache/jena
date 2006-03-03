/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: Assembler.java,v 1.5 2006-03-03 13:45:55 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.assembler.assemblers.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    An Assembler creates objects from their RDF descriptions. The root motivation
    is to create Models, but other objects are required as sub-components of
    those Models, so a general mechanism is available.
    
    @author kers
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

    public static final Assembler rdbModel = new RDBModelAssembler();
    
    public static final Assembler ontModel = new OntModelAssembler();

    public static final Assembler reasonerFactory = new ReasonerFactoryAssembler();

    public static final Assembler content = new ContentAssembler();
    
    public static final Assembler connection = new ConnectionAssembler();

    public static final Assembler prefixMapping = new PrefixMappingAssembler();

    public static final Assembler fileModel = new FileModelAssembler();

    public static final Assembler unionModel = new UnionModelAssembler();

    public static final Assembler ontModelSpec = new OntModelSpecAssembler();
    
    public static final Assembler ruleSet = new RuleSetAssembler();
    
    public static final Assembler locationMapper = new LocationMapperAssembler();

    public static final Assembler fileManager = new FileManagerAssembler();

    public static final Assembler documentManager = new DocumentManagerAssembler();
    
    public static final AssemblerGroup general = AssemblerGroup.create()
        .implementWith( JA.DefaultModel, defaultModel )
        .implementWith( JA.MemoryModel, memoryModel )
        .implementWith( JA.InfModel, infModel )
        .implementWith( JA.ReasonerFactory, reasonerFactory )
        .implementWith( JA.Content, content )
        .implementWith( JA.ContentItem, content )
        .implementWith( JA.Connection, connection )
        .implementWith( JA.RDBModel, rdbModel )
        .implementWith( JA.UnionModel, unionModel )
        .implementWith( JA.PrefixMapping, prefixMapping )
        .implementWith( JA.FileModel, fileModel )
        .implementWith( JA.OntModel, ontModel )
        .implementWith( JA.OntModelSpec, ontModelSpec )
        .implementWith( JA.RuleSet, ruleSet )
        .implementWith( JA.LocationMapper, locationMapper )
        .implementWith( JA.FileManager, fileManager )
        .implementWith( JA.DocumentManager, documentManager )
        ;
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/