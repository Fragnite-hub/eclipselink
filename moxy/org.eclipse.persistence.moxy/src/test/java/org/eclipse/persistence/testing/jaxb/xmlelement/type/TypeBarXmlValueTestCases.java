/*
 * Copyright (c) 1998, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Denise Smith - May 2013
package org.eclipse.persistence.testing.jaxb.xmlelement.type;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class TypeBarXmlValueTestCases extends JAXBWithJSONTestCases{

    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlelement/type/bartype.xml";
    private final static String JSON_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlelement/type/bartype.json";


    public TypeBarXmlValueTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);
        setControlJSON(JSON_RESOURCE);
        Class<?>[] classes = new Class<?>[]{Foo.class, Bar.class, BarXmlValue.class};
        setClasses(classes);
    }

     @Override
     protected Object getControlObject() {
         Foo f = new Foo();
         Bar b = new Bar();
         b.thing = "theThing";
         f.field = b;
         return f;
     }

}
