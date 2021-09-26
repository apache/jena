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

package org.apache.jena.sparql.sse.builders;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetStream;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.ItemList;
import org.apache.jena.sparql.sse.Tags;

public class BuilderRowSet {

    public static RowSet build(Item item) {
        if ( item.isTagged(Tags.tagTable) )
            return BuilderTable.build(item).toRowSet();

        if ( ! item.isTagged(Tags.tagResultSet) && ! item.isTagged(Tags.tagRowSet) )
            BuilderLib.broken(item, "Expected (resultset ...) or (rowset ...)", item);

        return buildRowSet(item);
    }

    /*package*/ static RowSet buildRowSet(Item item) {
        ItemList list = item.getList();

        List<Var> vars = BuilderNode.buildVarList(list.get(1));
        // skip tag, skip vars.
        int start = 2;

        List<Binding> bindings = new ArrayList<>();
        for ( int i = start ; i < list.size() ; i++ ) {
            Item itemRow = list.get(i);
            Binding b = BuilderBinding.build(itemRow);
            bindings.add(b);
        }

        return new RowSetStream(vars, bindings.iterator());
    }
}
