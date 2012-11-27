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

package com.hp.hpl.jena.tdb.store.bulkloader2;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.PrintStream ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.Hex ;

import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

final
public class RecordsFromInput implements Iterator<Record> 
{
    private final InputStream input ;
    private Record slot = null ;
    private boolean finished = false ;
    private final byte[] buffer ;
    private int len = -1 ;
    private int idx ;       // Where in buffer.
    private final int rowLength ;
    private final int rowBlockSize ;
    private final RecordFactory recordFactory ;
    private final int itemsPerRow ;
    private final ColumnMap colMap ;

    public RecordsFromInput(InputStream input, int itemsPerRow, ColumnMap colMap, int rowBlockSize)
    { 
        this.input = input ;
        this.itemsPerRow = itemsPerRow ;
        this.colMap = colMap ;
        this.rowLength = itemsPerRow*16 + itemsPerRow ;   // Length in bytes of a row.
        this.rowBlockSize = rowBlockSize ; 
        this.buffer = new byte[rowLength*rowBlockSize] ;
        this.idx = -1 ;
        this.recordFactory = new RecordFactory(itemsPerRow*SystemTDB.SizeOfNodeId, 0) ;
    }
    
    @Override
    public boolean hasNext()
    {
        if ( finished ) return false ;
        if ( slot != null )
            return true ;
        if ( idx == -1 || idx == buffer.length )
        {
            len = fill() ;
            if ( len == -1 )
            {
                finished = true ;
                return false ;
            }
            idx = 0 ;
        }

        // Fill one slot.
        Record record = recordFactory.create() ;
        
//        System.out.print("In:  ") ;
        for ( int i = 0 ; i < itemsPerRow ; i++ )
        {
            long x = Hex.getLong(buffer, idx) ;
            idx += 16 ;
            // Separator or end-of-line.
            idx++ ;     
            int j = ( colMap == null ) ? i : colMap.mapSlotIdx(i) ; 
            int recordOffset = j*SystemTDB.SizeOfLong ;
            Bytes.setLong(x, record.getKey(), recordOffset) ;
            
//            System.out.printf("%016X ", x) ;
            
        }
//        System.out.println() ;
//        System.out.print("Out: ") ;
//        printRecord(System.out, record, itemsPerRow) ;
        // Buffer all processed. 
        if ( idx >= len ) 
            idx = -1 ;
        
        slot = record ;
        return true ;
    }

    private static void printRecord(PrintStream out, Record r, int keyUnitLen)
    {
        int keySubLen = r.getKey().length/keyUnitLen ;
        for ( int i = 0 ; i < keyUnitLen ; i++ )
        {   
            if ( i != 0 )
                out.print(" ") ;
            
            // Print in chunks
            int k = i*keySubLen ;
            for ( int j = k ; j < k+keySubLen ; j++ )
                out.printf("%02x", r.getKey()[j]) ;
            
//            long x = Bytes.getLong(r.getKey(), i*SystemTDB.SizeOfNodeId) ;
//            System.out.printf("%016x", x) ;
        }
        out.println() ;
    }
    
    private int fill()
    {
        try {
            int len = 0 ;
            while ( len < buffer.length ) {
                int count = input.read(buffer, len, buffer.length - len) ;
                if ( count == -1 ) break ;
                len += count ;
            }
            if ( len == 0 ) return -1 ;
            if ( len%rowLength != 0 )
                throw new AtlasException("Wrong length: "+len) ;
            return len ;
        } catch (IOException ex) { throw new AtlasException(ex) ; }
    }

    @Override
    public Record next()
    {
        if ( !hasNext() ) throw new NoSuchElementException() ;
        Record r = slot ;
        slot = null ;
        return r ;
    }

    @Override
    public void remove()
    { throw new UnsupportedOperationException() ; }
}
