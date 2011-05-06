package dev;

import java.io.IOException;

import org.apache.jena.larq.IndexLARQ;
import org.apache.jena.larq.assembler.AssemblerLARQ;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

public class Run_Assembler {

    public static void main(String[] args) throws IOException {
        Location location = new Location("/opt/datasets/tdb/jake-dev1");
        Dataset dataset = TDBFactory.createDataset(location);
        IndexLARQ indexLARQ = AssemblerLARQ.make(dataset, "/opt/datasets/lucene/jake-dev1");
        System.out.println(indexLARQ);
        indexLARQ.close();
    }
    
}
