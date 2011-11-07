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

package com.hp.hpl.jena.sparql.sse.builders;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;

public class BuilderExec
{
    static public void main(String[] argv)
    {
        Item item = SSE.readFile("SSE/all.sse") ;
        exec(item) ;
    }
    
    static public void exec(Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build evaluation from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build evaluation from a bare symbol") ;

        if ( ! item.isTagged(Tags.tagExec) )
            throw new BuildException("Wanted ("+Tags.tagExec+"...) : got: "+item.shortString());

        ItemList list = item.getList() ;
        BuilderLib.checkLength(3, list, item.shortString()+ " does not have 2 components");
        
        DatasetGraph dsg = BuilderGraph.buildDataset(list.get(1)) ;
        Op op = BuilderOp.build(list.get(2)) ;
        QueryExecUtils.execute(op, dsg, ResultsFormat.FMT_TEXT) ;
    }
}
