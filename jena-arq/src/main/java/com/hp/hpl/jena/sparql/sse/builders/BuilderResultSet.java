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

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class BuilderResultSet
{
       
    public static ResultSet build(Item item)
    {
        BuilderLib.checkTagged(item, Tags.tagResultSet, "Not a (resultset ...)") ;
        ItemList list = item.getList() ; 
        
        List<Var> vars = BuilderNode.buildVarList(list.get(1)) ;
        // skip tag, skip vars.
        int start = 2 ;

        List<Binding> bindings = new ArrayList<>() ;
        for ( int i = start ; i < list.size() ; i++ )
        {
            Item itemRow = list.get(i) ;
            Binding b = BuilderBinding.build(itemRow) ;
            bindings.add(b) ;
        }
        
        QueryIterator qIter = new QueryIterPlainWrapper(bindings.listIterator()) ;
        return new ResultSetStream(Var.varNames(vars), null, qIter) ;
    }
}
