package org.apache.jena.riot.lang;

import static org.apache.jena.riot.Lang.JSONLD11;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.util.Context;
import org.junit.Test;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;

public class TestLangJSONLD11 {

    private static final String CONTENT = "";

    @Test
    public void testGetJsonLdOptions() {
        RDFParserBuilder.create().context(setupContext()).lang(JSONLD11).fromString(CONTENT).build();

        assertEquals("Custom DocumentLoader wasn't called to handle loading", 1, TestDocumentLoader.COUNTER);
    }

    private final Context setupContext() {
        TestDocumentLoader loader = new TestDocumentLoader();
        JsonLdOptions opts = new JsonLdOptions();

        opts.setDocumentLoader(loader);

        Context context = new Context();
        context.set(LangJSONLD11.JSONLD_OPTIONS, opts);

        return context;
    }

    private final static class TestDocumentLoader implements DocumentLoader {

        public static int COUNTER = 0;

        @Override
        public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
            DocumentLoader loader = HttpLoader.defaultInstance();

            COUNTER++;

            return loader.loadDocument(url, options);
        }
    }
}
