/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

package org.apache.jena.query.text.filter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link SelectiveFoldingFilter}.
 */

public class TestSelectiveFoldingFilter {

    private StringReader inputText;
    private CharArraySet whitelisted;

    @Before
    public void setUp() {
        inputText = new StringReader("Señora Siobhán, look at that façade");
    }

    /**
     * An empty white list means that the default behaviour of the Lucene's ASCIIFoldingFilter applies.
     * @throws IOException from Lucene API
     */
    @Test
    public void testEmptyWhiteListIsOkay() throws IOException {
        whitelisted = new CharArraySet(Collections.emptyList(), false);
        List<String> tokens = collectTokens(inputText, whitelisted);
        List<String> expected = Arrays.asList("Senora", "Siobhan", "look", "at", "that", "facade");
        assertTrue(tokens.equals(expected));
    }

    @Test
    public void testSingleCharacterWhiteListed() throws IOException {
        whitelisted = new CharArraySet(Arrays.asList("ç"), false);
        List<String> tokens = collectTokens(inputText, whitelisted);
        List<String> expected = Arrays.asList("Senora", "Siobhan", "look", "at", "that", "façade");
        assertTrue(tokens.equals(expected));
    }

    @Test
    public void testCompleteWhiteListed() throws IOException {
        whitelisted = new CharArraySet(Arrays.asList("ñ", "á", "ç"), false);
        List<String> tokens = collectTokens(inputText, whitelisted);
        // here we should have the complete input
        List<String> expected = Arrays.asList("Señora", "Siobhán", "look", "at", "that", "façade");
        assertTrue(tokens.equals(expected));
    }

    @Test
    public void testCaseMatters() throws IOException {
        // note the first capital letter
        whitelisted = new CharArraySet(Arrays.asList("Ñ", "á", "ç"), false);
        List<String> tokens = collectTokens(inputText, whitelisted);
        List<String> expected = Arrays.asList("Senora", "Siobhán", "look", "at", "that", "façade");
        assertTrue(tokens.equals(expected));
    }

    @Test
    public void testMismatchWhiteList() throws IOException {
        whitelisted = new CharArraySet(Arrays.asList("ú", "ć", "ž"), false);
        List<String> tokens = collectTokens(inputText, whitelisted);
        List<String> expected = Arrays.asList("Senora", "Siobhan", "look", "at", "that", "facade");
        assertTrue(tokens.equals(expected));
    }

    @Test(expected = NullPointerException.class)
    public void testNullWhiteListThrowsError() throws IOException {
        collectTokens(inputText, null);
    }

    @Test
    public void testEmptyInput() throws IOException {
        whitelisted = new CharArraySet(Arrays.asList("ç"), false);
        inputText = new StringReader("");
        List<String> tokens = collectTokens(inputText, whitelisted);
        List<String> expected = Collections.emptyList();
        assertTrue(tokens.equals(expected));
    }

    /**
     * Return the list of CharTermAttribute converted to a list of String's.
     *
     * @param whitelisted white-list
     * @return list of CharTermAttribute converted to a list of String's
     * @throws IOException from Lucene API
     */
    private List<String> collectTokens(StringReader inputText, CharArraySet whitelisted) throws IOException {
        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(inputText);

        SelectiveFoldingFilter selectiveFoldingFilter = new SelectiveFoldingFilter(tokenizer, whitelisted);

        CharTermAttribute termAttrib = (CharTermAttribute) selectiveFoldingFilter.getAttribute(CharTermAttribute.class);

        selectiveFoldingFilter.reset();
        List<String> tokens = new ArrayList<>();
        while (selectiveFoldingFilter.incrementToken()) {
            tokens.add(termAttrib.toString());
        }
        selectiveFoldingFilter.end();
        selectiveFoldingFilter.close();
        return tokens;
    }
}
