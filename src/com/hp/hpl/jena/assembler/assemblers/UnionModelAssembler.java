/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: UnionModelAssembler.java,v 1.4 2006-01-13 08:37:59 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

public class UnionModelAssembler extends ModelAssembler implements Assembler
    {
    private static final Graph immutable = new GraphBase() 
        {
        protected ExtendedIterator graphBaseFind( TripleMatch m )
            { return NullIterator.instance; }
        };
    
    protected Model openModel( Assembler a, Resource root, Mode mode )
        {
        checkType( root, JA.UnionModel );
        MultiUnion union = new MultiUnion();
        union.addGraph( getRootModel( a, root, mode ) );
        addSubModels( a, root, union, mode );
        return ModelFactory.createModelForGraph( union );
        }

    private Graph getRootModel( Assembler a, Resource root, Mode mode )
        {
        Resource r = getUniqueResource( root, JA.rootModel );
        return r == null ? immutable : a.openModel( r, mode ).getGraph();
        }

    private void addSubModels( Assembler a, Resource root, MultiUnion union, Mode mode )
        {
        for (StmtIterator it = root.listProperties( JA.subModel ); it.hasNext();)
            {
            Resource resource = getResource( it.nextStatement() );
            union.addGraph( a.openModel( resource, mode ).getGraph() );        
            }
        }

    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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