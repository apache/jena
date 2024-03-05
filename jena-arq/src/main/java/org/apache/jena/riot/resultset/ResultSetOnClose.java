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

package org.apache.jena.riot.resultset;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetWrapper;

/**
 * Perform an action on a result set when iteration finishes
 * or {@code close()} is called.
 */
public class ResultSetOnClose extends ResultSetWrapper {
    private final Runnable onClose;
    private boolean isClosed = false;

    public ResultSetOnClose(ResultSet rs, Runnable onClose) {
        super(rs);
        this.onClose = onClose;
    }

    private void onClose() {
        if ( isClosed )
            return;
        isClosed = true;
        onClose.run();
    }

    @Override
    public boolean hasNext() {
        boolean b = super.hasNext();
        if ( !b )
            onClose();
        return b;
    }

    @Override
    public QuerySolution next() {
        try { return super.next(); }
        catch (NoSuchElementException ex) { onClose(); throw ex; }
    }

    @Override
    public void forEachRemaining(Consumer<? super QuerySolution> action) {
        super.forEachRemaining(action);
        onClose();
    }

    @Override
    public QuerySolution nextSolution() {
        try { return super.nextSolution(); }
        catch (NoSuchElementException ex) { onClose(); throw ex; }
    }

    @Override
    public Binding nextBinding() {
        try { return super.nextBinding(); }
        catch (NoSuchElementException ex) { onClose(); throw ex; }
    }

    @Override
    public void close() {
        super.close();
        onClose();
    }
}