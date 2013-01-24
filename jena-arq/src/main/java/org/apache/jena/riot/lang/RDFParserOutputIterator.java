/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package org.apache.jena.riot.lang;

import java.util.Iterator;

import org.apache.jena.riot.system.StreamRDF;

/**
 * Interface for RDF streams which are also iterators
 *
 * @param <T>
 */
public interface RDFParserOutputIterator<T> extends StreamRDF, Iterator<T> {

}
