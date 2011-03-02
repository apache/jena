/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package arq;

import java.util.Iterator ;
import java.util.List ;

import org.openjena.riot.out.NQuadsWriter ;

import arq.cmd.CmdException ;
import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdUpdate ;

import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class update extends CmdUpdate
{
    // --service / --remote
    ArgDecl updateArg = new ArgDecl(ArgDecl.HasValue, "update", "file") ;
    ArgDecl dumpArg = new ArgDecl(ArgDecl.NoValue, "dump") ;       // Write the result to stdout.
    
    List<String> requestFiles = null ;
    boolean dump = false ;
    
    public static void main (String... argv)
    { new update(argv).mainRun() ; }
    
    protected update(String[] argv)
    {
        super(argv) ;
        super.add(updateArg, "--update=FILE", "Update commands to execute") ;
        super.add(dumpArg, "--dump", "Dump the resulting graph store") ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        requestFiles = getValues(updateArg) ;   // ????
        dump = contains(dumpArg) ;
        super.processModulesAndArgs() ;
    }
    
    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" --desc=assembler [--dump] --update=<request file>" ; }

    // Subclass for specialised commands making common updates more convenient
    @Override
    protected void execUpdate(GraphStore graphStore)
    {
        if ( requestFiles.size() == 0 && getPositional().size() == 0 )
            throw new CmdException("Nothing to do") ;
        
        for ( Iterator<String> iter = requestFiles.iterator() ; iter.hasNext() ; )
        {
            String filename = iter.next();
            execOneFile(filename, graphStore) ;
        }
        
        for ( Iterator<String> iter = super.getPositional().iterator() ; iter.hasNext() ; )
        {
            String requestString = iter.next();
            requestString = indirect(requestString) ;
            execOne(requestString, graphStore) ;
        }
        SystemARQ.sync(graphStore) ;
        if ( dump )
            //SSE.write(graphStore) ;
            NQuadsWriter.write(System.out, graphStore) ;
    }


    private void execOneFile(String filename, GraphStore store)
    {
        UpdateRequest req = UpdateFactory.read(filename) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }
    
    private void execOne(String requestString, GraphStore store)
    {
        UpdateRequest req = UpdateFactory.create(requestString) ;
        UpdateExecutionFactory.create(req, store).execute() ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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