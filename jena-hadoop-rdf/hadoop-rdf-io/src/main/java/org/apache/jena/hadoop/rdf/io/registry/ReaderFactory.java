package org.apache.jena.hadoop.rdf.io.registry;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * Interface for reader factories
 * 
 */
public interface ReaderFactory {

    /**
     * Gets the primary language this factory produces readers for
     * 
     * @return Primary language
     */
    public abstract Lang getPrimaryLanguage();

    /**
     * Gets the alternative languages this factory can produce readers for
     * 
     * @return Alternative languages
     */
    public abstract Collection<Lang> getAlternativeLanguages();

    /**
     * Gets whether this factory can produce readers that are capable of reading
     * quads
     * 
     * @return True if quads can be read, false if not
     */
    public abstract boolean canReadQuads();

    /**
     * Gets whether this factory can produce readers that are capable of reading
     * triples
     * 
     * @return True if triples can be read, false if not
     */
    public abstract boolean canReadTriples();

    /**
     * Creates a quad reader
     * 
     * @return Quad reader
     * @throws IOException
     *             May be thrown if a quad reader cannot be created
     */
    public abstract RecordReader<LongWritable, QuadWritable> createQuadReader() throws IOException;

    /**
     * Creates a triples reader
     * 
     * @return Triples reader
     * @throws IOException
     *             May be thrown if a triple reader cannot be created
     */
    public abstract RecordReader<LongWritable, TripleWritable> createTripleReader() throws IOException;
}
