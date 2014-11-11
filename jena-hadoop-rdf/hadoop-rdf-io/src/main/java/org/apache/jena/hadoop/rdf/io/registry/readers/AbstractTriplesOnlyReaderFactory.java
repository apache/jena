package org.apache.jena.hadoop.rdf.io.registry.readers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.hadoop.rdf.io.registry.ReaderFactory;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * Abstract reader factory for languages that only support triples
 */
public abstract class AbstractTriplesOnlyReaderFactory implements ReaderFactory {

    private Lang lang;
    private Collection<Lang> alternateLangs = Collections.unmodifiableList(Collections.<Lang>emptyList());

    public AbstractTriplesOnlyReaderFactory(Lang lang) {
        this(lang, (Collection<Lang>)null);
    }
    
    public AbstractTriplesOnlyReaderFactory(Lang lang, Lang...altLangs) {
        this(lang, Arrays.asList(altLangs));
    }

    public AbstractTriplesOnlyReaderFactory(Lang lang, Collection<Lang> altLangs) {
        this.lang = lang;
        if (altLangs != null)
            this.alternateLangs = Collections.unmodifiableCollection(altLangs);
    }

    @Override
    public final Lang getPrimaryLanguage() {
        return this.lang;
    }
    
    @Override
    public final Collection<Lang> getAlternativeLanguages() {
        return this.alternateLangs;
    }
    
    @Override
    public final boolean canReadQuads() {
        return false;
    }

    @Override
    public final boolean canReadTriples() {
        return true;
    }

    @Override
    public final RecordReader<LongWritable, QuadWritable> createQuadReader() throws IOException {
        throw new IOException(this.lang.getName() + " does not support reading quads");
    }

    @Override
    public abstract RecordReader<LongWritable, TripleWritable> createTripleReader() throws IOException;

}
