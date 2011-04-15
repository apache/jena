/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrWrapper ;

public class BlockMgrTracker extends BlockMgrWrapper // implements BlockMgr
{
    List<Integer> readGetIds = new ArrayList<Integer>() ;
    List<Integer> updateGetIds = new ArrayList<Integer>() ;
    List<Integer> readReleaseIds = new ArrayList<Integer>() ;
    List<Integer> updatePutIds = new ArrayList<Integer>() ;
    
    boolean inRead = false ;
    boolean inUpdate = false ;
    protected final Logger log ;
    

    public BlockMgrTracker(String label, BlockMgr blockMgr, boolean logUpdatesOnly)
    {
        //super(label, blockMgr, logUpdatesOnly) ;
        super(blockMgr) ;
        log = LoggerFactory.getLogger(label) ;
        
    }

    @Override
    public ByteBuffer get(int id)
    {
        if ( inRead )
            readGetIds.add(id) ;
        else if ( inUpdate )
            updateGetIds.add(id) ;
        else
            log.warn("No session active") ;
        
        return super.get(id) ;
    }

    @Override
    public void put(int id, ByteBuffer block)
    {
        updatePutIds.add(id) ;
        super.put(id, block) ;
    }

    @Override
    public void freeBlock(int id)
    {
        updatePutIds.add(id) ;
        log.info("Free buffer: "+id) ;
        super.freeBlock(id) ;
    }

    @Override
    public void startRead()
    {
        if ( inRead )
            log.warn("startRead when already in read") ;
        if ( inUpdate )
            log.warn("startRead when already in update") ;
        log.info("> start read") ;
        
        inRead = true ;
        super.startRead() ;
    }

    @Override
    public void finishRead()
    {
        if ( ! inRead )
            log.warn("finishRead but not in read") ;
        if ( inUpdate )
            log.warn("finishRead when in update") ;
        
        inRead = false ;

        log.info("Read end: Gets: "+readGetIds) ;
        log.info("Read end: Free: "+readReleaseIds) ;
        log.info("< finish read") ;
        super.finishRead() ;
    }

    @Override
    public void startUpdate()
    {
        if ( inRead )
            log.warn("startUpdate when already in read") ;
        if ( inUpdate )
            log.warn("startUpdate when already in update") ;
        inUpdate = true ;
        log.info("> start update") ;
        super.startUpdate() ;
    }

    @Override
    public void finishUpdate()
    {
        if ( ! inUpdate )
            log.warn("finishUpdate but not in update") ;
        if ( inRead )
            log.warn("finishUpdate when in update") ;
        inUpdate = false ;

        log.info("Update end: Gets: "+updateGetIds) ;
        log.info("Update end: Puts: "+updatePutIds) ;
        
        updateGetIds.clear() ;
        updatePutIds.clear() ;
        log.info("< finish update") ;
        super.finishUpdate() ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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