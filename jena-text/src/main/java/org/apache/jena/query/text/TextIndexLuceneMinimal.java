package org.apache.jena.query.text;

import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.util.Hashtable;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-11-12
 */
public class TextIndexLuceneMinimal extends TextIndexLucene {

    Hashtable config;

    public TextIndexLuceneMinimal(Hashtable cfg, EntityDefinition def) {
        super(new RAMDirectory(), def);
        this.config = cfg;
    }

    public String getDefaultGraphName() {
        return (String)config.get("defaultGraphName");
    }

    public File getIndexesDirectory() {
        return (File)config.get("indexesDirectory");
    }
}
