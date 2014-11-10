package org.apache.jena.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public class JsonLDQuadReader extends AbstractWholeFileQuadReader {

    @Override
    protected Lang getRdfLanguage() {
        return RDFLanguages.JSONLD;
    }

}
