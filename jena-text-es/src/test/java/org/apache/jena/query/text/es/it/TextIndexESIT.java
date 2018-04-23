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
package org.apache.jena.query.text.es.it;

import org.apache.jena.graph.Node;
import org.apache.jena.query.text.Entity;
import org.apache.jena.query.text.TextHit;
import org.apache.jena.vocabulary.RDFS;
import org.elasticsearch.action.get.GetResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Integration test class for {@link org.apache.jena.query.text.es.TextIndexES}
 */
public class TextIndexESIT extends BaseESTest {

    @Test
    public void testAddEntity() {

        addSpecialCharacterString("label", "this is a sample Label");

    }

    @Test
    public void testAddDateEntity() {

        addSpecialCharacterString("label", "2016-12-01T15:31:10-05:00");

    }

    @Test
    public void testPlusInSearchQuery() {
        //Test + character string
        addSpecialCharacterString("label", "We have +plus in the string");
        String queryString = "+plus";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testMinusInSearchQuery() {
        //Test - character string
        addSpecialCharacterString("label", "We have -minus in the string");
        String queryString = "-minus";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testEqualInSearchQuery() {
                //Test = character string
        addSpecialCharacterString("label", "We have =equal in the string");
        String queryString = "=equal";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }


    @Test
    public void testAmpersandInSearchQuery() {
                //Test && character string
        addSpecialCharacterString("label", "We have &&ampersand in the string");
        String queryString = "&&ampersand";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testPipeInSearchQuery() {
//        Test || character string
        addSpecialCharacterString("label", "We have ||pipe in the string");
        String queryString = "||pipe";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testGreaterInSearchQuery() {
//        //Test > character string
        addSpecialCharacterString("label", "We have >greater in the string");
        String queryString = ">greater";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testLessInSearchQuery() {
        // Test < character string
        addSpecialCharacterString("label", "We have <less in the string");
        String queryString = "<less";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }


    @Test
    public void testExclamationInSearchQuery() {
        //Test ! character string
        addSpecialCharacterString("label", "We have !notequal in the string");
        String queryString = "!notequal";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testOpenRoundInSearchQuery() {
        //Test ( character string
        addSpecialCharacterString("label", "We have (bracket in the string");
        String queryString = "(bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testClosedRoundInSearchQuery() {
        //Test ) character string
        addSpecialCharacterString("label", "We have )bracket in the string");
        String queryString = ")bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testOpenCurlyInSearchQuery() {
        //Test {bracket character string
        addSpecialCharacterString("label", "We have {bracket in the string");
        String queryString = "{bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testClosedCurlyInSearchQuery() {
        //Test }bracket character string
        addSpecialCharacterString("label", "We have }bracket in the string");
        String queryString = "}bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testOpenSquareInSearchQuery() {
        //Test [bracket character string
        addSpecialCharacterString("label", "We have [bracket in the string");
        String queryString = "[bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testClosedSquareInSearchQuery() {
        //Test ]bracket character string
        addSpecialCharacterString("label", "We have ]bracket in the string");
        String queryString = "]bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testCaretInSearchQuery() {
        //Test ^bracket character string
        addSpecialCharacterString("label", "We have ^bracket in the string");
        String queryString = "^bracket";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testTildaInSearchQuery() {
        //Test ~bracket character string
        addSpecialCharacterString("label", "We have ~tilda in the string");
        String queryString = "~tilda";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testQuestionInSearchQuery() {
        //Test ?question character string
        addSpecialCharacterString("label", "We have ?question in the string");
        String queryString = "?question";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);
    }

    @Test
    public void testDateQuery() {
        addSpecialCharacterString("label", "2016-12-01T15:31:10-05:00");
        String queryString = "2016-12-01T15:31:10-05:00";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);

    }

    @Test
    public void testDoubleQouteQuery() {
        addSpecialCharacterString("label", "This is a \"double\" quote");
        String queryString = "\"double\"";
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, null, 10, 1);
        querySpecialCharacterQuery(null, queryString, null, 10, 1);
        querySpecialCharacterQuery(RDFS.label.asNode(), queryString, "en", 10, 0);

    }


    private void addSpecialCharacterString(String key, String value) {

        Assert.assertNotNull(classToTest);
        Entity entityToAdd = entity("http://example/x5", key, value);
        GetResponse response = addEntity(entityToAdd);
        Assert.assertTrue(response.getSource().containsKey(key));
        Assert.assertEquals(value, ((List<?>)response.getSource().get(key)).get(0));

    }
    private void querySpecialCharacterQuery(Node label, String queryString, String lang, int limit, int expectedResults) {
        List<TextHit> result = classToTest.query(label, queryString, null, lang, limit);
        Assert.assertNotNull(result);
        Assert.assertEquals(expectedResults, result.size());
    }


    @Test
    public void testDeleteEntity() {
        testAddEntity();
        String labelKey = "label";
        String labelValue = "this is a sample Label";
        //Now Delete the entity
        classToTest.deleteEntity(entity("http://example/x5", labelKey, labelValue));

        //Try to find it
        GetResponse response = transportClient.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x5").get();
        //It Should Exist
        Assert.assertTrue(response.isExists());
        //But the field value should now be empty
        Assert.assertEquals("http://example/x5", response.getId());
        Assert.assertTrue(response.getSource().containsKey(labelKey));
        Assert.assertEquals(0, ((List<?>)response.getSource().get(labelKey)).size());
    }

    @Test
    public void testDeleteWhenNoneExists() {

        GetResponse response = transportClient.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();
        Assert.assertFalse(response.isExists());
        Assert.assertNotNull(classToTest);
        classToTest.deleteEntity(entity("http://example/x3", "label", "doesnt matter"));
        response = transportClient.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();
        Assert.assertFalse(response.isExists());

    }

    @Test
    public void testQuery() {
        testAddEntity();
        // This will search for value "this" only in the label field
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "this", null, null, 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        //This will search for value "this" across all the fields
        result =  classToTest.query(null, "this", null, null, 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        //This will search for value "this" in the label_en field, if it exists. In this case it doesnt so we should get zero results
        result =  classToTest.query(RDFS.label.asNode(), "this", null, "en", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

    }

    @Test
    public void testQueryWhenNoneExists() {
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "this",null, null, 1);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testGet() {
        testAddEntity();
        //Now Get the same entity
        Map<String, Node> response = classToTest.get("http://example/x5");
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
    }

    @Test
    public void testGetWhenNoneExists() {
        Map<String, Node> response = classToTest.get("http://example/x3");
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.size());
    }

    /**
     * This is an elaborate test that does the following:
     * 1. Create a Document with ID: "http://example/x3" , label: Germany and lang:en
     * 2. Makes sure the document is created successfully and is searchable based on the label
     * 3. Next add another label to the same Entity with ID: "http://example/x3", label:Deutschland and lang:de
     * 4. Makes sure that the document is searchable both with old (Germany) and new (Deutschland) values.
     * 5. Next, it deletes the value: Germany created in step 1.
     * 6. Makes sure that document is searchable with value: Deutschland but NOT with value: Germany
     * 7. Finally, delete the value: Deutschland
     * 8. The document should not be searchable with value: Deutschland
     * 9. The document should still exist
     */
    @Test
    public void testMultipleValuesinMultipleLanguages() throws InterruptedException{
        addEntity(entity("http://example/x3", "label", "Germany", "en"));
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "Germany",null, "en", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());
        //Next add another label to the same entity
        addEntity(entity("http://example/x3", "label", "Deutschland", "de"));
        //Query with old value
        result =  classToTest.query(RDFS.label.asNode(), "Germany", null, "en", 10);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Query with new value
        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, "de", 10);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Query without lang value
        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, null, 10);
        Assert.assertEquals(0, result.size());

        //Query without lang value as *
        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, "*", 10);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Now lets delete the Germany label
        classToTest.deleteEntity(entity("http://example/x3", "label", "Germany", "en"));

        TimeUnit.SECONDS.sleep(1);

        //We should NOT be able to find the entity using Germany label anymore
        result =  classToTest.query(RDFS.label.asNode(), "Germany", null, null, 10);
        Assert.assertEquals(0, result.size());

        result =  classToTest.query(RDFS.label.asNode(), "Germany", null, "en", 10);
        Assert.assertEquals(0, result.size());

        //But we should be able to find it with the Deutschland label value
        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, "de", 10);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Now lets delete the Deutschland label
        classToTest.deleteEntity(entity("http://example/x3", "label", "Deutschland", "de"));

        //if the Delete and query happens almost instantly, then there are chances to still get false positives
        //Thus sleeping for couple of seconds to give ES time to clean up.
        TimeUnit.SECONDS.sleep(1);
        //We should NOT be able to find the entity using Deutschland label anymore
        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, null, 10);
        Assert.assertEquals(0, result.size());

        result =  classToTest.query(RDFS.label.asNode(), "Deutschland", null, "de", 10);
        Assert.assertEquals(0, result.size());


    }

    /**
     * This test tries to save the same label values in different languages and makes sure that they are saved properly
     */
    @Test
    public void testSameLabelInDifferentLanguages() throws InterruptedException{
        addEntity(entity("http://example/x3", "label", "Berlin", "en"));
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "Berlin", null, "en", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Next add Berlin with 'de' language
        addEntity(entity("http://example/x3", "label", "Berlin", "de"));
        result =  classToTest.query(RDFS.label.asNode(), "Berlin", null, "de", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Now let's remove Berlin for language 'en'
        classToTest.deleteEntity(entity("http://example/x3", "label", "Berlin", "en"));
        //We should still be able to find the Document
        result =  classToTest.query(RDFS.label.asNode(), "Berlin", null, "de", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Now Lets remove Berlin for language 'de'
        classToTest.deleteEntity(entity("http://example/x3", "label", "Berlin", "de"));

        //if the Delete and query happens almost instantly, then there are chances to still get false positives
        //Thus sleeping for couple of seconds to give ES time to clean up
        TimeUnit.SECONDS.sleep(1);
        //Now we should NOT be able to find the document
        result =  classToTest.query(RDFS.label.asNode(), "Berlin", null, "de", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testLanguageTagSubCodes() {
        addEntity(entity("http://example/x3", "label", "color", "en-US"));
        addEntity(entity("http://example/x3", "label", "colour", "en-GB"));

        //Let's find it using color
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "color", null, "en-US", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        result =  classToTest.query(RDFS.label.asNode(), "color", null, "none", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        //Next Lets find it using colour
        result =  classToTest.query(RDFS.label.asNode(), "colour", null, "en-GB", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        result =  classToTest.query(RDFS.label.asNode(), "colour", null, "none", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());

        //Next lets find it after specifying the lang parameter
        result =  classToTest.query(RDFS.label.asNode(), "colour",null, "en*", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        result =  classToTest.query(RDFS.label.asNode(), "color",null, "en*", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //Now lets find it by specifying exact lang values
        result =  classToTest.query(RDFS.label.asNode(), "colour",null, "en-GB", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        result =  classToTest.query(RDFS.label.asNode(), "color",null, "en-US", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("http://example/x3", result.get(0).getNode().getURI());

        //We should NOT be able to find anything for wrong language
        result =  classToTest.query(RDFS.label.asNode(), "color",null, "en-GB", 10);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());


    }
    private Entity entity(String id, String fieldName, String fieldValue) {
        return entity(id, fieldName, fieldValue, null);
    }

    private Entity entity(String id, String fieldName, String fieldValue, String lang) {
        Entity entity = new Entity(id, null, lang, null);
        entity.put(fieldName, fieldValue);
        return entity;
    }

    private GetResponse addEntity(Entity entityToAdd) {
        classToTest.addEntity(entityToAdd);
        GetResponse response = transportClient.prepareGet(INDEX_NAME, DOC_TYPE, entityToAdd.getId()).get();

        Assert.assertNotNull(response);
        Assert.assertEquals(entityToAdd.getId(), response.getId());
        return response;

    }

}
