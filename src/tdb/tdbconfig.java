/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lib.Tuple;
import tdb.cmdline.CmdSub;
import tdb.cmdline.CmdTDB;
import arq.cmdline.CmdARQ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

/** Tools to manage a TDB store.  Subcommand based. */
public class tdbconfig extends CmdSub
{
    static final String CMD_CLEAN   = "clean" ;
    static final String CMD_HELP    = "help" ;
    static final String CMD_STATS   = "stats" ;
    
    static public void main(String... argv)
    {
        new tdbconfig(argv).exec();
    }

    protected tdbconfig(String[] argv)
    {
        super(argv) ;
//        super.addSubCommand(CMD_CLEAN, new Exec()
//          { @Override public void exec(String[] argv) { new tdbclean(argv).main() ; } }) ;
        super.addSubCommand(CMD_HELP, new Exec()
          { @Override public void exec(String[] argv) { new SubHelp(argv).main() ; } }) ;
        super.addSubCommand(CMD_STATS, new Exec()
        { @Override public void exec(String[] argv) { new SubStats(argv).main() ; } }) ;
    }
    
    
    // Subcommand : help
    static class SubHelp extends CmdARQ
    {
        public SubHelp(String ... argv)
        {
            super(argv) ;
            //super.addModule(modSymbol) ;
        }
        
        @Override
        protected String getSummary()
        {
            return null ;
        }

        @Override
        protected void exec()
        {
            System.out.println("Help!") ;
        }

        @Override
        protected String getCommandName()
        {
            return "help" ;
        }
    }
    
    static class SubStats extends CmdTDB
    {
        public SubStats(String ... argv)
        {
            super(argv) ;
            //super.addModule(modSymbol) ;
        }
        
        @Override
        protected String getSummary()
        {
            return null ;
        }

        @Override
        protected void exec()
        {
            long count = 0 ;
            Map<NodeId, Integer> predicates = new HashMap<NodeId, Integer>(10000) ;
            PGraphBase graph = getGraph() ;
            TripleIndex primary = graph.getIndexSPO() ;
            Iterator<Tuple<NodeId>> iter = primary.all() ;
            for ( ; iter.hasNext() ; )
            {
                NodeId p  = iter.next().get(1) ; // 0,1,2
                count++ ;
                Integer n = predicates.get(p) ;
                if ( n == null )
                    predicates.put(p,1) ;
                else
                    predicates.put(p, n+1) ;
            }
            // Now print.
            System.out.printf("# Count: %d\n", count) ;
            for ( NodeId n : predicates.keySet() )
            {
                Node p = graph.getNodeTable().retrieveNode(n) ;
                System.out.printf("%s : %d\n", p.getURI(), predicates.get(n)) ;
            }
        }

        @Override
        protected String getCommandName()
        {
            return "stats" ;
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