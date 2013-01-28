/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package org.apache.jena.riot.system;

import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Interface for lightweight prefix maps, this is similar to
 * {@link PrefixMapping} but it omits any reverse lookup functionality. The
 * contract does not require an implementation to do any validation unlike a
 * {@link PrefixMapping} which require validation of prefixes
 * 
 */
public interface LightweightPrefixMap {

    /** return the underlying mapping - do not modify */
    public abstract Map<String, IRI> getMapping();

    /** return a copy of the underlying mapping */
    public abstract Map<String, IRI> getMappingCopy();

    public abstract Map<String, String> getMappingCopyStr();

    /** Add a prefix, overwrites any existing association */
    public abstract void add(String prefix, String iriString);

    /** Add a prefix, overwrites any existing association */
    public abstract void add(String prefix, IRI iri);

    /** Add a prefix, overwrites any existing association */
    public abstract void putAll(PrefixMap pmap);

    /** Add a prefix, overwrites any existing association */
    public abstract void putAll(PrefixMapping pmap);

    /** Delete a prefix */
    public abstract void delete(String prefix);

    public abstract boolean contains(String prefix);

    /** Abbreviate an IRI or return null */
    public abstract String abbreviate(String uriStr);

    /**
     * Abbreviate an IRI or return a pair of prefix and local parts.
     * 
     * @param uriStr
     *            URI string to abbreviate
     * @see #abbreviate
     */
    public abstract Pair<String, String> abbrev(String uriStr);

    /** Expand a prefix named, return null if it can't be expanded */
    public abstract String expand(String prefixedName);

    /** Expand a prefix, return null if it can't be expanded */
    public abstract String expand(String prefix, String localName);

}