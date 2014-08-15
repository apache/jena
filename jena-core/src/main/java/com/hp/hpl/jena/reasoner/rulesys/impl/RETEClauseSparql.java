/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.SparqlQuery;
import java.util.Map;

/**
 *
 * @author mba
 */
public class RETEClauseSparql implements RETENode{

    SparqlQuery sparqlQuery;
    
    public RETEClauseSparql(SparqlQuery psparqlQuery) {
        sparqlQuery = psparqlQuery;
    }
    
    public SparqlQuery getSparqlQuery() {
        return sparqlQuery;
    }
    
    @Override
    public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
        return new RETEClauseSparql(this.getSparqlQuery());
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
