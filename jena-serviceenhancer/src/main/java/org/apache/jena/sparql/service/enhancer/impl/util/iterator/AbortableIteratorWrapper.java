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

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.sparql.serializer.SerializationContext;

public class AbortableIteratorWrapper<T>
    extends AbortableIteratorBase<T>
{
    private AbortableIterator<T> delegate;

    public AbortableIteratorWrapper(AbortableIterator<T> delegate) {
        this.delegate = delegate;
    }

    protected AbortableIterator<T> getDelegate() {
        return delegate;
    }

    @Override
    protected boolean hasNextBinding() {
        return getDelegate().hasNext();
    }

    @Override
    protected T moveToNextBinding() {
        return getDelegate().nextBinding();
    }

    @Override
    protected void closeIterator() {
        AbortableIterator<T> d = getDelegate();
        if (d != null) {
            d.close();
        }
    }

    @Override
    protected void requestCancel() {
        AbortableIterator<T> d = getDelegate();
        if (d != null) {
            d.cancel();
        }
    }

    @Override
    public void output(IndentedWriter out) {
        getDelegate().output(out);
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        out.println(Lib.className(this) + "/" + Lib.className(getDelegate()));
        out.incIndent();
        getDelegate().output(out, sCxt);
        out.decIndent();
        // out.println(Utils.className(this)+"/"+Utils.className(iterator)) ;
    }
}
