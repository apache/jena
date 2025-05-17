package org.apache.jena.query;

import java.util.Objects;

/**
 * A record for holding a query object or a query string.
 *
 * If both are provided then the string should be the original string that was passed to the parser.
 * In that case, {@link #toString()} returns the formatted string obtained from the query object.
 */
//FIXME Experimental; subject to removal.
public record QueryElt(Query query, String queryString) {
    public QueryElt(Query query, String queryString) {
        if (query == null && queryString == null) {
            throw new IllegalArgumentException("At least one argument must not be null.");
        }
        this.query = query;
        this.queryString = queryString;
    }

    public QueryElt(Query query)        { this(Objects.requireNonNull(query), null); }
    public QueryElt(String queryString) { this(null, Objects.requireNonNull(queryString)); }

    boolean isParsed() { return query() != null; }

    /** If the query is parsed then return the formatted string. Otherwise return the given query string. */
    @Override
    public String toString() {
        return isParsed()
                ? query().toString()
                : queryString();
    }
}
