/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import java.util.ArrayList;

/**
 *
 * @author mba
 */
public class ResultList extends Result{
        ArrayList<ResultRow> result = new ArrayList<>();
        
        public ResultList(ArrayList<ResultRow> presult) {
            result = presult;
        }
        
        public void print() {
            for(ResultRow rs : result) {
                rs.print();
                System.out.println("");
            }
        }
        
        public boolean sameResult(Result vrl) {
            ArrayList<ResultRow> r1 = new ArrayList<>();
            r1.addAll(result);
            ArrayList<ResultRow> r2 = new ArrayList<>();
            r2.addAll(((ResultList) vrl).result);
            return CompareResults.sameResult(r1, r2);
        }
    }
