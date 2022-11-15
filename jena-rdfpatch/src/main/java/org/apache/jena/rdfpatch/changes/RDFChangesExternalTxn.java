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

package org.apache.jena.rdfpatch.changes;

import org.apache.jena.rdfpatch.PatchException;
import org.apache.jena.rdfpatch.RDFChanges;

/**
 * Wrapper for {@linkplain RDFChanges} that ignores transaction begin/commit in the patch
 * assuming the caller is handling that, for example, executing a batch of patches in a
 * single transaction.
 * <p>
 * On abort, throws {@linkplain PatchTxnAbortException} which is a
 * {@linkplain PatchException}. If used with batched transactions, the caller does not know whether
 * some patches committed and then one had a {@code TA} record.
 *
 */
public class RDFChangesExternalTxn extends RDFChangesWrapper {
    public RDFChangesExternalTxn(RDFChanges other) {
        super(other);
    }

    private int depth = 0;
    private int countBegin = 0;
    private int countCommit = 0;

    public int txnDepth() {
        return depth;
    }

    public int txnCountBegin() {
        return countBegin;
    }

    public int txnCountCommit() {
        return countCommit;
    }

    @Override
    public void txnBegin() { depth++; countBegin++; }

    @Override
    public void txnCommit() { depth--; countCommit++; }

    @Override
    public void txnAbort() {
        throw new PatchTxnAbortException();
    }

    @Override
    public void segment() {}
}