/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package test;

import static org.openjena.atlas.lib.RandomLib.random;
import static org.openjena.atlas.test.Gen.permute;
import static org.openjena.atlas.test.Gen.rand;
import static org.openjena.atlas.test.Gen.strings;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.tdb.index.ext.ExtHash;
import com.hp.hpl.jena.tdb.index.ext.ExtHashTestBase;
import com.hp.hpl.jena.tdb.sys.SystemTDB;


//import tdb.Cmd;

public abstract class ExtHashRun
{
    static boolean showProgress = true ;
    
    static { Log.setLog4j() ; }

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
            new Perf().exec(args) ;
        else
        {
            System.err.println("Unknown subcommand: "+subCmd) ;
            System.exit(1) ;
        }
    }
    
    public void exec(List<String> args)
    {
        args = processArgs(args) ;
        int numKeys = Integer.parseInt(args.get(0)) ;
        int iterations = Integer.parseInt(args.get(1)) ;
        exec(numKeys, iterations) ;
    }        
    
    protected abstract void exec(int numKeys, int iterations) ;
    
    // ---- Test
    public static class Test extends ExtHashRun
    {
        @Override
        protected void exec(int numKeys, int iterations)
        {
            ExtHash.Checking = true ;
            ExtHashTestBase.randTests(10*numKeys, numKeys, iterations, showProgress) ;
        }
    }

    // ---- Perfromance
    public static class Perf extends ExtHashRun
    {
        @Override
        public void exec(List<String> args)
        {
            showProgress = true ;
            ExtHash.Checking = false ;
            ExtHash.Logging = false ;
            SystemTDB.NullOut = false ;
            super.exec(args) ;
        }
        
        @Override
        protected void exec(int numKeys, int iterations)
        {
            RandomGen rand = new RandomGen(100*numKeys, numKeys) ;
            org.openjena.atlas.test.RepeatExecution.repeatExecutions(rand, iterations, showProgress) ;
        }
    }
    
    static class RandomGen implements org.openjena.atlas.test.ExecGenerator
    {
        int maxNumKeys ;
        int maxValue ;

        RandomGen(int maxValue, int maxNumKeys)
        {
            if ( maxValue <= maxNumKeys )
                throw new IllegalArgumentException("ExtHash: Max value less than number of keys") ;
            this.maxValue = maxValue ; 
            this.maxNumKeys = maxNumKeys ;
        }

        @Override
        public void executeOneTest()
        {
            int numKeys = random.nextInt(maxNumKeys)+1 ;
            perfTest(maxValue, numKeys) ;
        }
        
        /* Performance test : print the keys if there was a problem */ 
        public static void perfTest(int maxValue, int numKeys)
        {
//            if ( numKeys >= 3000 )
//                System.err.printf("Warning: a lot of keys\n") ;
                
            int[] keys1 = rand(numKeys, 0, maxValue) ;
            int[] keys2 = permute(keys1, numKeys) ;
            try {
                ExtHash extHash = ExtHashTestBase.create(keys1) ;
                ExtHashTestBase.delete(extHash, keys2) ;
            } catch (RuntimeException ex)
            {
                System.err.printf("int[] keys1 = {%s} ;\n", strings(keys1)) ;
                System.err.printf("int[] keys2 = {%s}; \n", strings(keys2)) ;
                throw ex ;
            }
        }
    }

    List<String> processArgs(List<String> args)
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
            {}
            else if ( a.equalsIgnoreCase("check") )
            {
                ExtHash.Checking = true ;
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

        if ( args.size() != 2 )
        {
            usage(System.err) ;
            System.exit(1) ;
        }
        return args ;
    }

    public static void usage(PrintStream printStream)
    {
        printStream.println("Usage: OPTIONS NumKeys Iterations") ;
        printStream.println("Options:") ;
        printStream.println("   --check") ;
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