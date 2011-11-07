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
import java.util.List ;

import org.apache.log4j.Level ;
import org.openjena.atlas.test.ExecGenerator ;

import com.hp.hpl.jena.tdb.base.file.BlockAccessMem ;
import com.hp.hpl.jena.tdb.index.IndexTestGenerator ;
import com.hp.hpl.jena.tdb.index.RangeIndexMaker ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public abstract class RunnerRangeIndex extends RunnerExecute
{
    int order ;
    int maxValue ; 
    int maxNumKeys ;
    
    static boolean trackingBlocks = false ;
    
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
        order = Integer.parseInt(args.get(0)) ;
        int numKeys = Integer.parseInt(args.get(1)) ;
        int iterations = Integer.parseInt(args.get(2)) ;

        maxValue = 10*numKeys ;  
        maxNumKeys = numKeys ;
        return iterations ;
    }

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
                BPlusTreeParams.CheckingNode = false ;
                SystemTDB.NullOut = true ;
                BlockAccessMem.SafeMode = true ;
            }
            else if ( a.equalsIgnoreCase("bptree:checknode") )
            {
                BPlusTreeParams.CheckingTree = true ;
                BPlusTreeParams.CheckingNode = true ;
                SystemTDB.NullOut = true ;
                BlockAccessMem.SafeMode = true ;
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
                BlockAccessMem.SafeMode = true ;
            else if ( a.equalsIgnoreCase("check") )
            {
                BPlusTreeParams.CheckingNode = false;
                BPlusTreeParams.CheckingTree = false ;
//                SystemTDB.NullOut = true ;
//                FileAccessMem.SafeMode = true ;
            }
            else if ( a.equalsIgnoreCase("display") )
            {
                showProgress = ! showProgress ;
            }
            else if ( a.equalsIgnoreCase("bptree:track") )
            {
                BPlusTreeParams.CheckingTree = false ;
                BPlusTreeParams.CheckingNode = false ;
                trackingBlocks = true ;
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
