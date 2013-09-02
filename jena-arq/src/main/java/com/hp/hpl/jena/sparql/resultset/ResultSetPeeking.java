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

package com.hp.hpl.jena.sparql.resultset;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.atlas.lib.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * A wrapper around another result set that provides peek capabilities
 * 
 */
public class ResultSetPeeking implements ResultSetPeekable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultSetPeekable.class);

    /**
     * Controls whether a log warning is printed if someone modifies the
     * underlying result set externally to us
     */
    public static boolean warnOnSyncErrors = true;

    private ResultSet results;
    private Model model;
    private Binding peeked = null;
    private int rowNumber = 0;

    /**
     * Creates a peeking wrapper around another result set
     * 
     * @param results
     *            Inner results
     */
    public ResultSetPeeking(ResultSet results) {
        if (results == null)
            throw new IllegalArgumentException("Inner result set cannot be null");
        this.results = results;
        this.model = results.getResourceModel();
    }

    @Override
    public boolean hasNext() {
        if (this.hasPeeked()) {
            return true;
        } else {
            return this.canPeek();
        }        
    }

    @Override
    public QuerySolution next() {
        return new ResultBinding(this.model, this.nextBinding());
    }

    @Override
    public QuerySolution nextSolution() {
        return this.next();
    }

    @Override
    public Binding nextBinding() {
        if (this.hasPeeked()) {
            Binding b = this.peeked;
            this.peeked = null;
            this.rowNumber++;
            return b;
        } else if (this.canPeek()) {
            Binding b = this.peekBinding();
            this.peeked = null;
            this.rowNumber++;
            return b;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int getRowNumber() {
        // Calculate row number based on whether we have peeked
        return this.hasPeeked() ? this.results.getRowNumber() - 1 : this.results.getRowNumber();
    }

    @Override
    public List<String> getResultVars() {
        return this.results.getResultVars();
    }

    @Override
    public Model getResourceModel() {
        return this.model;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

    private boolean hasPeeked() {
        int diff = this.results.getRowNumber() - this.rowNumber;
        if (diff == 0) {
            // If no difference we have not peeked
            return false;
        } else if (diff == 1 && this.peeked != null) {
            // If difference is one then we have peeked
            return true;
        } else if (diff >= 1) {
            // If difference between what we think the row number is and that of
            // the underlying result set is > 1 then someone has moved positions
            // in the underlying result set independently
            // Sync up with current position and report false
            if (warnOnSyncErrors)
                LOGGER.warn("Underlying result set was moved forward " + (diff - 1)
                        + " result(s), this result set was synced back up but some results have been missed");
            this.rowNumber = this.results.getRowNumber();
            this.peeked = null;
            return false;
        } else {
            // If difference is negative then someone has reset the underlying
            // result set so we are completely out of sync, syncing back up at
            // this point would be illegal since we have gone backwards in the
            // stream
            throw new IllegalStateException(
                    "Underlying result set position has moved backwards, this result set is no longer usable");
        }
    }
    
    /**
     * Gets whether we can peek
     * @return True if we can peek, false otherwise
     */
    private boolean canPeek() {
        return this.results.hasNext();
    }

    @Override
    public QuerySolution peek() {
        return new ResultBinding(this.model, this.peekBinding());
    }

    @Override
    public Binding peekBinding() {
        if (this.hasPeeked()) {
            return this.peeked;
        } else if (this.canPeek()) {
            this.peeked = this.results.nextBinding();
            return this.peeked;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void close() {
        if (this.results instanceof Closeable) {
            ((Closeable)this.results).close();
        }
    }

}
