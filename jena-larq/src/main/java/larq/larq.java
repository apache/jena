/**
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

package larq;

import java.util.Iterator;

import org.apache.jena.larq.HitLARQ;
import org.apache.jena.larq.IndexLARQ;

import larq.cmdline.CmdLARQ;
import larq.cmdline.ModLARQindex;

import com.hp.hpl.jena.sparql.util.FmtUtils;

public class larq extends CmdLARQ
{
    ModLARQindex modIndex = new ModLARQindex() ;
    
    public static void main(String... argv)
    {
        new larq(argv).mainRun() ;
    }
    
    protected larq(String[] argv)
    {
        super(argv) ;
        super.addModule(modIndex) ;
    }

    @Override
    protected String getSummary()
    {
        return "larqquery --larq DIR LuceneQueryString" ;
    }

    @Override
    protected void exec()
    {
        IndexLARQ index = modIndex.getIndexLARQ() ;
        for ( String s : super.getPositional() )
        {
            System.out.println("Search : "+s) ;
            Iterator<HitLARQ> hits = index.search(s) ;
            while ( hits.hasNext() )
            {
                HitLARQ h = hits.next() ;
                String str = FmtUtils.stringForNode(h.getNode()) ;
                if ( super.isVerbose() )
                    System.out.printf("  %-20s %.2f\n", str, h.getScore()) ;
                else
                    System.out.printf("  %-20s\n",str) ;
            }
        }
    }

//    private void index(IndexBuilderString larqBuilder, Model model)
//    {
//        StmtIterator sIter = model.listStatements() ;
//        larqBuilder.indexStatements(sIter) ;
//    }
}
