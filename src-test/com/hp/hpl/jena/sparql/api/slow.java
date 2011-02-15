/*
 * (c) Copyright Apache Software Foundation - Apache Software License 2.0
 */

package com.hp.hpl.jena.sparql.api;

import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase0;

public class slow extends FunctionBase0 {

    static NodeValue value_true = NodeValue.makeBoolean(true) ;
	
	@Override
	public NodeValue exec()
	{
	    Lib.sleep(100) ;
		return value_true;
	}

}
