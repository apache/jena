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

package org.seaborne.dboe.engine.general;

import java.util.Iterator ;

import org.seaborne.dboe.engine.Row ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Wrap a Row&lt;Node&gt; to present the Binding interface */
public final class BindingRowNode implements Binding {
    private final Row<Node> row ;

    public BindingRowNode(Row<Node> row) { this.row = row ; }
    
    @Override
    public Iterator<Var> vars() {
        return row.vars().iterator() ;
    }

    @Override
    public boolean contains(Var var) {
        return row.contains(var) ;
    }

    @Override
    public Node get(Var var) {
        return row.get(var) ;
    }

    @Override
    public int size() {
        return row.vars().size() ;
    }

    @Override
    public boolean isEmpty() {
        return row.vars().size() == 0 ;
    }
}