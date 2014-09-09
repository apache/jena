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

package tdb;


import java.util.List ;
import java.util.Map ;

import org.apache.jena.atlas.io.IndentedWriter ;
import tdb.cmdline.CmdSub ;
import tdb.cmdline.CmdTDB ;
import tdb.cmdline.CmdTDBGraph ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModVersion ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;
import com.hp.hpl.jena.tdb.setup.Build ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsResults ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.DatasetControlNone ;

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
        CmdTDB.init() ;
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
            DatasetPrefixStorage prefixes = Build.makePrefixes(location, new DatasetControlNone()) ;
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
    
    static class SubStats extends CmdTDBGraph
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
            DatasetGraphTDB dsg = getDatasetGraphTDB() ;
            Node gn = getGraphName() ;
            StatsResults results = tdbstats.stats(dsg, gn) ;
            Stats.write(System.out, results) ;
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
