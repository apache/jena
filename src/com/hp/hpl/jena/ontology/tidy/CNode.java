package com.hp.hpl.jena.ontology.tidy;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/**
 * @author jjc
 *
 */
abstract class CNode extends EnhNode implements CNodeI {
	final static public Implementation factory = new Implementation() {
		public EnhNode wrap(Node n, EnhGraph eg) {
            // work out what sort of node this is.
            if ( n.isLiteral())
               return new CLit(n,eg);
            if ( n.isURI() ) {
               int type = Grammar.getBuiltinID(n.getURI());
               if ( type != -1 )
                 return new CBuiltin(n,eg,type);
            }
			return new CGeneral(n, eg);
		}
	};
	CNode(Node n, EnhGraph eg) {
		super(n, eg);
	}

}
