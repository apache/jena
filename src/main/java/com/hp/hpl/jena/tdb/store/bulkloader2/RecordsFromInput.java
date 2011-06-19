/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.PrintStream ;
import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.Hex ;

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
            int len = input.read(buffer) ;
            if ( len == -1 ) return -1 ;
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
/*
 * (c) Copyright 2010 Epimorphics Ltd.
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