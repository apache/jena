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
        System.out.println(args) ;
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
        
        initialize(runType) ;
        
        args = processArgs(args) ;
        int iterations = startRun(args, runType) ;
        
        ExecGenerator gen = execGenerator() ;
        RepeatExecution.repeatExecutions(gen, iterations, showProgress) ;
        finishRun() ;
    }
    
    protected abstract void initialize(RunType runType) ;
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
        printStream.println("   --bptree:track") ;
        printStream.println("   --bptree:checknode (expensive)") ;
        printStream.println("   --bptree:log") ;
        printStream.println("   --bptree:safe") ;
    }
    
    /* Performance test : print the keys if there was a problem */ 
    
    public static void perfTest(int order, int maxValue, int numKeys)
    {
        // UNUSED.
//        if ( numKeys >= 3000 )
//            System.err.printf("Warning: too many keys\n") ;
       
        int[] keys1 = rand(numKeys, 0, maxValue) ;
        int[] keys2 = permute(keys1, numKeys) ;
        try {
            IndexMaker maker = new BPlusTreeMaker(order, order, false) ;
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
