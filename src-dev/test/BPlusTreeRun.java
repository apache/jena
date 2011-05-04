/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package test;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.index.RangeIndexMaker;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeMaker;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class BPlusTreeRun extends RunnerRangeIndex
{
    static { Log.setLog4j() ; }
    
    static public void main(String...a)
    {
        new BPlusTreeRun().perform(a) ;
    }
    
    
    @Override
    protected RangeIndexMaker makeRangeIndexMaker()
    {
        
        BPlusTree bpt = (BPlusTree)(new BPlusTreeMaker(order, order).makeIndex()) ;
        BPlusTreeParams param = bpt.getParams() ;
        
        System.out.println(bpt.getParams()) ;
        System.out.println("Block size = "+bpt.getParams().getCalcBlockSize()) ;
        return new BPlusTreeMaker(order, order) ;
        

    }


    @Override
    protected void startRun(RunType runType)
    {
        
        switch (runType)
        {
            case test:
                showProgress = true ;
                //BPlusTreeParams.checkAll() ;
                BPlusTreeParams.CheckingTree = true ;
                BPlusTreeParams.CheckingNode = true ;
                SystemTDB.NullOut = true ;
                break ;
            case perf:  
                showProgress = false ;
                BPlusTreeParams.CheckingTree = false ;
                BPlusTreeParams.CheckingNode = false ;
                SystemTDB.NullOut = false ;
                break ;
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