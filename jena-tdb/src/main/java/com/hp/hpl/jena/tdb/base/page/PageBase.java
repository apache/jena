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

package com.hp.hpl.jena.tdb.base.page;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.Block ;

/** A page with a byte buffer */
public abstract class PageBase implements Page
{
    private final int id ;
    private Block block ;

    protected PageBase(Block block)
    {
        this.block = block ;
        long x = block.getId() ;
        if ( x < 0 )
            throw new TDBException("Page id is negative: "+x) ;
        if ( x > Integer.MAX_VALUE )
            throw new TDBException("Page id is large than MAX_INT: "+x) ;
        this.id = block.getId().intValue() ;
    }
    
    @Override
    final public void reset(Block block2)
    { 
        if ( block2.getId() != id )
            Log.fatal(this, "Block id changed: "+id+" => "+block2.getId()) ;
        _reset(block2) ; 
        this.block = block2 ;
    } 

    protected abstract void _reset(Block block) ;

    @Override
    final public Block getBackingBlock()    { return block ; }

    @Override
    final public int getId()                { return id ; }
}
