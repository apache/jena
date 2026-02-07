/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;

/**
 * A QueryIterator that upon access of the first item consumes the underlying
 * iterator into a list. Supports abort during materialization.
 */
public class QueryIteratorMaterialize extends QueryIteratorWrapper {
    protected QueryIterator outputIt = null;
    protected ExecutionContext execCxt;

    /** If the threshold policy is not set then it will be lazily initialized from the execCxt */
    protected ThresholdPolicy<Binding> thresholdPolicy;

    public QueryIteratorMaterialize(QueryIterator qIter, ExecutionContext execCxt) {
        this(qIter, execCxt, null);
    }

    /** Ctor with a fixed threshold policy. */
    public QueryIteratorMaterialize(QueryIterator qIter, ExecutionContext execCxt, ThresholdPolicy<Binding> thresholdPolicy) {
        super(qIter);
        this.execCxt = execCxt;
        this.thresholdPolicy = thresholdPolicy;
    }

    /**
     * Get the threshold policy.
     * May return null if it was not initialized yet.
     * Call {@link #hasNext()} to force initialization.
     */
    public ThresholdPolicy<Binding> getThresholdPolicy() {
        return thresholdPolicy;
    }

    @Override
    protected boolean hasNextBinding() {
        collect();
        return outputIt.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        collect();
        Binding b = outputIt.next();
        return b;
    }

    protected void collect() {
        if (outputIt == null) {
            Context cxt = execCxt.getContext();
            if (thresholdPolicy == null) {
                thresholdPolicy = ThresholdPolicyFactory.policyFromContext(cxt);
            }
            DataBag<Binding> db = BagFactory.newDefaultBag(thresholdPolicy, SerializationFactoryFinder.bindingSerializationFactory());
            try {
                db.addAll(iterator);
            } finally {
                iterator.close();
            }
            outputIt = QueryIterPlainWrapper.create(db.iterator(), execCxt);
        }
    }

    @Override
    protected void closeIterator() {
        // If the output iterator is set, then the input iterator has been consumed and closed.
        if (outputIt != null) {
            outputIt.close();
        } else {
            // Output iterator was not created -> close the input iterator.
            super.closeIterator();
        }
    }
}
