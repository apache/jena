package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public class JsonLDTripleWriter<TKey> extends AbstractWholeFileTripleWriter<TKey> {

    public JsonLDTripleWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected Lang getRdfLanguage() {
        return RDFLanguages.JSONLD;
    }

}
