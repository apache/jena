package org.apache.jena.update;

import java.util.Objects;

/**
 * A record for holding an update request object or a update request string.
 *
 * If both are provided then the string should be the original string that was passed to the parser.
 * In that case, {@linkplain #toString()} returns the formatted string obtained from the update request object.
 */
// FIXME Experimental; subject to removal.
public record UpdateRequestElt(UpdateRequest updateRequest, String updateRequestString) {
    public UpdateRequestElt(UpdateRequest updateRequest, String updateRequestString) {
        if (updateRequest == null && updateRequestString == null) {
            throw new IllegalArgumentException("At least one argument must not be null.");
        }
        this.updateRequest = updateRequest;
        this.updateRequestString = updateRequestString;
    }

    public UpdateRequestElt(UpdateRequest updateRequest) { this(Objects.requireNonNull(updateRequest), null); }
    public UpdateRequestElt(String updateRequestString)  { this(null, Objects.requireNonNull(updateRequestString)); }

    boolean isParsed() { return updateRequest() != null; }

    /**
     * If the update request is parsed then return the formatted string.
     * Otherwise return the given update request string.
     */
    @Override
    public String toString() {
        return isParsed()
                ? updateRequest().toString()
                : updateRequestString();
    }
}
