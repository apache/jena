/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.trans.bplustree;

import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.Sync ;
import org.seaborne.dboe.base.file.BufferChannel ;
import org.seaborne.dboe.sys.SystemLz ;

/** Manage the persistent state of the tree*/
public class BPTRootMgr implements Sync {
    private int currentLatestRoot = 0 ;
    private final BufferChannel storage ;
    private final ByteBuffer bb = ByteBuffer.allocate(SystemLz.SizeOfInt) ;
    private boolean dirty = false ;
    
    // Often the BufferChannel of a TransBloc.
    public BPTRootMgr(BufferChannel storage) {
        this.storage = storage ;
        if ( ! storage.isEmpty() ) {
            bb.rewind() ;
            storage.read(bb, 0) ;
            bb.rewind() ;
            currentLatestRoot = bb.getInt() ;
        }
            
    }
    
    public BufferChannel getChannel() { return storage ; }
    
    void setRoot(int rootIdx) {
        currentLatestRoot = rootIdx ;
        bb.rewind() ;
        bb.putInt(rootIdx) ;
        storage.write(bb, 0) ;
        dirty = true ;
    }
    
    public int getRoot() {
        return currentLatestRoot ;
    }
    
    @Override
    public void sync() {
        if ( dirty )
            storage.sync() ;
        dirty = false ;
    }
}
