/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package test;
import java.util.List ;

import org.apache.log4j.Level ;
import org.openjena.atlas.test.ExecGenerator ;

import com.hp.hpl.jena.tdb.base.file.FileAccessMem ;
import com.hp.hpl.jena.tdb.index.IndexTestGenerator ;
import com.hp.hpl.jena.tdb.index.RangeIndexMaker ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public abstract class RunnerRangeIndex extends RunnerExecute
{
    int order ;
    int maxValue ; 
    int maxNumKeys ;
    
    protected abstract RangeIndexMaker makeRangeIndexMaker() ;
    
    @Override
    protected ExecGenerator execGenerator()
    {
        RangeIndexMaker maker = makeRangeIndexMaker() ;
        //new RangeIndexTestGenerator(maker, numKeys*100, numKeys) ;
        IndexTestGenerator test = new IndexTestGenerator(maker, maxValue, maxNumKeys) ;
        return test ;
    }

    @Override
    protected int startRun(List<String> args, RunType runType)
    {
        startRun(runType) ;
        order = Integer.parseInt(args.get(0)) ;
        int numKeys = Integer.parseInt(args.get(1)) ;
        int iterations = Integer.parseInt(args.get(2)) ;
        
        maxValue = 10*numKeys ;  
        maxNumKeys = numKeys ;
        return iterations ;
    }

    protected abstract void startRun(RunType runType) ;

    @Override
    protected void finishRun()
    {}
    
    /** Process the arguments - return any to be done later (positionals) */  
    @Override
    protected List<String> processArgs(List<String> args)
    {
        
        int i = 0 ;
        while ( args.size()>0 )
        {
            if ( !args.get(0).startsWith("-") )
                break ;

            String a = args.remove(0) ;
            if ( a.startsWith("--") )
                a = a.substring(2) ;
            else
                a = a.substring(1) ;

            if ( a.equals("h") || a.equals("help") )
            {
                usage(System.out) ;
                System.exit(0) ;
            }
            else if ( a.equals("v") )
                BPlusTreeParams.DumpTree = true ;
            else if ( a.equalsIgnoreCase("bptree:check") )
            {
                BPlusTreeParams.CheckingTree = true ;
                SystemTDB.NullOut = true ;
            }
            else if ( a.equalsIgnoreCase("bptree:checknode") )
            {
                BPlusTreeParams.CheckingNode = true ;
                SystemTDB.NullOut = true ;
                FileAccessMem.SafeMode = true ;
            }
            else if ( a.equalsIgnoreCase("bptree:log") )
            {
                showProgress = false ;
                org.apache.log4j.LogManager.getLogger("bptree").setLevel(Level.DEBUG) ;
                org.apache.log4j.LogManager.getLogger("bptree.block").setLevel(Level.INFO) ;
            }
            else if ( a.equalsIgnoreCase("block:log") )
            {
                showProgress = false ;
                org.apache.log4j.LogManager.getLogger("bptree.block").setLevel(Level.DEBUG) ;
            }
            else if ( a.equalsIgnoreCase("block:safe") )
                FileAccessMem.SafeMode = true ;
            else if ( a.equalsIgnoreCase("check") )
            {
                BPlusTreeParams.CheckingNode = true ;
                SystemTDB.NullOut = true ;
                FileAccessMem.SafeMode = true ;
            }
            else if ( a.equalsIgnoreCase("display") )
            {
                showProgress = ! showProgress ;
            }
            else   
            {
                System.err.println("Unknown argument: "+a) ;
                System.exit(1) ;
            }
        }
        
        if ( args.size() != 3 )
        {
            usage(System.err) ;
            System.exit(1) ;
        }
        
        return args ;
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