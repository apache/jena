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

package org.seaborne.jena.engine.join;

import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;

import org.seaborne.jena.engine.Row ;
import org.seaborne.jena.engine.RowLib ;
import org.seaborne.jena.engine.RowList ;

/** Use with Merge to ge a sort-merge join */ 
public class SortRowList
{
    /** Sort a RowList */
    public static <X> RowList<X> sort(RowList<X> rows, Comparator<Row<X>> compare) {
        List<Row<X>> data = rows.toList() ;
        Collections.sort(data, compare);
        return RowLib.createRowList(rows.vars(), data.iterator()) ;
    }
}
