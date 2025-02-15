/*
 * Copyright (c) 1998, 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.testing.oxm.mappings.simpletypes.typetranslator.rootelement;

// JDK imports

import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.testing.oxm.mappings.XMLMappingTestCases;

import java.nio.ByteBuffer;
// TopLink imports

public class TypeTranslatorTestCases extends XMLMappingTestCases {
    private final static String XML_RESOURCE = "org/eclipse/persistence/testing/oxm/mappings/simpletypes/typetranslator/RootElementTypeTranslatorTest.xml";

    private XMLMarshaller xmlMarshaller;

    public TypeTranslatorTestCases(String name) throws Exception {
        super(name);
        setControlDocument(XML_RESOURCE);
        setProject(new ByteHolderProject());
    }

    @Override
    protected Object getControlObject() {
        ByteHolder byteHolder = new ByteHolder();

        ByteBuffer myBuffer = ByteBuffer.allocate(4);

        myBuffer.putInt(15);
        byte bytes[] = myBuffer.array();

        Byte[] byteObjects = new Byte[bytes.length];
        for(int i=0; i<bytes.length; i++){
            byteObjects[i] = bytes[i];
        }

        byteHolder.setBytes(byteObjects);
    return byteHolder;
    }

}
