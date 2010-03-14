/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.exactlyOneProperty;
import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pDescription;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pFile;
import org.openjena.atlas.lib.ColumnMap ;


import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class IndexAssembler extends AssemblerBase //implements Assembler
{
    /* 
     * [ :description "SPO" ; :file "SPO.idx" ]
     */
    
    private Location location = null ;
    private IndexAssembler()                   { this.location = null ; }
    private IndexAssembler(Location location)  { this.location = location ; }
    
    @Override
    public TupleIndex open(Assembler a, Resource root, Mode mode)
    {
        exactlyOneProperty(root, pDescription) ;
        String desc = getAsStringValue(root, pDescription).toUpperCase() ;
        exactlyOneProperty(root, pFile) ;
        String filename = getAsStringValue(root, pFile) ;
        
        // Need to get location from the enclosing PGraphAssembler
        if ( location != null )
            filename = location.absolute(filename) ;
        
        String primary = null ;
        RecordFactory rf = null ;
        
        switch ( desc.length() )
        {
            case 3:
                primary = Names.primaryIndexTriples ;
                rf = SystemTDB.indexRecordTripleFactory ;
                break ;
            case 4:
                primary = Names.primaryIndexQuads;
                rf = SystemTDB.indexRecordQuadFactory ;
                break ;
            default:
                throw new TDBException("Bad length for index description: "+desc) ;
                
        }
        // Problems with spotting the index technology.
        FileSet fileset = null ; //FileSet.fromFilename(filename) ;
        
        RangeIndex rIndex = IndexBuilder.createRangeIndex(fileset, rf) ;
        return new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), rf, rIndex) ;
    }

//    public static RangeIndex rangeIndex(String filename, String name)
//    {
//     // Problems with spotting the index technology.
//        FileSet fileset = IndexBuilder.filesetForIndex(new Location(filename), desc) ;
//        return IndexBuilder.createRangeIndex(new Location(filename), name, FactoryGraphTDB.indexRecordTripleFactory) ;
//    }

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