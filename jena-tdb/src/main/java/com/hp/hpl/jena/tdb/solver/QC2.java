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

package com.hp.hpl.jena.tdb.solver;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.lib.Tuple ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class QC2
{
    public static Filter<Tuple<NodeId>> getFilter(Context context)
    {
        Object x = context.get(SystemTDB.symTupleFilter) ;

        try {
            @SuppressWarnings("unchecked")
            Filter<Tuple<NodeId>> f = (Filter<Tuple<NodeId>>)x ;
            return f ;
        } catch (ClassCastException ex)
        {
            throw new TDBException("Not a Filter<Tuple<NodeId>>:"+x, ex) ;
        }
    }

    public static void setFilter(Context context, Filter<Tuple<NodeId>> filter)
    {
        context.set(SystemTDB.symTupleFilter, filter) ;
    }
}
