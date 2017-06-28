/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.query.text.analyzer;

import org.apache.jena.rdf.model.Resource;
import org.apache.lucene.analysis.Analyzer;
import java.lang.reflect.Constructor;
import java.util.Hashtable;

public class Util {

    private static Hashtable<String, Class<?>> analyzersClasses; //mapping between ISO2-letter language and lucene existing analyzersClasses
    private static Hashtable<String, Analyzer> cache = new Hashtable<>(); //to avoid unnecessary multi instantiation
    
    // cache of defined text:defineAnalyzers
    private static Hashtable<String, Analyzer> definedAnalyzers = new Hashtable<>();

    static {
        initAnalyzerDefs();
    }

    public static Analyzer getLocalizedAnalyzer(String lang) {
        if (lang == null)
            return null;

        if (cache.containsKey(lang))
            return cache.get(lang);

        try {
            Class<?> className = analyzersClasses.get(lang);
            if (className == null)
                return null;
            Constructor<?> constructor = className.getConstructor();
            Analyzer analyzer = (Analyzer)constructor.newInstance();
            cache.put(lang, analyzer);
            return analyzer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void addAnalyzer(String lang, Analyzer analyzer) {
        cache.put(lang, analyzer);
    }
    
    public static Analyzer getDefinedAnalyzer(Resource key) {
        return definedAnalyzers.get(key.getURI());
    }
    
    public static void defineAnalyzer(Resource key, Analyzer analyzer) {
        definedAnalyzers.put(key.getURI(), analyzer);
    }

    private static void initAnalyzerDefs() {
        analyzersClasses = new Hashtable<>();
        analyzersClasses.put("ar", org.apache.lucene.analysis.ar.ArabicAnalyzer.class);
        analyzersClasses.put("bg", org.apache.lucene.analysis.bg.BulgarianAnalyzer.class);
        analyzersClasses.put("ca", org.apache.lucene.analysis.ca.CatalanAnalyzer.class);
        analyzersClasses.put("cs", org.apache.lucene.analysis.cz.CzechAnalyzer.class);
        analyzersClasses.put("da", org.apache.lucene.analysis.da.DanishAnalyzer.class);
        analyzersClasses.put("de", org.apache.lucene.analysis.de.GermanAnalyzer.class);
        analyzersClasses.put("el", org.apache.lucene.analysis.el.GreekAnalyzer.class);
        analyzersClasses.put("en", org.apache.lucene.analysis.en.EnglishAnalyzer.class);
        analyzersClasses.put("es", org.apache.lucene.analysis.es.SpanishAnalyzer.class);
        analyzersClasses.put("eu", org.apache.lucene.analysis.eu.BasqueAnalyzer.class);
        analyzersClasses.put("fa", org.apache.lucene.analysis.fa.PersianAnalyzer.class);
        analyzersClasses.put("fi", org.apache.lucene.analysis.fi.FinnishAnalyzer.class);
        analyzersClasses.put("fr", org.apache.lucene.analysis.fr.FrenchAnalyzer.class);
        analyzersClasses.put("ga", org.apache.lucene.analysis.ga.IrishAnalyzer.class);
        analyzersClasses.put("gl", org.apache.lucene.analysis.gl.GalicianAnalyzer.class);
        analyzersClasses.put("hi", org.apache.lucene.analysis.hi.HindiAnalyzer.class);
        analyzersClasses.put("hu", org.apache.lucene.analysis.hu.HungarianAnalyzer.class);
        analyzersClasses.put("hy", org.apache.lucene.analysis.hy.ArmenianAnalyzer.class);
        analyzersClasses.put("id", org.apache.lucene.analysis.id.IndonesianAnalyzer.class);
        analyzersClasses.put("it", org.apache.lucene.analysis.it.ItalianAnalyzer.class);
        analyzersClasses.put("ja", org.apache.lucene.analysis.cjk.CJKAnalyzer.class);
        analyzersClasses.put("ko", org.apache.lucene.analysis.cjk.CJKAnalyzer.class);
        analyzersClasses.put("lv", org.apache.lucene.analysis.lv.LatvianAnalyzer.class);
        analyzersClasses.put("nl", org.apache.lucene.analysis.nl.DutchAnalyzer.class);
        analyzersClasses.put("no", org.apache.lucene.analysis.no.NorwegianAnalyzer.class);
        analyzersClasses.put("pt", org.apache.lucene.analysis.pt.PortugueseAnalyzer.class);
        analyzersClasses.put("ro", org.apache.lucene.analysis.ro.RomanianAnalyzer.class);
        analyzersClasses.put("ru", org.apache.lucene.analysis.ru.RussianAnalyzer.class);
        analyzersClasses.put("sv", org.apache.lucene.analysis.sv.SwedishAnalyzer.class);
        analyzersClasses.put("th", org.apache.lucene.analysis.th.ThaiAnalyzer.class);
        analyzersClasses.put("tr", org.apache.lucene.analysis.tr.TurkishAnalyzer.class);
        analyzersClasses.put("zh", org.apache.lucene.analysis.cjk.CJKAnalyzer.class);
    }
}
