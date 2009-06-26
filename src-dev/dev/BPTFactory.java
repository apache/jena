/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.MetaFile;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;

/** Make B+Trees */
public class BPTFactory
{
    private static final Logger log = LoggerFactory.getLogger(BPTFactory.class) ;

    // Sort out with IndexBuilder and ...tdb.index.factories.* when ready.

    public static RangeIndex create(FileSet fileset, int order, int blockSize, RecordFactory factory)
    {
        if ( blockSize < 0 && order < 0 )
            throw new IllegalArgumentException("Nother blocksize nor order specificied") ;
        if ( blockSize >= 0 && order < 0 )
            order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if ( blockSize >= 0 && order >= 0 )
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if ( order != order2 )
                throw new IllegalArgumentException("Wrong order ("+order+"), calculated = "+order2) ;
        }
        
        // Iffy
        if ( blockSize < 0 && order >= 0 )
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        
        MetaFile mf = fileset.getMetaFile() ;
        if ( mf == null )
            mf = fileset.getLocation().getMetaFile() ;
        
        BPlusTreeParams params = null ;
        // Params from previous settings
        if ( mf.existsMetaData() )
        {
            // Put block size in BPTParams?
            log.debug("Reading metadata ...") ;
            BPlusTreeParams params2 = BPlusTreeParams.readMeta(fileset) ;

            int blkSize2 = mf.getPropertyAsInteger(BPlusTreeParams.ParamBlockSize) ;
            log.info(String.format("Block size -- %d, given %d", blkSize2, blockSize)) ;
            log.info("Read: "+params2.toString()) ;

            if ( blkSize2 != blockSize )
                log.error(String.format("Metadata declares block size to be %d, not %d", blkSize2, blockSize)) ;  
            // params = ...;
            // Check.
            params = params2 ;
        }
        else
        {
            params = new BPlusTreeParams(order, factory) ;
            mf.setProperty(BPlusTreeParams.ParamBlockSize, blockSize) ;
            params.addToMetaData(fileset) ;
            mf.flush();
        }

        log.info("Params: "+params) ;
        
        //MetaFile metafile = fileset.getMetaFile() ;
        return null ;
    }
}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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