/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store.bulkloader2;

import java.util.Iterator ;

import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** Copy one index to another, probably with a different key order */
public class CmdIndexDump
{
    static { Log.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger(CmdIndexDump.class) ;
    
    static long tickQuantum = 100*1000 ;
    static int superTick = 10 ;
    
    public static void main(String...argv)
    {
        
        //argv = new String[]{"tmp/DB-100M", "SPO" }; 
        
        // Ideas:
        // Copy to buffer, sort, write in sequential clumps.
        // Profile code for hotspots
        
        // Maybe be worth opening the data file (the leaves) as a regular, 
        // non-memory mapped file as we read it through once, in natural order,
        // and it may be laid out in increasing block order on-disk, e.g. repacked
        // and in increasing order with occassional oddities if SPO from the bulk loader.
        
        if ( argv.length != 2 )
        {
            System.err.println("Usage: Location Index") ;
            System.exit(1) ;
        }
        
        String locationStr1 = argv[0] ;
        String indexName1 = argv[1] ;
        
        // Argument processing
        
        Location location1 = new Location(locationStr1) ;
        
        int keyLength = SystemTDB.SizeOfNodeId * indexName1.length() ;
        int valueLength = 0 ;
        
        // The name is the order.
        String primary = indexName1 ;  
        
        // Scope for optimization:
        // Null column map => no churn.
        // Do record -> record copy, not Tuple, Tuple copy.
        
        if ( true )
        {
            TupleIndex index1 = IndexFactory.openTupleIndex(location1, indexName1, primary, primary, 10, 10, keyLength, valueLength) ;
            dump(index1) ;
            index1.close() ;
        }
        else
        {
            // BPT -> BPT copy with reordering (hardcoded)
            RangeIndex index1 = IndexFactory.openBPT(location1, indexName1, 10, 10, keyLength, valueLength) ;  
            // Unlikely to be metafile for destination.
            keyLength = index1.getRecordFactory().keyLength() ;
            valueLength = index1.getRecordFactory().valueLength() ;
            dump(index1) ;
            index1.close() ;
        }
    }

    
    // -------------------

    private static void dump(TupleIndex index)
    {
        int rowBlock = 1000 ;
        // Spaces between, newline at end.
//        int rowLength = 16*index.getTupleLength()+index.getTupleLength() ;
//
//        byte b[] = new byte[rowBlock*rowLength] ;
//
//        int rows = 0 ;
//        int idx = 0 ;

        
        WriteRows writer = new WriteRows(System.out, index.getTupleLength(), rowBlock) ;    
        Iterator<Tuple<NodeId>> iter = index.all() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next() ;
            //System.out.println(tuple) ;
            boolean first = true ;

            for ( NodeId n : tuple.tuple() )
            {
                writer.write(n.getId()) ;
            }
            writer.endOfRow() ;

        }
        writer.close() ;
    }

    private static void dump(RangeIndex index)
    {
        Iterator<Record> iter = index.iterator() ;
        for ( ; iter.hasNext() ; )
        {
            Record record = iter.next() ;
            System.out.println(record) ;
        }

    }

    static class Hex
    {
        // No checking.
        public static int formatUnsignedLongHex(byte[] b, int start, long x, int width)
        {
            // Insert from low value end to high value end.
            int idx = start+width-1 ;
            
            while ( width > 0 )
            {
                int d = (int)(x & 0xF) ;
                x = x>>4 ;
                byte ch = Bytes.hexDigits[d] ; 
                b[idx] = ch ;
                width-- ;
                idx-- ;

                if ( x == 0 )
                    break ;
            }

            if ( x != 0 )
                throw new AtlasException("formatUnsignedLongHex: overflow") ;

            while ( width > 0 )
            {
                b[idx] = '0' ;
                idx-- ;
                width-- ;
            }
            return width ;
        }
    }
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