package org.apache.jena.query.text;



import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 *
 * Integration test for {@link TextIndexES} class
 * ES Integration test depends on security policies that may sometime not be loaded properly.
 * If you find any issues regarding security set the following VM argument to resolve the issue:
 * -Dtests.security.manager=false
 *
 */
@ESIntegTestCase.ClusterScope()
public class TestTextIndexES extends ESIntegTestCase {

    static final String DOC_TYPE = "text";

    static final String INDEX_NAME = "test";


    private static TextIndexES classToTest;

    private static Client client;

    /**
     * Test {@link TextIndexES#addEntity(Entity)}
     */
    @Test
    public void testAddEntity() {
        init();
        String labelKey = "label";
        String labelValue = "this is a sample Label";
        classToTest.addEntity(entity("http://example/x3", labelKey, labelValue));
        GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();

        Assert.assertNotNull(response);
        Assert.assertEquals("http://example/x3", response.getId());
        Assert.assertTrue(response.getSource().containsKey(labelKey));
        Assert.assertEquals(labelValue, response.getSource().get(labelKey));
    }

    @Test
    public void testDeleteEntity() {
        init();
        //First add an entity
        testAddEntity();
        //Now Delete the entity
        classToTest.deleteEntity(entity("http://example/x3", "doesnt matter", "doesnt matter"));

        //Try to find it
        GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();
        Assert.assertFalse(response.isExists());
    }

    @Test
    public void testDeleteWhenNoneExists() {
        init();
        GetResponse response = client.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();
        Assert.assertFalse(response.isExists());
        classToTest.deleteEntity(entity("http://example/x3", "doesnt matter", "doesnt matter"));
        response = client.prepareGet(INDEX_NAME, DOC_TYPE, "http://example/x3").get();
        Assert.assertFalse(response.isExists());

    }

    @Test
    public void testQuery() {
        init();
        testAddEntity();
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "this", 1);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("this is a sample Label", result.get(0).getLiteral().getLiteralValue().toString());
    }

    @Test
    public void testQueryWhenDataDoesNotExist() {
        init();
        List<TextHit> result =  classToTest.query(RDFS.label.asNode(), "this", 1);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testGetEntity() {
        init();
        //First add an entity
        testAddEntity();
        //Now Get the same entity
        Map<String, Node> response = classToTest.get("http://example/x3");
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.size());
    }

    @Test
    public void testGetWhenDataDoesNotExist() {
        init();
        Map<String, Node> response = classToTest.get("http://example/x3");
        Assert.assertNotNull(response);
        Assert.assertEquals(0, response.size());

    }

    private TextIndexConfig config() {
        EntityDefinition ed = new EntityDefinition(DOC_TYPE, "label", RDFS.label);
        ed.set("comment", RDFS.comment.asNode());
        TextIndexConfig config = new TextIndexConfig(ed);
        return config;
    }

    private Entity entity(String id, String fieldName, String fieldValue) {
        Entity entity = new Entity(id, null);
        entity.put(fieldName, fieldValue);
        return entity;
    }

    private void init() {
        if (client == null) {
            client = client();
        }
        try {
            if(!client.admin().indices().exists(new IndicesExistsRequest(INDEX_NAME)).get().isExists()) {
                client.admin().indices().prepareCreate(INDEX_NAME).get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        classToTest = new TextIndexES(config(), client, INDEX_NAME);
    }

}
