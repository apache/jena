/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package test;

import static org.openjena.atlas.test.Gen.permute ;
import static org.openjena.atlas.test.Gen.rand ;
import static org.openjena.atlas.test.Gen.strings ;

import java.io.PrintStream ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;

import org.openjena.atlas.test.ExecGenerator ;
import org.openjena.atlas.test.RepeatExecution ;

import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexMaker ;
import com.hp.hpl.jena.tdb.index.IndexTestLib ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeMaker ;


public abstract class RunnerExecute
{
    static enum RunType { test , perf }

    static boolean showProgress = true ;
    
    public void perform(String...a)
    {
        List<String> args = new ArrayList<String>(Arrays.asList(a)) ;
        if ( args.size() == 0 )
        {
            System.err.println("No subcommand") ;
            System.exit(1) ;
        }
        String subCmd = args.remove(0) ;
        RunType runType = null ;
        
        if ( "test".equalsIgnoreCase(subCmd) )
            runType = RunType.test ;
        else if ( "perf".equalsIgnoreCase(subCmd) )
            runType = RunType.perf ;
        else
        {
            System.err.println("Unknown subcommand: "+subCmd) ;
            System.exit(1) ;
        }
        
        args = processArgs(args) ;
        int iterations = startRun(args, runType) ;
        ExecGenerator gen = execGenerator() ;
        RepeatExecution.repeatExecutions(gen, iterations, showProgress) ;
        finishRun() ;
    }
    
    protected abstract List<String> processArgs(List<String> args) ;

    protected abstract ExecGenerator execGenerator() ;
    protected abstract int startRun(List<String> args, RunType runType) ;
    protected abstract void finishRun() ;
    
    public static void usage(PrintStream printStream)
    {
        printStream.println("Usage: OPTIONS Order NumKeys Iterations") ;
        printStream.println("Options:") ;
        printStream.println("   --display") ;
        printStream.println("   --check (same as btree:checknode)") ;
        printStream.println("   --bptree:check") ;
        printStream.println("   --bptree:checknode (expensive)") ;
        printStream.println("   --bptree:log") ;
        printStream.println("   --bptree:safe") ;
    }
    
    /* Performance test : print the keys if there was a problem */ 
    
    public static void perfTest(int order, int maxValue, int numKeys)
    {
//        if ( numKeys >= 3000 )
//            System.err.printf("Warning: too many keys\n") ;
            
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, numKeys) ;
        try {
            IndexMaker maker = new BPlusTreeMaker(order, order) ;
            Index rIndex = IndexTestLib.buildIndex(maker, keys1);
            IndexTestLib.delete(rIndex, keys2) ;
        } catch (RuntimeException ex)
        {
            System.err.printf("int order=%d ;\n", order) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s} ; \n", strings(keys2)) ;
            throw ex ;
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