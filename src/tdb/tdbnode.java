/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.NodeTable;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;

import arq.cmd.CmdUtils;
import tdb.cmdline.CmdTDB;

public class tdbnode extends CmdTDB
{
    // Debugging tool.
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbnode(argv).main() ;
    }

    protected tdbnode(String[] argv)
    {
        super(argv) ;
    }

    @Override
    protected String getSummary()
    {
        return getCommandName()+" NodeId ..." ;
    }

    @Override
    protected void exec()
    {
        GraphTDB graph = getGraph() ;
        NodeTable nodeTable = graph.getNodeTable() ;
        @SuppressWarnings("unchecked")
        Iterator<String> iter = (Iterator<String>)super.getPositional().iterator() ;
        if ( ! iter.hasNext() )
        {
            System.err.println("No node ids") ;
            return ;
        }
        
        for ( ; iter.hasNext() ; )
        {
            String id = iter.next() ;
            try {
                long x = Long.parseLong(id) ;
                NodeId nodeId = new NodeId(x) ;
                Node n = nodeTable.retrieveNodeByNodeId(nodeId) ;
                System.out.printf("%s [%d] => %s\n", id, x, n) ;
            } catch (Exception ex)
            {
                System.out.println("Failed to decode: "+id) ;
            }
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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