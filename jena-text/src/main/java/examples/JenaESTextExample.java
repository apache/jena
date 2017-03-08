package examples;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

/**
 * Simple example class to test the {@link org.apache.jena.query.text.assembler.TextIndexESAssembler}
 * For this class to work properly, an elasticsearch V 5.2.1 node should be up and running, otherwise it will fail.
 * You can find the details of downloading and running an ElasticSearch version here: https://www.elastic.co/downloads/past-releases/elasticsearch-5-2-1
 * Unzip the file in your favourite directory and then execute the appropriate file under the bin directory.
 * It will take less than a minute.
 * In order to visualize what is written in ElasticSearch, you need to download and run Kibana: https://www.elastic.co/downloads/kibana
 * To run kibana, just go to the bin directory and execute the appropriate file.
 * We need to resort to this mechanism as ElasticSearch has stopped supporting embedded ElasticSearch.
 *
 * In addition we cant have it in the test package because ElasticSearch
 * detects the thread origin and stops us from instantiating a client.
 */
public class JenaESTextExample {

    public static void main(String[] args) {

        queryData(loadData(createAssembler()));
    }


    private static Dataset createAssembler() {
        String assemblerFile = "text-config-es.ttl";
        Dataset ds = DatasetFactory.assemble(assemblerFile,
                "http://localhost/jena_example/#text_dataset") ;
        return ds;
    }

    private static Dataset loadData(Dataset ds) {
        JenaTextExample1.loadData(ds, "data-es.ttl");
        return ds;
    }

    /**
     * The data being queried from ElasticSearch is proper but what is getting printed is wrong.
     * This is NOT a bug in ES code.
     * @param ds
     */
    private static void queryData(Dataset ds) {
        JenaTextExample1.queryData(ds);

    }
}
