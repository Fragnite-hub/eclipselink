/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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
//     Denise Smith - 2.4
package org.eclipse.persistence.testing.jaxb.json.xmlvalue;

import java.util.Map;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

public class XMLValuePropDifferentTestCases extends XMLValuePropTestCases {

    private final static String JSON_RESOURCE = "org/eclipse/persistence/testing/jaxb/json/xmlvalue/personUnmarshal.json";
    private final static String JSON_WRITE_RESOURCE = "org/eclipse/persistence/testing/jaxb/json/xmlvalue/personMarshal.json";

    public XMLValuePropDifferentTestCases(String name) throws Exception {
        super(name);
        setClasses(new Class<?>[]{Person.class});
        setControlJSON(JSON_RESOURCE);
        setWriteControlJSON(JSON_WRITE_RESOURCE);
    }

    @Override
    public void setUp() throws Exception{
        super.setUp();
        jaxbMarshaller.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, "marshalWrapper");
        jaxbUnmarshaller.setProperty(UnmarshallerProperties.JSON_VALUE_WRAPPER, "unmarshalWrapper");
    }

    //public Map getProperties(){
    //    return null;
    //}

}
