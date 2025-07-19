package org.apache.jena.http.sys;

import java.util.Objects;

import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/** Update element. Either an Update object or a string. */
record UpdateElt(Update update, String updateString) {
    UpdateElt(Update update) { this(Objects.requireNonNull(update), null); }
    UpdateElt(String updateString) { this(null, Objects.requireNonNull(updateString)); }
    boolean isParsed() { return update != null; }

    @Override
    public String toString() {
        return isParsed()
                ? new UpdateRequest(update()).toString() // Reuse UpdateRequest's serialization approach
                : updateString();
    }
}
