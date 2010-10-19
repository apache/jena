/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import java.io.InputStream ;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.fuseki.server.SPARQLServer ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.lang.SinkQuadsToDataset ;
import org.openjena.riot.lang.SinkTriplesToGraph ;
import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdARQ ;
import arq.cmdline.ModDatasetAssembler ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.TDB ;

public class FusekiCmd extends CmdARQ
{
    private static ArgDecl argMem       = new ArgDecl(ArgDecl.NoValue, "mem") ;
    private static ArgDecl argFile       = new ArgDecl(ArgDecl.HasValue, "file") ;
    private static ArgDecl argMemTDB    = new ArgDecl(ArgDecl.NoValue, "memtdb", "memTDB") ;
    private static ArgDecl argPort      = new ArgDecl(ArgDecl.HasValue, "port") ;
    
    private static ModDatasetAssembler modDataset = new ModDatasetAssembler() ;
    
    // fuseki [--mem|--desc assembler.ttl] [--port PORT] **** /datasetURI

    static public void main(String...argv)
    {
        // Just to make sure ...
        ARQ.init();
        Fuseki.init() ;
        new FusekiCmd(argv).mainRun() ;
    }
    
    private int port = 3030 ;
    private DatasetGraph dsg ;
    private String datasetPath ; 
    
    public FusekiCmd(String...argv)
    {
        super(argv) ;
        addModule(modDataset) ;
        add(argMem, "--mem", "Create an in-memory, non-persistent dataset for the server") ;
        add(argFile, "--file=FILE", "Create an in-memory, non-persistent dataset for the server, initialised with the contents of the file") ;
        add(argMemTDB, "--memTDB", "Create an in-memory, non-persistent dataset using TDB (testing only)") ;
        add(argPort, "--port", "Port number") ;
        super.modVersion.addClass(TDB.class) ;
        super.modVersion.addClass(Fuseki.class) ;
    }

    static String argUsage = "[--mem|--desc=AssemblerFile|--file=FILE] [--port PORT] /DatasetPathName" ; 
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" "+argUsage ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        int x = 0 ;
        
        ArgDecl assemblerDescDecl = new ArgDecl(ArgDecl.HasValue, "desc", "dataset") ;
        if ( contains(argMem) ) x++ ; 
        if ( contains(argFile) ) x++ ;
        if ( contains(assemblerDescDecl) ) x++ ;
        
        if ( x > 1 )
            throw new CmdException("Only one of --mem, --file and --desc") ;
        
        if ( x == 0 )
            throw new CmdException("Required: one of --mem, --file and --desc") ;

        if ( contains(argMem) || contains(argFile))
            dsg = DatasetGraphFactory.createMem() ;
        if ( contains(argFile) )
        {
            // replace by RiotLoader after ARQ refresh.
            String filename = getValue(argFile) ;
            Lang language = Lang.guess(filename) ;
            if ( language == null )
                throw new CmdException("Can't guess language for file; "+filename) ;
            InputStream input = IO.openFile(filename) ; 
            
            if ( language.isQuads() )
            {
                Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
                RiotLoader.readQuads(input, language, filename, sink) ;
            }
            else
            {
                Sink<Triple> sink = new SinkTriplesToGraph(dsg.getDefaultGraph()) ;
                RiotLoader.readTriples(input, language, filename, sink) ;
            }
        }
        
        if ( contains(argMemTDB) )
            throw new CmdException(argMemTDB.getKeyName()+" not implemented") ;
        
        if ( contains(argPort) )
        {
            String portStr = getValue(argPort) ;
            try {
                port = Integer.parseInt(portStr) ;
            } catch (NumberFormatException ex)
            {
                throw new CmdException(argPort.getKeyName()+" : bad port number: "+portStr) ;
            }
        }
        Dataset ds = modDataset.createDataset() ;
        if ( ds != null )
            dsg = ds.asDatasetGraph() ;
        if ( dsg == null )
            throw new CmdException("No dataset defined: "+argUsage) ;
        
        if ( getPositional().size() == 0 )
            throw new CmdException("No dataset path name given") ;
        if ( getPositional().size() > 1  )
            throw new CmdException("Multiple dataset path names given") ;
        datasetPath = getPositionalArg(0) ;
        if ( datasetPath.length() > 0 && ! datasetPath.startsWith("/") )
            throw new CmdException("Dataset path name must begin with a /: "+datasetPath) ;
    }

    @Override
    protected void exec()
    {
        SPARQLServer server = new SPARQLServer(dsg, datasetPath, port, super.isVerbose()) ;
        server.start() ;
        try { server.getServer().join() ; } catch (Exception ex) {}
    }

    @Override
    protected String getCommandName()
    {
        return "fuseki" ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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