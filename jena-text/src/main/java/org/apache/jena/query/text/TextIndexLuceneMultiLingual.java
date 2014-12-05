package org.apache.jena.query.text;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: amiara
 * Date: 13-11-12
 */
public class TextIndexLuceneMultiLingual extends TextIndexLucene {

    Hashtable<String, TextIndex> indexes;

    public TextIndexLuceneMultiLingual(File directory, EntityDefinition def, HashSet languages) {
        super(new RAMDirectory(), def);

        indexes = new Hashtable<String, TextIndex>();

        try {
            //default index
            File indexDir = directory;
            Directory dir = FSDirectory.open(indexDir);
            TextIndex index = new TextIndexLucene(dir, def);
            indexes.put("default", index);

            //language indexes
            for (Iterator it = languages.iterator(); it.hasNext();) {
                String lang = (String)it.next();
                indexDir = new File(directory, lang );
                dir = FSDirectory.open(indexDir);
                index = new TextIndexLucene(dir, def, lang);
                indexes.put(lang, index);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<TextIndex> getIndexes() {
        return indexes.values();
    }

    TextIndex getIndex(String lang) {
        if (lang == null)
            return indexes.get("default");
        else
            return indexes.get(lang);
    }

    public void startIndexing() {
        if (indexes == null)
            return;
        for (TextIndex index : indexes.values() )
            index.startIndexing();
    }

    public void finishIndexing() {
        if (indexes == null)
            return;
        for (TextIndex index : indexes.values() )
            index.finishIndexing();
    }

    public void addEntity(Entity entity, String lang) {
        lang = ("".equals(lang))?"default":lang.toLowerCase().substring(0, 2);
        indexes.get(lang).addEntity(entity) ;
    }

    public void deleteEntity(Entity entity, String lang) {
        lang = ("".equals(lang))?"default":lang.toLowerCase().substring(0, 2);
        indexes.get(lang).deleteEntity(entity) ;
    }

}
