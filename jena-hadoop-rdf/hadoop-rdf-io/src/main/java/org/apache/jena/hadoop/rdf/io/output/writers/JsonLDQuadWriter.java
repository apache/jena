package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public class JsonLDQuadWriter<TKey> extends AbstractWholeFileQuadWriter<TKey> {

    public JsonLDQuadWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected Lang getRdfLanguage() {
        return RDFLanguages.JSONLD;
    }

}
