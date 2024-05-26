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

public class PatchSummary {
    public long countStart        = 0;
    public long countFinish       = 0;
    public long countHeader       = 0;
    public long countAddData      = 0;
    public long countDeleteData   = 0;
    public long countAddPrefix    = 0;
    public long countDeletePrefix = 0;
    public long countTxnBegin     = 0;
    public long countTxnCommit    = 0;
    public long countTxnAbort     = 0;
    public long countSegment      = 0;

    public PatchSummary() {}

    public void reset() {
        countStart        = 0;
        countFinish       = 0;
        countHeader       = 0;
        countAddData      = 0;
        countDeleteData   = 0;
        countAddPrefix    = 0;
        countDeletePrefix = 0;
        countTxnBegin     = 0;
        countTxnCommit    = 0;
        countTxnAbort     = 0;
        countSegment      = 0;
    }

    @Override
    public PatchSummary clone() {
        PatchSummary other = new PatchSummary();
        other.countStart        = this.countStart;
        other.countFinish       = this.countFinish;
        other.countHeader       = this.countHeader;
        other.countAddData      = this.countAddData;
        other.countDeleteData   = this.countDeleteData;
        other.countAddPrefix    = this.countAddPrefix;
        other.countDeletePrefix = this.countDeletePrefix;
        other.countTxnBegin     = this.countTxnBegin;
        other.countTxnCommit    = this.countTxnCommit;
        other.countTxnAbort     = this.countTxnAbort;
        other.countSegment      = this.countSegment;
        return other;
    }

    public long getCountStart() {
        return countStart;
    }

    public long getCountFinish() {
        return countFinish;
    }

    public long getDepth() {
        return countStart - countFinish;
    }

    public long getCountHeader() {
        return countHeader;
    }

    public long getCountAddData() {
        return countAddData;
    }

    public long getCountDeleteData() {
        return countDeleteData;
    }

    public long getCountAddPrefix() {
        return countAddPrefix;
    }

    public long getCountDeletePrefix() {
        return countDeletePrefix;
    }

    public long getCountTxnBegin() {
        return countTxnBegin;
    }

    public long getCountTxnCommit() {
        return countTxnCommit;
    }

    public long getCountTxnAbort() {
        return countTxnAbort;
    }

    public long getCountSegment() {
        return countSegment;
    }
}

