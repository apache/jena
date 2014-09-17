/*
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


package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.List;


public class ResultsOp {
   public static boolean sameResult(ResultSet r1, ResultSet r2) {
        ArrayList<Binding> list_res1 = new ArrayList<Binding>();
        ArrayList<Binding> list_res2 = new ArrayList<Binding>();
        
        while(r1.hasNext()){
            list_res1.add(r1.nextBinding());
        };
        
        while(r2.hasNext()){
            list_res2.add(r2.nextBinding());
        };

         
        return (list_res1.containsAll(list_res2) && list_res2.containsAll(list_res1));
    }
    
    public static boolean sameResult (ArrayList<? extends Result> vl1, ArrayList<? extends Result> vl2) {
            boolean retV = (vl1.size() == vl2.size());

            if(retV) {

                boolean cont = true;
                while(cont && vl1.size()>0) {
                    int pos = getPosAL(vl1.get(0), vl2);
                    if(pos>=0) {
                        vl2.remove(pos);
                        vl1.remove(0);
                    }
                    else {
                        cont = false;
                    }
                }
               retV = retV && vl2.isEmpty();
            }

            return retV;
        }
        
    private static int getPosAL(Result o, ArrayList<? extends Result> alO) {
            int pos = -1;
            int i=0;
            boolean found = false; 

            while(i<alO.size() && !found) {
                if(alO.get(i).sameResult(o)) {
                    pos = i;
                    found = true;
                }
                else {
                    i++;
                }
            }

            return pos; 
        }

    public static ResultList convertResult(ResultSet rs) {
        rs.getResultVars();

        List<String> resultVars = rs.getResultVars();

        ArrayList<ResultRow> rows = new ArrayList<>();

        while(rs.hasNext()) {

            QuerySolution qs = rs.nextSolution();

            ResultRow row = new ResultRow();

            for(String field : resultVars) {     
                 row.addResult(field, qs.get(field).asNode());
            }

            rows.add(row);

        }    
        return new ResultList(rows);
    }    
    
}
