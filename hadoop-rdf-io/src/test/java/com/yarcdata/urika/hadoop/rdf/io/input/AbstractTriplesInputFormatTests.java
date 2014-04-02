/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;
import java.io.Writer;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for Triple input formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractTriplesInputFormatTests extends AbstractNodeTupleInputFormatTests<Triple, TripleWritable> {

    @Override
    protected void generateTuples(Writer writer, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            writer.write("<http://subjects/" + i + "> <http://predicate> \"" + i + "\" .\n");
        }
        writer.flush();
        writer.close();
    }

    @Override
    protected void generateBadTuples(Writer writer, int num) throws IOException {
        for (int i = 0; i < num; i++) {
            writer.write("<http://broken\n");
        }
        writer.flush();
        writer.close();
    }

    @Override
    protected void generateMixedTuples(Writer writer, int num) throws IOException {
        boolean bad = false;
        for (int i = 0; i < num; i++, bad = !bad) {
            if (bad) {
                writer.write("<http://broken\n");
            } else {
                writer.write("<http://subjects/" + i + "> <http://predicate> \"" + i + "\" .\n");
            }
        }
        writer.flush();
        writer.close();
    }

}
