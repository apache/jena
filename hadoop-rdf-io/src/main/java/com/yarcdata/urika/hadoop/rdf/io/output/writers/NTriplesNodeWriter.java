/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.out.CharSpace;
import org.apache.jena.riot.out.NodeFormatterNT;

/**
 * A NTriples based node writer
 * 
 * @author rvesse
 * 
 * @param <TValue>
 *            Value type
 */
public class NTriplesNodeWriter<TValue> extends AbstractNodeWriter<TValue> {

    /**
     * Creates a new writer
     * 
     * @param writer
     *            Writer
     */
    public NTriplesNodeWriter(Writer writer) {
        super(writer);
    }

    /**
     * Creates a new writer
     * 
     * @param writer
     *            Writer
     * @param charSpace
     *            Character space to use
     */
    public NTriplesNodeWriter(Writer writer, CharSpace charSpace) {
        super(writer, new NodeFormatterNT(charSpace));
    }

}
