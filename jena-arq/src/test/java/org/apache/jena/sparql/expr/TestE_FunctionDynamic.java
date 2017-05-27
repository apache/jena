package org.apache.jena.sparql.expr;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestE_FunctionDynamic {
	
	@Test
	public void testConstructor_ExprList() {
		Expr e = new E_FunctionDynamic( new ExprList() );
		assertTrue( e instanceof E_FunctionDynamic );
	}

	@Test
	public void testConstructor_ExprAndExprList() {
		Expr e = new E_FunctionDynamic( Expr.NONE, new ExprList() );
		assertTrue( e instanceof E_FunctionDynamic );
	}
}
