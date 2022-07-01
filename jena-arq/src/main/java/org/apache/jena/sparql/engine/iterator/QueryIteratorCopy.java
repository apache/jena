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

package org.apache.jena.sparql.engine.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

/**
 * A QueryIterator that copies an iterator.
 * 
 * @see QueryIter#materialize
 */

class QueryIteratorCopy extends QueryIteratorBase {
    // Not tracked.
    List<Binding> elements = new ArrayList<>();
    QueryIterator iterator;

    public QueryIteratorCopy(QueryIterator qIter) {
        for ( ; qIter.hasNext() ; )
            elements.add(qIter.nextBinding());
        qIter.close();
        iterator = copy();
    }

    @Override
    protected Binding moveToNextBinding() {
        return iterator.nextBinding();
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        out.print("QueryIteratorCopy");
        out.incIndent();
        out.decIndent();
    }

    public List<Binding> elements() {
        return Collections.unmodifiableList(elements);
    }

    public QueryIterator copy() {
        return QueryIterPlainWrapper.create(elements.iterator());
    }

    @Override
    protected void closeIterator() {
        iterator.close();
    }

    @Override
    protected void requestCancel() {
        iterator.cancel();
    }

    @Override
    protected boolean hasNextBinding() {
        return iterator.hasNext();
    }
}
