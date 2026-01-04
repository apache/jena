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

package org.apache.jena.http.sys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/** Accumulator for update elements. Can build an overall string or UpdateRequest from the elements. */
public class UpdateEltAcc implements Iterable<UpdateElt> {
    /** Delimiter for joining multiple SPARQL update strings into a single one.
      * The delimiter takes into account that the last line of a statement may be a single-line-comment. */
    public static final String DELIMITER = "\n;\n";

    private List<UpdateElt> updateOperations = new ArrayList<>();
    private List<UpdateElt> updateOperationsView = Collections.unmodifiableList(updateOperations);
    private boolean isParsed = true; // True iff there are no strings in updateOperations

    public boolean isParsed() {
        return isParsed;
    }

    public void add(UpdateElt updateElt) {
        isParsed = isParsed && updateElt.isParsed();
        updateOperations.add(updateElt);
    }

    public void add(Update update) {
        add(new UpdateElt(update));
    }

    /** Add a string by parsing it. */
    public void add(String updateRequestString) {
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestString);
        add(updateRequest);
    }

    public void add(UpdateRequest updateRequest) {
        updateRequest.getOperations().forEach(this::add);
    }

    /** Add a string without parsing it. */
    public void addString(String updateRequestString) {
        add(new UpdateElt(updateRequestString));
    }

    /** Attempt to build an UpdateRequest from the state of this accumulator. Attempts to parse any string elements. */
    public UpdateRequest buildUpdateRequest() {
        return addToUpdateRequest(new UpdateRequest());
    }

    public UpdateRequest addToUpdateRequest(UpdateRequest updateRequest) {
        for (UpdateElt elt : updateOperations) {
            if (elt.isParsed()) {
                updateRequest.add(elt.update());
            } else {
                try {
                    updateRequest.add(elt.updateString());
                } catch (Exception e) {
                    // Expose the string that failed to parse
                    e.addSuppressed(new RuntimeException("Failed to parse: " + elt.updateString()));
                    throw e;
                }
            }
        }
        return updateRequest;
    }

    public void clear() {
        updateOperations.clear();
        isParsed = true;
    }

    public boolean isEmpty() {
        return updateOperations.isEmpty();
    }

    @Override
    public Iterator<UpdateElt> iterator() {
        return updateOperationsView.iterator();
    }

    public String buildString() {
        return updateOperations.stream()
            .map(UpdateElt::toString)
            .collect(Collectors.joining(DELIMITER));
    }
}
