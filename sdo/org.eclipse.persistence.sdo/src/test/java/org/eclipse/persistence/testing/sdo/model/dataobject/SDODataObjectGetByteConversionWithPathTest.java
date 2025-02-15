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
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.testing.sdo.model.dataobject;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.eclipse.persistence.sdo.SDOConstants;
import org.eclipse.persistence.sdo.SDOProperty;

public class SDODataObjectGetByteConversionWithPathTest extends SDODataObjectConversionWithPathTestCases {
    public SDODataObjectGetByteConversionWithPathTest(String name) {
        super(name);
    }

     public static void main(String[] args) {
        String[] arguments = { "-c", "org.eclipse.persistence.testing.sdo.model.dataobject.SDODataObjectGetByteConversionWithPathTest" };
        TestRunner.main(arguments);
    }

    //1. purpose: getByte with boolean property
    public void testGetByteFromBoolean() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_BOOLEAN);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        dataObject_c.set(property_c, true);
        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //2. purpose: getByte with Defined Byte Property
    public void testGetByteConversionFromDefinedByteProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_BYTE);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        byte by = 12;

        dataObject_a.setByte(propertyPath_a_b_c, by);// add it to instance list

        assertEquals(by, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //3. purpose: getByte with Undefined Byte Property
    public void testGetByteConversionFromUnDefinedByteProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_BYTE);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //4. purpose: getByte with character property
    public void testGetByteFromCharacter() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_CHARACTER);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        char theValue = 'e';
        dataObject_c.set(property_c, theValue);
        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //5. purpose: getByte with Defined Double Property
    public void testGetByteConversionFromDefinedDoubleProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_DOUBLE);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        double db = 12;
        dataObject_a.setDouble(propertyPath_a_b_c, db);// add it to instance list

        assertEquals((byte)db, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //6. purpose: getByte with Undefined Double Property
    public void testGetByteConversionFromUnDefinedDoubleProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_DOUBLE);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //7. purpose: getByte with Defined float Property
    public void testGetByteConversionFromDefinedFloatProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_FLOAT);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        float fl = 12;
        dataObject_a.setFloat(propertyPath_a_b_c, fl);// add it to instance list

        assertEquals((byte)fl, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //8. purpose: getByte with Undefined float Property
    public void testGetByteConversionFromUnDefinedFloatProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_FLOAT);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //9. purpose: getByte with Defined int Property
    public void testGetByteConversionFromDefinedIntProperty() {
        // dataObject's type add int property
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_FLOAT);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        int in = 12;
        dataObject_a.setInt(propertyPath_a_b_c, in);// add it to instance list

        assertEquals((byte)in, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //10. purpose: getByte with Undefined int Property
    public void testGetByteConversionFromUnDefinedIntProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_FLOAT);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //11. purpose: getByte with Defined long Property
    public void testGetByteConversionFromDefinedLongProperty() {
        // dataObject's type add short property
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_LONG);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        long lg = 12;
        dataObject_a.setLong(propertyPath_a_b_c, lg);// add it to instance list

        assertEquals((byte)lg, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //12. purpose: getByte with Undefined long Property
    public void testGetByteConversionFromUnDefinedLongProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_LONG);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //13. purpose: getByte with Defined short Property
    public void testGetByteConversionFromDefinedShortProperty() {
        // dataObject's type add short property
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_LONG);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        short shr = 12;
        dataObject_a.setShort(propertyPath_a_b_c, shr);// add it to instance list

        assertEquals((byte)shr, dataObject_a.getByte(propertyPath_a_b_c));
    }

    //14. purpose: getByte with Undefined short Property
    public void testGetDoubleConversionFromUnDefinedShortProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_LONG);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //15. purpose: getByte with Defined String Property
    public void testGetByteConversionFromDefinedStringProperty() {
        // dataObject's type add int property
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_STRING);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        String str = "12";
        Byte s_d = Byte.valueOf(str);
        dataObject_a.setString(propertyPath_a_b_c, str);// add it to instance list

        assertEquals(s_d.byteValue(), dataObject_a.getByte(property));
    }

    //16. purpose: getDouble with Undefined string Property
    public void testGetByteConversionFromUnDefinedStringProperty() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_STRING);
        dataObject_c._setType(type_c);

        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //17. purpose: getByte with bytes property
    public void testGetByteFromBytes() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_BYTES);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        byte[] theValue = new byte[]{10,100};
        dataObject_c.set(property_c, theValue);
        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //18. purpose: getByte with decimal property
    public void testGetByteFromDecimal() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_DECIMAL);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        BigDecimal theValue = new BigDecimal(10);
        dataObject_c.set(property_c, theValue);
        try {
            byte value = dataObject_a.getByte(propertyPath_a_b_c);
            byte controlValue = theValue.byteValue();
            assertEquals(controlValue, value);
            //TODO: conversion not supported by sdo spec but is supported by TopLink
        } catch (ClassCastException e) {
        }
    }

    //19. purpose: getByte with decimal property
    public void testGetByteFromInteger() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_INTEGER);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        BigInteger theValue = new BigInteger("10");
        dataObject_c.set(property_c, theValue);
        try {
            byte value = dataObject_a.getByte(propertyPath_a_b_c);
            byte controlValue = theValue.byteValue();
            assertEquals(controlValue, value);
            //TODO: conversion not supported by sdo spec but is supported by TopLink
        } catch (ClassCastException e) {
        }
    }

    //20. purpose: getByte with date property
    public void testGetByteFromDate() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_INTEGER);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);

        dataObject_c.set(property_c, Calendar.getInstance().getTime());
        try {
            dataObject_a.getByte(propertyPath_a_b_c);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

    //purpose: getByte with nul value
    public void testGetByteWithNullArgument() {
        try {
            String p = null;
            dataObject_a.getByte(p);
        } catch (Exception e) {
            fail("No Exception expected, but caught " + e.getClass());
        }
    }

          //22. purpose: getDouble from null
    public void testGetByteFromNull() {
        property_c = new SDOProperty(aHelperContext);
        property_c.setName(PROPERTY_NAME_C);
        property_c.setType(SDOConstants.SDO_BYTE);
        type_c.addDeclaredProperty(property_c);
        dataObject_c._setType(type_c);
        dataObject_c.set(property_c, null);

        byte value = dataObject_a.getByte(propertyPath_a_b_c);
        byte byteValue = 0;
        assertEquals(byteValue, value);
        DataObject doNext = dataObject_a.getDataObject("PName-a");
        doNext = doNext.getDataObject("PName-b");
        Property prop  = doNext.getInstanceProperty("PName-c");
        byte value2 =  doNext.getByte(prop);
        assertEquals(byteValue, value2);
    }
}
