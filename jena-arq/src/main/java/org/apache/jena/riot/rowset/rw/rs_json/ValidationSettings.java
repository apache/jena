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

package org.apache.jena.riot.rowset.rw.rs_json;

import java.io.Serializable;

/** Validation settings class */
public class ValidationSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * What to do if the JSON is effectively 'empty', i.e. if neither
     * the head nor the results key were present.
     * Unexpected elements are captured by onUnexpectedJsonElement.
     * e.g. returned older version of virtuoso open source
     * Mitigation is to assume an empty set of bindings.
     */
    protected Severity emptyJsonSeverity = Severity.ERROR;

    /** What to do if no head was encountered. We may have already
     * optimistically streamed all the bindings in anticipation of an
     * eventual head. */
    protected Severity missingHeadSeverity = Severity.ERROR;

    /** What to do if there is a repeated 'results' key
     * At this stage we have already optimisticaly streamed results which
     * under JSON semantics would have been superseded by this newly
     * encountered key. */
    protected Severity invalidatedResultsSeverity = Severity.ERROR;

    /** What to do if there is a repeated 'head' <b>whole value does not match the
     * prior value</b>. Repeated heads with the same value are valid.
     * Any possibly prior reported head would have been superseded by this newly
     * encountered key.
     * Should parsing continue then only the first encountered value will remain active.
     */
    protected Severity invalidatedHeadSeverity = Severity.FATAL;

    /**
     * What to do if the JSON contains both a boolean result and bindings
     * Mitigation is to assume bindings and ignore the boolean result
     */
    protected Severity mixedResultsSeverity = Severity.FATAL;

    /** What to do if we encounter an unexpected JSON key */
    protected Severity unexpectedJsonElementSeverity = Severity.IGNORE;

    public Severity getEmptyJsonSeverity() {
        return emptyJsonSeverity;
    }

    public void setEmptyJsonSeverity(Severity severity) {
        this.emptyJsonSeverity = severity;
    }

    public Severity getInvalidatedHeadSeverity() {
        return invalidatedHeadSeverity;
    }

    public void setInvalidatedHeadSeverity(Severity severity) {
        this.invalidatedHeadSeverity = severity;
    }

    public Severity getInvalidatedResultsSeverity() {
        return invalidatedResultsSeverity;
    }

    public void setInvalidatedResultsSeverity(Severity severity) {
        this.invalidatedResultsSeverity = severity;
    }

    public Severity getMissingHeadSeverity() {
        return missingHeadSeverity;
    }

    public void setMissingHeadSeverity(Severity severity) {
        this.missingHeadSeverity = severity;
    }

    public Severity getMixedResultsSeverity() {
        return mixedResultsSeverity;
    }

    public void setMixedResultsSeverity(Severity severity) {
        this.mixedResultsSeverity = severity;
    }

    public Severity getUnexpectedJsonElementSeverity() {
        return unexpectedJsonElementSeverity;
    }

    public void setUnexpectedJsonElementSeverity(Severity severity) {
        this.unexpectedJsonElementSeverity = severity;
    }
}