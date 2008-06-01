/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package test;

import static com.hp.hpl.jena.tdb.btree.RangeIndexTestLib.buildRangeIndex;
import static com.hp.hpl.jena.tdb.btree.RangeIndexTestLib.delete;
import static lib.RandomLib.random;
import static test.Gen.permute;
import static test.Gen.rand;
import static test.Gen.strings;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import tdb.Cmd;
import test.RandomExecution;
import test.RandomExecution.ExecGenerator;

import com.hp.hpl.jena.tdb.base.BaseConfig;
import com.hp.hpl.jena.tdb.base.block.BlockMgrMem;
import com.hp.hpl.jena.tdb.btree.*;
import com.hp.hpl.jena.tdb.index.RangeIndex;

public abstract class BTreeRun
{
    static boolean showProgress = true ;
    
    static public void main(String...a)
    {
        List<String> args = new ArrayList<String>(Arrays.asList(a)) ;
        if ( args.size() == 0 )
        {
            System.err.println("No subcommand") ;
            System.exit(1) ;
        }
        String subCmd = args.remove(0) ;
        if ( "test".equalsIgnoreCase(subCmd) )
            new Test().exec(args) ;
        else if ( "perf".equalsIgnoreCase(subCmd) )
        {
            showProgress = false ;
            new Perf().exec(args) ;
        }
        else
        {
            System.err.println("Unknown subcommand: "+subCmd) ;
            System.exit(1) ;
        }
    }
    
    public void exec(List<String> args)
    {
        args = processArgs(args) ;
        
        int order = Integer.parseInt(args.get(0)) ;
        int numKeys = Integer.parseInt(args.get(1)) ;
        int iterations = Integer.parseInt(args.get(2)) ;
        exec(order, numKeys, iterations) ;
    }        
    
    protected abstract void exec(int order, int numKeys, int iterations) ;
    

    // ---- Test
    public static class Test extends BTreeRun
    {
        @Override
        protected void exec(int order, int numKeys, int iterations)
        {
            RangeIndexMaker maker = new BTreeMaker(order) ;
            RangeIndexTestLib.randTests(maker, numKeys*100, numKeys, iterations, showProgress) ;
        }
    }

    // ---- Performance
    public static class Perf extends BTreeRun
    {
        @Override
        public void exec(List<String> args)
        {
            showProgress = false ;
            BTreeParams.CheckingBTree = false ;
            BTreeParams.CheckingNode= false ;
            BaseConfig.NullOut = false ;
            super.exec(args) ;
        }
        
        @Override
        protected void exec(int order, int numKeys, int iterations)
        {
            RandomGen rand = new RandomGen(order, 100*numKeys, numKeys) ;
            RandomExecution.randExecGenerators(rand, iterations, showProgress) ;
        }
    }
    
    
    List<String> processArgs(List<String> args)
    {
        Cmd.setLog4j() ;
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
                BTreeParams.DumpTree = true ;
            else if ( a.equalsIgnoreCase("btree:check") )
            {
                BTreeParams.CheckingBTree = true ;
                BaseConfig.NullOut = true ;
            }
            else if ( a.equalsIgnoreCase("btree:checknode") )
            {
                BTreeParams.CheckingNode = true ;
                BaseConfig.NullOut = true ;
                BlockMgrMem.SafeMode = true ;
            }
            else if ( a.equalsIgnoreCase("btree:log") )
            {
                showProgress = false ;
                org.apache.log4j.LogManager.getLogger("btree").setLevel(Level.DEBUG) ;
                org.apache.log4j.LogManager.getLogger("btree.block").setLevel(Level.INFO) ;
            }
            else if ( a.equalsIgnoreCase("block:log") )
            {
                showProgress = false ;
                org.apache.log4j.LogManager.getLogger("btree.block").setLevel(Level.DEBUG) ;
            }
            else if ( a.equalsIgnoreCase("block:safe") )
                BlockMgrMem.SafeMode = true ;
            else if ( a.equalsIgnoreCase("check") )
            {
                //BTreeParams.CheckingBTree = true ;
                BTreeParams.CheckingNode = true ;
                BaseConfig.NullOut = true ;
                BlockMgrMem.SafeMode = true ;
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
    
    public static void usage(PrintStream printStream)
    {
        printStream.println("Usage: OPTIONS Order NumKeys Iterations") ;
        printStream.println("Options:") ;
        printStream.println("   --display") ;
        printStream.println("   --check (same as btree:checknode)") ;
        printStream.println("   --btree:check") ;
        printStream.println("   --btree:checknode (expensive)") ;
        printStream.println("   --btree:log") ;
        printStream.println("   --block:log") ;
        printStream.println("   --block:safe") ;
    }
    
    static class RandomGen implements ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;
        int order ;

        RandomGen(int order, int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("BTree: Max value less than number of keys") ;
            this.order = order ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }

        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            perfTest(order, maxValue, numKeys) ;
        }
    }

    /* Performance test : print the keys if there was a problem */ 
    
    public static void perfTest(int order, int maxValue, int numKeys)
    {
        if ( numKeys >= 3000 )
            System.err.printf("Warning: too many keys\n") ;
            
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, numKeys) ;
        try {
            RangeIndexMaker maker = new BTreeMaker(order) ;
            RangeIndex rIndex = buildRangeIndex(maker, keys1);
            delete(rIndex, keys2) ;
        } catch (RuntimeException ex)
        {
            System.err.printf("int order=%d ;\n", order) ;
            System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
            System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
            throw ex ;
        }
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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