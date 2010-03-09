/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import tdb.cmdline.CmdTDB;
import tdb.cmdline.ModTDBDataset;
import arq.cmdline.ArgDecl;
import arq.cmdline.ModDataset;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.solver.Explain ;


public class tdbquery extends arq.query
{
    private ArgDecl argExplain = new ArgDecl(ArgDecl.NoValue, "explain") ;

    // Inherits from arq.query so is not a CmdTDB.  Mixins for Java!
    public static void main(String...argv)
    {
        new tdbquery(argv).mainRun() ;
    }
    
    public tdbquery(String[] argv)
    {
        super(argv) ;
        // Because this inherits from an ARQ command
        CmdTDB.init() ;
        super.add(argExplain) ;
        super.modVersion.addClass(TDB.class) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( isVerbose() )
            ARQ.getContext().setTrue(TDB.symLogExec) ;
        if ( super.hasArg(argExplain) )
        {
            //Log.enable(TDB.logExec.getName(), "info") ;
            TDB.setExecutionLogging(Explain.InfoLevel.ALL) ;
        }
    }
    
    @Override
    protected ModDataset setModDataset()
    {
        return new ModTDBDataset() ;
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