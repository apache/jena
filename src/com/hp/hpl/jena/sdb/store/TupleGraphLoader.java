/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;
//import java.lang.String.format

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;

/** Adapter from a tuple loader to a graph loader.*/ 
public class TupleGraphLoader implements StoreLoader
{
    private TupleLoader loader ;

    /** The loader must be for a triple table of some kind */
    public TupleGraphLoader(TupleLoader loader)
    { 
        if ( loader.getTableDesc() == null )
            throw new SDBInternalError("No table description for loader") ;
        if ( loader.getTableDesc().getWidth() != 3 ) 
        {
            String x = String.format("Table description width is %d, not 3",
                                     loader.getTableDesc().getWidth()) ;
            throw new SDBInternalError(x) ;
        }
        this.loader = loader ;
    }
        
    public void addTriple(Triple triple)
    { loader.load(row(triple)) ; }

    public void deleteTriple(Triple triple)
    { loader.unload(row(triple)) ; }
    
    private static Node[] row(Triple triple)
    {
        Node[] nodes = new Node[3] ;
        nodes[0] = triple.getSubject() ;
        nodes[1] = triple.getPredicate() ;
        nodes[2] = triple.getObject() ;
        return nodes ;
    }

    public void close()
    { loader.finish() ; }

    public void startBulkUpdate()
    { loader.start() ; }

    public void finishBulkUpdate()
    { loader.finish() ; }

    public int getChunkSize()
    { throw new SDBNotImplemented("TupleGraphLoader.getChunkSize") ; }
    
    public void setChunkSize(int chunks)
    { throw new SDBNotImplemented("TupleGraphLoader.setChunkSize") ; }

    public boolean getUseThreading()
    { throw new SDBNotImplemented("TupleGraphLoader.getUseThreading") ; }

    public void setUseThreading(boolean useThreading)
    { throw new SDBNotImplemented("TupleGraphLoader.setUseThreading") ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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