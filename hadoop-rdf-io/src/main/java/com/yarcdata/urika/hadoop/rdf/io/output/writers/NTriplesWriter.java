/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.out.CharSpace;
import org.apache.jena.riot.out.NodeFormatterNT;

/**
 * A record writer for NTriples
 * 
 * @author rvesse
 * @param <TKey>
 *            Key type
 * 
 */
public class NTriplesWriter<TKey> extends AbstractLineBasedTripleWriter<TKey> {

    /**
     * Creates a new writer
     * 
     * @param writer
     *            Writer
     */
    public NTriplesWriter(Writer writer) {
        super(writer, new NodeFormatterNT());
    }

    /**
     * Creates a new writer using the given character space
     * 
     * @param writer
     *            Writer
     * @param charSpace
     *            Character space
     */
    public NTriplesWriter(Writer writer, CharSpace charSpace) {
        super(writer, new NodeFormatterNT(charSpace));
    }
}
