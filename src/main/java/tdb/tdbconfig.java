/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;


import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.io.IndentedWriter ;
import tdb.cmdline.CmdSub ;
import tdb.cmdline.CmdTDB ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModVersion ;

import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.sys.DatasetControlNone ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;

/** Tools to manage a TDB store.  Subcommand based. */
public class tdbconfig extends CmdSub
{
    static final String CMD_CLEAN       = "clean" ;
    static final String CMD_HELP        = "help" ;
    static final String CMD_STATS       = "stats" ;
    static final String CMD_NODES       = "nodes" ;
    static final String CMD_INFO        = "info" ;
    static final String CMD_PREFIXES    = "prefixes" ;
    
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
        { /*@Override*/ @Override
        public void exec(String[] argv) { new SubHelp(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_STATS, new Exec()
        { /*@Override*/ @Override
        public void exec(String[] argv) { new SubStats(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_NODES, new Exec()
        { /*@Override*/ @Override
        public void exec(String[] argv) { new SubNodes(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_INFO, new Exec()
        { /*@Override*/ @Override
        public void exec(String[] argv) { new SubInfo(argv).mainRun() ; } }) ;
        
        super.addSubCommand(CMD_PREFIXES, new Exec()
        { /*@Override*/ @Override
        public void exec(String[] argv) { new SubPrefixes(argv).mainRun() ; } }) ;

        
    }
    
    static class SubPrefixes extends CmdTDB
    {
        public SubPrefixes(String ... argv)
        {
            super(argv) ;
            //super.addModule(modSymbol) ;
        }

        @Override
        protected String getSummary()
        {
            return "tdbconfig prefixes" ;
        }

        @Override
        protected void exec()
        {
            Location location = getLocation() ;
            DatasetPrefixStorage prefixes = SetupTDB.makePrefixes(location, SetupTDB.globalConfig, new DatasetControlNone()) ;
            for ( String gn : prefixes.graphNames() )
            {
                System.out.println("Graph: "+gn) ;
                PrefixMapping pmap = prefixes.getPrefixMapping(gn) ;
                Map<String, String> x = pmap.getNsPrefixMap() ;
                for ( String k : x.keySet() )
                    System.out.printf("  %-10s %s\n", k+":", x.get(k)) ;
            }
        }
    }
    
    // Subcommand : help
    class SubHelp extends CmdARQ
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
            IndentedWriter out = IndentedWriter.stdout ;
            out.println("Sub-commands:") ;
            out.incIndent() ;

            for ( String name : subCommandNames() )
            {
                out.println(name) ; 
            }
            out.decIndent() ;
            out.flush() ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig help" ;
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
            return "tdbconfig stats" ;
        }

        @Override
        protected void exec()
        {
            GraphTDB graph = getGraph() ;
            StatsCollector stats = Stats.gatherTDB(graph) ;
            Stats.write(System.out, stats) ;
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig stats" ;
        }
    }
    
    static class SubNodes extends CmdTDB
    {
        public SubNodes(String ... argv)
        {
            super(argv) ;
        }
        
        @Override
        protected String getSummary()
        {
            return "tdbconfig nodes" ;
        }

        @Override
        protected void exec()
        {
            List<String> args = positionals ;
            for ( String x : args )
            {
                System.out.println("**** Object File: "+x) ;
                StringFile objs = FileFactory.createStringFileDisk(x) ;
                objs.dump() ;
            }
        }

        @Override
        protected String getCommandName()
        {
            return "tdbconfig nodes" ;
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
            return "tdbconfig info" ;
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
            return "tdbconfig info" ;
        }
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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