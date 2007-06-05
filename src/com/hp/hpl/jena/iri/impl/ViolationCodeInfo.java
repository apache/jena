/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri.impl;

public class ViolationCodeInfo extends IRIExamples {

    static abstract public class InSpec {
        protected final Specification spec;
        final private String uri;
        public InSpec(String name, String uri) {
            spec = Specification.get(name);
            if (uri.equals(spec.getUri())) {
                this.uri = null;
            } else {
                this.uri = uri;
            }
        }
        public void add(ViolationCodeInfo info) {
            spec.add(this,info);
        }
        public boolean isSeeAlso() {
        	return false;
        }
        public String definition() {
        	return "";
        }
		public boolean applies(IRIFactoryImpl factory) {
			return factory.specs.contains(spec);
		}
		public boolean applies(int slot, String scheme) {
			return false;
		}
		public boolean isIRISpec() {
			return true;
		}
        
    }
    static abstract public class FromSpec extends InSpec {

        final private int component;
        final private String definition;
        final private String definitionHtml;
		public boolean applies(int slot, String scheme) {
			if (component != -1 && component != slot)
			   return false;
			return spec.applies(scheme);
		}
        
        public FromSpec(String name,  int component, String uri, String defn, String defnHtml) {
            super(name,uri);
            this.component = component;
            definition = defn;
            // TODO: definitions of schemes etc.
//            if (definition == null) 
//            	System.err.println(name);
            definitionHtml = defnHtml;
        }

        public String definition() {
        	return "[[ " + definition + " ]]";
        }
        
    }
    static public class FromSpec_other extends FromSpec {
        public FromSpec_other(String name, int component, String uri, String defn, String defnHtml) {
            super(name,component,uri,defn,defnHtml);
        }
    }
    static public class FromSpec_scheme extends FromSpec {
        public FromSpec_scheme(String name, int component, String uri, String defn, String defnHtml) {
            super(name,component,uri,defn,defnHtml);
          }
        public FromSpec_scheme(String name, int component, String uri) {
            this(name,component,uri,null,null);
        }
		public boolean isIRISpec() {
			return false;
		}
    }
    static public class FromSpec_iri extends FromSpec {
        public FromSpec_iri(String name, int component, String uri, String defn, String defnHtml) {
            super(name,component,uri,defn,defnHtml);
        }
    }
    static public class FromAlso extends InSpec {

        public FromAlso(String spec, String uri) {
            super(spec,uri);
        }
        public boolean isSeeAlso() {
        	return true;
        }
		public boolean applies(int slot, String scheme) {
			return false;
		}
        
    }
    
    final private int force;
    final private String name;
    final private int code;
    final private String description;
    final private String descriptionHtml;
    final private boolean unimplemented;
    final private InSpec specifications[];
    
    public ViolationCodeInfo(int code, String name, 
            String desc, String descHtml, 
            int force, 
            InSpec specs[], 
            String[] badExamples, 
            String[] goodExamples,
            boolean unimplemented) {
        super(badExamples,goodExamples);
        if (force==0) force = Force.must;
        this.force = force;
        this.name = name;
        this.code = code;
        this.description = desc;
        this.descriptionHtml = descHtml;
        this.unimplemented = unimplemented;
//        this.badExamples = badExamples;
//        this.goodExamples = goodExamples;
        specifications = specs;
        init();   
    }

    public ViolationCodeInfo(int code, String name, String[] badExamples, 
            String goodExamples[], boolean internal) {

        super(badExamples,goodExamples);
         this.force = 0;
        this.name = name;
        this.code = code;
        this.description = name;
        this.descriptionHtml = "<p>name</p>";
        this.unimplemented = !internal;
//        this.badExamples = badExamples;
//        this.goodExamples = goodExamples;
        specifications = new InSpec[0];
        init();
    }

    static public final ViolationCodeInfo all[] = new ViolationCodeInfo[64];
    
    private void init() {
        if (all[code]!=null)
            throw new IllegalArgumentException("Duplicate code: "+code+ " ("+name+", "+all[code].name+")");
        all[code] = this;
        for (int i=0;i<specifications.length;i++) {
            specifications[i].add(this);
        }
    }

    public int getCode() {
        return code;
    }

    public int getForce() {
        return force;
    }

    public String getCodeName() {
        return name;
    }

    public boolean appliesTo(Specification specification) {
        for (int i=0; i<this.specifications.length; i++)
            if (specifications[i].spec == specification)
                return true;
        return false;
    }

    public boolean isImplemented() {
        return !unimplemented;
    }

	public String description(int slot, IRIFactoryImpl impl) {
		return description;
	}

	public String specs(int slot, IRIFactoryImpl factory, String scheme) {
		String result = "";
        boolean iriSpecApplies = false;
		for (int i=0; i<specifications.length;i++) {
			InSpec inSpec = specifications[i];
			if (inSpec.isIRISpec() &&
					inSpec.applies(factory)) 
				iriSpecApplies = true;
				
			
		}
		for (int i=0; i<specifications.length;i++) {
			InSpec inSpec = specifications[i];
			if (inSpec.isSeeAlso())
				continue;
			if (inSpec.isIRISpec() && !iriSpecApplies)
				continue;
			if (!inSpec.applies(slot, scheme) )
				continue;
			Specification spec = inSpec.spec;
			String uri = inSpec.uri;
			if (uri == null)
				uri = spec.getUri();
			result = result + spec.name()
			   + " <" + uri + "> " + inSpec.definition();
			
			
		}
		return result;
	}

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 
