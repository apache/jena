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

package org.apache.jena.riot.rowset;

import java.util.NoSuchElementException;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

/**
 * Perform an action on a row set when iteration finishes
 * or {@code close()} is called.
 */
public class RowSetOnClose extends RowSetWrapper {
    private final Runnable onClose;
    private boolean isClosed = false;

    public RowSetOnClose(RowSet rs, Runnable onClose) {
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
    public Binding next() {
        try { return super.next(); }
        catch (NoSuchElementException ex) { onClose(); throw ex; }
    }

    @Override
    public void close() {
        super.close();
        onClose();
    }
}