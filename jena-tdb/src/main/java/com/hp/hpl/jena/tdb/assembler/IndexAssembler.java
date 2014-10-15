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

package com.hp.hpl.jena.tdb.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.exactlyOneProperty ;
import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getAsStringValue ;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pDescription ;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pFile ;

import java.util.Locale ;

import org.apache.jena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.IndexFactory ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.sys.Names ;
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
        String desc = getAsStringValue(root, pDescription).toUpperCase(Locale.ENGLISH) ;
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
        IndexParams idxParams = StoreParams.getDftStoreParams() ;
        RangeIndex rIndex = IndexFactory.buildRangeIndex(fileset, rf, idxParams) ;
        return new TupleIndexRecord(desc.length(), new ColumnMap(primary, desc), desc, rf, rIndex) ;
    }
}
