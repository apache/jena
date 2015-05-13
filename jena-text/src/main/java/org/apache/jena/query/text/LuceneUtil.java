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

package org.apache.jena.query.text;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import java.lang.reflect.Constructor;
import java.util.Hashtable;

public class LuceneUtil {

    private static Hashtable<String, Class> analyzersClasses; //mapping between ISO2-letter language and lucene existing analyzersClasses
    private static Hashtable<String, Analyzer> cache = new Hashtable<>(); //to avoid unnecessary multi instantiation

    static {
        initAnalyzerDefs();
    }

    public static Analyzer getLocalizedAnalyzer(String lang) {
        return getLocalizedAnalyzer(lang, TextIndexLucene.VER);
    }

    public static Analyzer getLocalizedAnalyzer(String lang, Version ver) {
        lang = getISO2Language(lang);
        if (lang == null)
            return null;

        if (cache.containsKey(lang))
            return cache.get(lang);

        try {
            Class<?> className = analyzersClasses.get(lang);
            if (className == null)
                return null;
            Constructor constructor = className.getConstructor(Version.class);
            Analyzer analyzer = (Analyzer)constructor.newInstance(ver);
            cache.put(lang, analyzer);
            return analyzer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getISO2Language(String lang) {
        if (lang != null) {
            lang = lang.split("-")[0].toLowerCase();
            if (lang.length() == 2)
                return lang;
            else {
                if ("ara".equals(lang)) return "ar";
                if ("bul".equals(lang)) return "bg";
                if ("cat".equals(lang)) return "ca";
                if ("ces".equals(lang)) return "cs";
                if ("cze".equals(lang)) return "cs";
                if ("dan".equals(lang)) return "da";
                if ("deu".equals(lang)) return "de";
                if ("ger".equals(lang)) return "de";
                if ("ell".equals(lang)) return "el";
                if ("gre".equals(lang)) return "el";
                if ("eng".equals(lang)) return "en";
                if ("spa".equals(lang)) return "es";
                if ("eus".equals(lang)) return "eu";
                if ("baq".equals(lang)) return "eu";
                if ("fas".equals(lang)) return "fa";
                if ("per".equals(lang)) return "fa";
                if ("fin".equals(lang)) return "fi";
                if ("fra".equals(lang)) return "fr";
                if ("fre".equals(lang)) return "fr";
                if ("gle".equals(lang)) return "ga";
                if ("glg".equals(lang)) return "gl";
                if ("hin".equals(lang)) return "hi";
                if ("hun".equals(lang)) return "hu";
                if ("hye".equals(lang)) return "hy";
                if ("arm".equals(lang)) return "hy";
                if ("ind".equals(lang)) return "id";
                if ("ita".equals(lang)) return "it";
                if ("jpn".equals(lang)) return "jp";
                if ("kor".equals(lang)) return "ko";
                if ("lav".equals(lang)) return "lv";
                if ("nld".equals(lang)) return "nl";
                if ("dut".equals(lang)) return "nl";
                if ("nor".equals(lang)) return "no";
                if ("por".equals(lang)) return "pt";
                if ("ron".equals(lang)) return "ro";
                if ("rum".equals(lang)) return "ro";
                if ("rus".equals(lang)) return "ru";
                if ("swe".equals(lang)) return "sv";
                if ("tha".equals(lang)) return "th";
                if ("tur".equals(lang)) return "tr";
                if ("zho".equals(lang)) return "zh";
                if ("chi".equals(lang)) return "zh";
            }
        }

        return null;
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
