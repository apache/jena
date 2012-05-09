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
        
    @Override
    public void addTriple(Triple triple)
    { loader.load(row(triple)) ; }

    @Override
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

    @Override
    public void close()
    { loader.finish() ; }

    @Override
    public void startBulkUpdate()
    { loader.start() ; }

    @Override
    public void finishBulkUpdate()
    { loader.finish() ; }

    @Override
    public int getChunkSize()
    { throw new SDBNotImplemented("TupleGraphLoader.getChunkSize") ; }
    
    @Override
    public void setChunkSize(int chunks)
    { throw new SDBNotImplemented("TupleGraphLoader.setChunkSize") ; }

    @Override
    public boolean getUseThreading()
    { throw new SDBNotImplemented("TupleGraphLoader.getUseThreading") ; }

    @Override
    public void setUseThreading(boolean useThreading)
    { throw new SDBNotImplemented("TupleGraphLoader.setUseThreading") ; }
    
	@Override
    public void deleteAll()
	{ throw new SDBNotImplemented("TupleGraphLoader.deleteAll"); }
}
