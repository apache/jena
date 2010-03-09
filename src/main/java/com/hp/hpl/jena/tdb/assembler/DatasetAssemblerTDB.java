/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.exactlyOneProperty;
import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getStringValue;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pLocation;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;

import com.hp.hpl.jena.sparql.core.assembler.DatasetAssembler;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class DatasetAssemblerTDB extends DatasetAssembler
{
    static { TDB.init(); }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        TDB.init() ;
        return make(root) ;
    }

    static Dataset make(Resource root)
    {
        if ( ! exactlyOneProperty(root, pLocation) )
            throw new AssemblerException(root, "No location given") ;

        String dir = getStringValue(root, pLocation) ;
        Location loc = new Location(dir) ;
        return TDBFactory.createDataset(loc) ;
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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