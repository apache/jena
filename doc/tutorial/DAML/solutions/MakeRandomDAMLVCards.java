/*
 * MakeRandomDAMLVCards.java
 *
 * Author: Alex Barnell, Jeremy Carroll
 *
 * (c) Copyright Hewlett-Packard Company 2002
 * All rights reserved.
 *
 * See end-of-file for license terms.
 */
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;
import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import java.io.*;
import java.util.*;

public class MakeRandomDAMLVCards {
    public MakeRandomDAMLVCards() throws Exception {
	int i;
	// Load in schema
	vcardModel = new DAMLModelImpl();
	vcardModel.read(new FileReader("vcard-daml.rdf"), vcardBaseURI);

	// Create instance-model
	instanceModel = new DAMLModelImpl();

	// Create useful classes
	vcardClass = (DAMLClass)vcardModel.getDAMLValue(vcardBaseURI + "#VCARD");
        npropertiesClass = (DAMLClass)vcardModel.getDAMLValue(vcardBaseURI + "#NPROPERTIES");

	// Create the properties we will be adding
	int numProps = propNames.length;
	props = new DAMLProperty[numProps];
	for (i = 0; i < numProps; i++) {
	    props[i] = (DAMLProperty)vcardModel.getDAMLValue(vcardBaseURI + "#" + propNames[i]);
	    System.err.println("Property " + i + ": " + props[i]);
	}
	
	// Get the classes of the objects of the properties	
	// Note,  the iterator returned by getRangeClasses() doesn't iterate purely
	// over DAMLClasses.
        // When the property is a DatatypeProperty they are Resources only, 
        // so we have to create the appropriate DAMLDatatype manually.
	Iterator it;
	Object o;
	DAMLClass objClass;
	objectClasses = new DAMLClass[numProps];
	for (i = 0; i < numProps; i++) {
	    it = props[i].getRangeClasses();
	    System.err.println("\nRange classes for " + props[i]);
	    while (it.hasNext()) {
		o = it.next();
		System.err.println("Object class: " + o);
		System.err.println("Class: " + o.getClass().getName());

		// Make it into a DAMLClass
		if (props[i] instanceof DAMLObjectProperty ) {
		    // Easy
		    objectClasses[i] = (DAMLClass)o;
		} else if (props[i] instanceof DAMLDatatypeProperty) {
		    Resource r = (Resource)o;
		    // Not so easy
		    objectClasses[i] = vcardModel.createDAMLClass(r.getURI());
		}
		System.err.println("Final class: " + objectClasses[i]);
	    }
	}
	
    }

    public void generate(int num) throws Exception {
	DAMLInstance vcard;
        DAMLInstance nproperties;
	
	for (int i = 0; i < num; i++) {
	    // Create a new VCard instance
	    vcard = instanceModel.createDAMLInstance(vcardClass, null);
            
            //Create a new NPROPERTIES instance
            nproperties = instanceModel.createDAMLInstance(npropertiesClass,null);
	   
	    // Make random person details
	    boolean gender = getRandomGender();
	    String name = getRandomName(gender);
	    String surname = getRandomSurname();
	    String fullname = name + " " + surname;
	    String email = name.toLowerCase() + "_" + surname.toLowerCase() + "@example.org";

	    setProperty(vcard, 0, nproperties);
	    setProperty(nproperties, 1, name);
	    setProperty(nproperties, 2, fullname);
	    setProperty(vcard, 3, fullname);
	    setProperty(vcard, 4, email);
	    

	}

	// output XML
	// Output the model
        RDFWriter abbrev = instanceModel.getWriter("RDF/XML-ABBREV");
        abbrev.setNsPrefix("vCard",vcardBaseURI+"#");
        abbrev.setNsPrefix("rxsd","http://www.w3.org/2000/10/XMLSchema#");
        abbrev.write(instanceModel,new OutputStreamWriter(System.out),"HTTP://foobar");
    }

    /**
     * Sets the property props[propNum] to the given value, making sure all the
     * datatype stuff is done OK
     * (This is getting very messy!)
     */
    private void setProperty(DAMLInstance vcard, int propNum, Object value) throws Exception {
	if (value instanceof String) {
            DAMLClass objClass = objectClasses[propNum];
            if ( propNum == 4 ) {
                objClass = getRandomSubclass(objClass);
            }
            DAMLInstance di = instanceModel.createDAMLInstance(objClass, null);
            di.addProperty(RDF.value, value);
	    vcard.accessProperty(props[propNum]).add(di);
        } else {
	    vcard.accessProperty(props[propNum]).add((DAMLCommon)value);
        }
    }

    public static void main(String[] args) throws Exception {
	MakeRandomDAMLVCards mrdv = new MakeRandomDAMLVCards();
	int num = 20;
	mrdv.generate(num);
    }
    
    public boolean getRandomGender() {
	return rnd.nextBoolean();
    }

    public String getRandomName(boolean gender) {
	if (gender)
	    return getRandomMaleName();
	else
	    return getRandomFemaleName();
    }


    public String getRandomFemaleName() {
	int i = Math.abs(rnd.nextInt() % femaleNames.length);
	return femaleNames[i];
    }
    public String getRandomMaleName() {
	int i = Math.abs(rnd.nextInt() % maleNames.length);
	return maleNames[i];
    }

    public String getRandomSurname() {
	int i = Math.abs(rnd.nextInt() % surnames.length);
	return surnames[i];
    }
    
    public DAMLClass getRandomSubclass(DAMLClass in) {
        Iterator it = in.getSubClasses();
        Vector v = new Vector();
        while (it.hasNext())
            v.add(it.next());
        int index = Math.abs(rnd.nextInt() % v.size());
        return (DAMLClass)v.elementAt(index);
        
    }

    private DAMLModel vcardModel, instanceModel;

    private  Random rnd = new Random();
    private  String vcardBaseURI = "http://www.w3.org/2001/vcard-rdf/3.0";

    private  String[] femaleNames = {"Amanda", "Beatrice", "Catherine", "Daffney", "Eunice", "Freya"};
    
    private  String[] maleNames = {"Alex", "Brian", "Chris", "David", "Edward", "Frank",
					 "Geoffrey", "Harold", "Ivor", "Jack", "Keith", "Larry",
					 "Michael", "Nicholas", "Orvil", "Paddy", "Quentin",
					 "Ronald", "Stuart", "Terrance", "Ug", "Vincent", "Wally",
					 "Xavier", "Zebedee"};
    
    private  String[] surnames = {"Andrews", "Baker", "Cartwright", "Dunster"};
    
    private DAMLClass vcardClass;
    private DAMLClass npropertiesClass;
    private String[] propNames = {"N", "Family", "Given", "FN", "EMAIL"};
    private DAMLProperty[] props;
    private DAMLClass[] objectClasses;
}


/* 
 * (c) Copyright Hewlett-Packard Company 2002
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
 *****************************************************************************/

