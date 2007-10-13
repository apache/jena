/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator;
import java.util.List;

import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;
import arq.cmdline.ModDataset;

import com.hp.hpl.jena.sparql.sse.WriterSSE;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class update extends CmdARQ
{
    ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "--update") ;
    ArgDecl outArg = new ArgDecl(ArgDecl.HasValue, "--out") ;
    ModDataset dataset = new ModAssembler() ;
    List requests = null ;
    
    public static void main (String [] argv)
    { new update(argv).main() ; }
    
    protected update(String[] argv)
    {
        super(argv) ;
        super.addModule(dataset) ;
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
    }

    protected void processModulesAndArgs()
    {
        requests = getValues(updateArg) ;
        
        super.processModulesAndArgs() ;
    }
    
    protected String getCommandName() { return Utils.className(this) ; }
    
    protected String getSummary() { return getCommandName()+" --data=file --update=<query>" ; }

    protected void exec()
    {
        Dataset ds = dataset.getDataset() ;
        if ( ds == null )
            ds = DatasetFactory.create() ;
        
        GraphStore store = GraphStoreFactory.create(ds) ;
        
        if ( requests.size() == 0 )
            throw new CmdException("Nothing to do") ;
        
        for ( Iterator iter = requests.iterator() ; iter.hasNext() ; )
        {
            String filename = (String)iter.next();
            execOne(filename, store) ;
        }
        
        // Writer
        IndentedWriter out = new IndentedWriter(System.out) ;
        WriterSSE.out(out, ds.asDatasetGraph()) ;
        out.flush();
    }


    private void execOne(String filename, GraphStore store)
    {
        UpdateRequest req = UpdateFactory.read(filename) ;
        store.execute(req) ;
    }


}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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