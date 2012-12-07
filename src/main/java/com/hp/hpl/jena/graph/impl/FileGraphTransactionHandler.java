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

package com.hp.hpl.jena.graph.impl;

import java.io.File;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.shared.JenaException;

/**
    A TransactionHandler for FileGraphs. When a FileGraph begin()s a 
    transaction, its current contents are checkpointed into a sibling file
    (whose name starts "checkPoint-"). On an abort(), the current contents 
    are discarded, and the checkpointed contents restored; on a commit(),
    the current contents are written back to the backing file, and the
    checkpoint file is deleted. Nested transactions are Not Allowed.

*/
public class FileGraphTransactionHandler 
    extends TransactionHandlerBase implements TransactionHandler
    {
    protected boolean inTransaction;
    protected FileGraph fileGraph;
    protected File checkPointFile;
    
    public FileGraphTransactionHandler( FileGraph fileGraph )
        { this.fileGraph = fileGraph; }
    
    @Override
    public boolean transactionsSupported()
        { return true; }
    
    @Override
    public void begin()
        { if (inTransaction) 
            throw new JenaException( "nested transactions not supported" );
        else 
            { checkPointFile = new File( checkPointName( fileGraph.name ) ); 
            checkPointFile.deleteOnExit();
            fileGraph.saveContents( checkPointFile );
            inTransaction = true; } }
    
    protected String checkPointName( File name )
        {
        String path = name.getPath();
        int pos = path.lastIndexOf( File.separatorChar );
        String start = path.substring( 0, pos + 1 );
        String finish = path.substring( pos + 1 );
        return start + "checkPoint-" + finish;
        }
    
    @Override
    public void abort()
        { fileGraph.clear();
        fileGraph.readModelFrom( fileGraph.model, true, checkPointFile );
        checkPointFile.delete();
        inTransaction = false; }
    
    @Override
    public void commit()
        { fileGraph.saveContents( fileGraph.name ); 
        checkPointFile.delete(); 
        inTransaction = false; }
    
    }
