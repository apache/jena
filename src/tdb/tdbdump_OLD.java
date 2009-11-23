/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.List ;

import tdb.cmdline.CmdSub ;
import tdb.cmdline.CmdTDB ;
import tdb.cmdline.ModFormat ;
import arq.cmd.CmdException ;
import atlas.logging.Log ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.objectfile.StringFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class tdbdump_OLD extends CmdSub
{
    ModFormat modFormat =  new ModFormat() ;
    
    static public void main(String... argv)
    { 
        Log.setLog4j() ;
        new tdbdump_OLD(argv).exec() ;
    }

    static final String CMD_DATA =      "data" ; 
    static final String CMD_INDEX =     "index" ; 
    static final String CMD_NODES =     "nodes" ;
    
    protected tdbdump_OLD(String...argv)
    {
        super(argv) ;
        super.addSubCommand(CMD_INDEX, new Exec()
        { /*@Override*/ public void exec(String[] argv) { new SubIndex(argv).mainRun() ; } }) ;
        super.addSubCommand(CMD_DATA, new Exec()
        { /*@Override*/ public void exec(String[] argv) { new SubData(argv).mainRun() ; } }) ;
        super.addSubCommand(CMD_NODES, new Exec()
        { /*@Override*/ public void exec(String[] argv) { new SubNodes(argv).mainRun() ; } }) ;
    }


    class SubData extends CmdTDB
    {
        protected SubData(String... argv)
        {
            super(argv) ;
        }

        @Override
        protected String getSummary()
        {
            return null ;
        }

        @Override
        protected void exec()
        {
            Model model = getModel() ;
            String format = modFormat.getFormat("N3-TRIPLES") ;
            model.write(System.out, format) ;
        }
    }
    
    static class SubIndex extends CmdTDB
    {
        protected SubIndex(String[] argv)
        {
            super(argv) ;
        }

        @Override
        protected String getSummary()
        {
            return "tdbdump index INDEX" ;
        }

        @Override
        protected void exec()
        {
            for ( String fn: super.getPositional() )
            {
                execOne(fn) ;
            }
        }
        
        private void execOne(String fn)
        {
            FileSet fileset = new FileSet(fn) ;
            // Look in the fileset metadata.
            
            if ( !fileset.getMetaFile().existsMetaData() )
                throw new CmdException("No metadata") ;
                
            if ( ! fileset.getMetaFile().hasProperty("tdb.bplustree.record") )
                throw new CmdException("No record size in metadata") ;
            
            RangeIndex rIndex = SetupTDB.makeBPlusTree(fileset, SystemTDB.BlockReadCacheSize, SystemTDB.BlockWriteCacheSize, -1, -1) ;
            dumpIndex(rIndex) ;
        }
        
        static void dumpIndex(RangeIndex rIndex )
        {
            for ( Record r : rIndex )
                System.out.println(r.toString()) ;
        }
    }
    
    static class SubNodes extends CmdTDB
    {
        protected SubNodes(String[] argv)
        {
            super(argv) ;
        }

        @Override
        protected String getSummary()
        {
            return "tdbdump nodes FILE..." ;
        }

        @Override
        protected void exec()
        {
            List<String> args = positionals ;
            for ( String x : args )
            {
                System.out.println("**** File: "+x) ;
                StringFile objs = FileFactory.createStringFileDisk(x) ;
                objs.dump(handler) ;
            }
        }
        
        static void dumpNodeTable(NodeTable nodeTable)
        {
            // ???? 
        }
        
        static StringFile.DumpHandler handler = new StringFile.DumpHandler() {
            //@Override
            public void handle(long fileIdx, String str)
            {
                System.out.printf("0x%08X : %s\n", fileIdx, str) ;
            }
        } ;
        
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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