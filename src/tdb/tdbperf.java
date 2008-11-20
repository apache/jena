/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import static com.hp.hpl.jena.tdb.sys.Names.tripleIndexes;

import java.util.ArrayList;
import java.util.List;

import tdb.cmdline.CmdSub;
import tdb.cmdline.CmdTDB;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModVersion;

import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation;
import com.hp.hpl.jena.tdb.store.*;

/** Tools to test performance.  Subcommand based. */
public class tdbperf extends CmdSub
{
    static final String CMD_LOAD    = "load" ;
    static final String CMD_HELP    = "help" ;
    static final String CMD_INFO    = "info" ;
    
    static public void main(String... argv)
    {
        new tdbperf(argv).exec();
    }

    protected tdbperf(String[] argv)
    {
        super(argv) ;

        super.addSubCommand(CMD_LOAD, new Exec()
          { @Override public void exec(String[] argv) { new SubLoad(argv).exec() ; } }) ;
        
        super.addSubCommand(CMD_HELP, new Exec()
        { @Override public void exec(String[] argv) { new SubHelp(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_INFO, new Exec()
        { @Override public void exec(String[] argv) { new SubInfo(argv).mainRun() ; } }) ;

        
    }
    
    static class SubLoad //extends CmdTDB
    {
        public SubLoad(String... argv)
        {
            //super(argv) ;
        }

        protected void exec()
        {
            TDB.init();
            GraphTDB g = setup() ;
            BulkLoader b = new BulkLoader(g, true) ;
            List<String> files = new ArrayList<String>() ;
            files.add("/home/afs/Datasets/MusicBrainz/artists.nt") ;
            b.load(files) ;
            System.exit(0) ;
        }
            
        private static GraphTDB setup()
        {
            // Setup a graph - for experimental alternatives.
            BlockMgrMem.SafeMode = true ;
            IndexBuilder indexBuilder = IndexBuilder.mem() ;
            Location location = null ;

            NodeTable nodeTable = NodeTableFactory.create(indexBuilder, location) ;

            TripleTable table = FactoryGraphTDB.createTripleTable(indexBuilder, nodeTable, location, tripleIndexes) ; 
            ReorderTransformation transform = ReorderLib.identity() ;
            GraphTDB g = new GraphTriplesTDB(table, transform, location) ;
            return g ;
        } 
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
            return "tdbperf help" ;
        }
    }
    
    static class SubInfo extends CmdTDB
    {
        public SubInfo(String ... argv)
        {
            super(argv) ;
        }
        
        @Override
        protected String getSummary()
        {
            return "tdbperf info" ;
        }

        @Override
        protected void exec()
        {
            System.out.println("-- "+Utils.nowAsString()+" --") ;
            ModVersion v = new ModVersion(true) ;
            v.addClass(TDB.class) ;
            v.printVersionAndExit() ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbperf info" ;
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