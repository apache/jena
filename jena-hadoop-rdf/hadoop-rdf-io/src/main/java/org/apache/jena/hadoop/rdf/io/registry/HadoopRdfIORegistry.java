package org.apache.jena.hadoop.rdf.io.registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * A registry which is used by various classes to dynamically select record
 * readers and writers based on a provided {@link Lang}
 * <p>
 * Readers and writers are dynamically discovered using the Java
 * {@link ServiceLoader} mechanism. This will look for a file under
 * {@code META-INF/services} named
 * {@code org.apache.jena.hadoop.rdf.io.registry.ReaderFactory} . This follows
 * the standard {@linkplain ServiceLoader} format of provided one class name per
 * line which implements the relevant interface.
 * </p>
 * 
 */
public class HadoopRdfIORegistry {

    private static Map<Lang, ReaderFactory> readers = new HashMap<Lang, ReaderFactory>();
    private static boolean init = false;

    static {
        init();
    }

    private static synchronized void init() {
        if (init)
            return;

        // Dynamically load and register reader factories
        ServiceLoader<ReaderFactory> readerFactoryLoader = ServiceLoader.load(ReaderFactory.class);
        Iterator<ReaderFactory> iter = readerFactoryLoader.iterator();
        while (iter.hasNext()) {
            ReaderFactory f = iter.next();
            addReaderFactory(f);
        }

        init = true;
    }

    /**
     * Resets the registry to the default configuration
     */
    public static synchronized void reset() {
        if (!init)
            return;

        init = false;
        init();
    }

    public static void addReaderFactory(ReaderFactory f) {
        if (f == null)
            throw new NullPointerException("Factory cannot be null");

        readers.put(f.getPrimaryLanguage(), f);
        for (Lang altLang : f.getAlternativeLanguages()) {
            readers.put(altLang, f);
        }
    }

    /**
     * Gets whether there is a quad reader available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasQuadReader(Lang lang) {
        if (lang == null)
            return false;

        ReaderFactory f = readers.get(lang);
        if (f == null)
            return false;
        return f.canReadQuads();
    }

    /**
     * Gets whether there is a triple reader available for the given language
     * 
     * @param lang
     *            Language
     * @return True if available, false otherwise
     */
    public static boolean hasTriplesReader(Lang lang) {
        if (lang == null)
            return false;

        ReaderFactory f = readers.get(lang);
        if (f == null)
            return false;
        return f.canReadTriples();
    }

    /**
     * Tries to create a quad reader for the given language
     * 
     * @param lang
     *            Language
     * @return Quad reader if one is available
     * @throws IOException
     *             Thrown if a quad reader is not available or the given
     *             language does not support quads
     */
    public static RecordReader<LongWritable, QuadWritable> createQuadReader(Lang lang) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a quad reader for an undefined language");

        ReaderFactory f = readers.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canReadQuads())
            throw new IOException(lang.getName() + " does not support reading quads");
        return f.createQuadReader();
    }

    /**
     * Tries to create a triple reader for the given language
     * 
     * @param lang
     *            Language
     * @return Triple reader if one is available
     * @throws IOException
     *             Thrown if a triple reader is not available or the given
     *             language does not support triple
     */
    public static RecordReader<LongWritable, TripleWritable> createTripleReader(Lang lang) throws IOException {
        if (lang == null)
            throw new IOException("Cannot create a triple reader for an undefined language");

        ReaderFactory f = readers.get(lang);
        if (f == null)
            throw new IOException("No factory registered for language " + lang.getName());
        if (!f.canReadTriples())
            throw new IOException(lang.getName() + " does not support reading triples");
        return f.createTripleReader();
    }
}
