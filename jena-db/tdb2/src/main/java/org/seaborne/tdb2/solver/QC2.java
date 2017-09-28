/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.solver;

import java.util.function.Predicate;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.sparql.util.Context ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.store.NodeId ;
import org.seaborne.tdb2.sys.SystemTDB ;

public class QC2
{
    public static Predicate<Tuple<NodeId>> getFilter(Context context)
    {
        Object x = context.get(SystemTDB.symTupleFilter) ;

        try {
            @SuppressWarnings("unchecked")
            Predicate<Tuple<NodeId>> f = (Predicate<Tuple<NodeId>>)x ;
            return f ;
        } catch (ClassCastException ex)
        {
            throw new TDBException("Not a Filter<Tuple<NodeId>>:"+x, ex) ;
        }
    }

    public static void setFilter(Context context, Predicate<Tuple<NodeId>> filter)
    {
        context.set(SystemTDB.symTupleFilter, filter) ;
    }
}
