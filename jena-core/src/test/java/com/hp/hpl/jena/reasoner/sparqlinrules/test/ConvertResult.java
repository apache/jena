/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mba
 */
public class ConvertResult {       
        
        public ConvertResult() {};
        
        public ResultList getresult(ResultSet rs) {
            rs.getResultVars();

            List<String> resultVars = rs.getResultVars();

            ArrayList<ResultRow> rows = new ArrayList<>();

            while(rs.hasNext()) {

                QuerySolution qs = rs.nextSolution();

                ResultRow row = new ResultRow();

                for(String field : resultVars) {     
                     row.addField(new ResultField(field, qs.get(field).toString()));
                }

                rows.add(row);

            }    
            return new ResultList(rows);
        }        
    }