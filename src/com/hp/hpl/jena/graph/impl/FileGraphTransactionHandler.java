/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	[See end of file]
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
    
    @author kers
*/
public class FileGraphTransactionHandler 
    extends TransactionHandlerBase implements TransactionHandler
    {
    protected boolean inTransaction;
    protected FileGraph fileGraph;
    protected File checkPointFile;
    
    public FileGraphTransactionHandler( FileGraph fileGraph )
        { this.fileGraph = fileGraph; }
    
    public boolean transactionsSupported()
        { return true; }
    
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
    
    public void abort()
        { fileGraph.getBulkUpdateHandler().removeAll();
        fileGraph.readModelFrom( fileGraph.model, true, checkPointFile );
        checkPointFile.delete();
        inTransaction = false; }
    
    public void commit()
        { fileGraph.saveContents( fileGraph.name ); 
        checkPointFile.delete(); 
        inTransaction = false; }
    
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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