/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.ResourceUtils;

public class ReportRenameResources {

    static Model _renameTestModel = null;
    private static Dataset _dataset;

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

            // Warm up all classes/methods we will use
            //String datasetName = "renameTestDS";
            //_dataset = TDBFactory.createDataset(datasetName);
            _dataset = TDBFactory.createDataset();

            _renameTestModel = _dataset
                    .getNamedModel("http://www.boeing.com/sem/renametest");

            try {
                String res1 = "http://www.boeing.com/sem/renametest#User-user7";
                String res2 = "http://www.boeing.com/sem/renametest#User-user8";

                Resource res1Resource = _renameTestModel.createResource(res1);
                Property typeProp = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                Resource thing = _renameTestModel.createResource("http://www.w3.org/2002/07/owl#Thing");
               
                Statement stmt = ResourceFactory.createStatement(res1Resource, typeProp, thing);

                _renameTestModel.add(stmt);
                ResourceUtils.renameResource(res1Resource, res2);

            } catch (Exception ex) {
                ex.printStackTrace();
            }



    }


}


/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */