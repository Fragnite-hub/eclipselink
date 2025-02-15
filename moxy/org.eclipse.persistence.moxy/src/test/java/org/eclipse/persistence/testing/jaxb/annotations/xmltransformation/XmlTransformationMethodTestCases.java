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
// Oracle = 2.2 - Initial implementation
package org.eclipse.persistence.testing.jaxb.annotations.xmltransformation;

import org.eclipse.persistence.testing.jaxb.JAXBTestCases;
import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class XmlTransformationMethodTestCases extends JAXBWithJSONTestCases{
public XmlTransformationMethodTestCases(String name) throws Exception {
      super(name);
      setClasses(new Class<?>[] {EmployeeTransformationMethod.class});
      setControlDocument("org/eclipse/persistence/testing/jaxb/externalizedmetadata/mappings/xmltransformation/employee.xml");
      setControlJSON("org/eclipse/persistence/testing/jaxb/externalizedmetadata/mappings/xmltransformation/employee.json");
   }

  @Override
  public Object getControlObject() {
      EmployeeTransformationMethod emp = new EmployeeTransformationMethod();
      emp.name = "John Smith";
      String[] hours = new String[2];
      hours[0] = "9:00AM";
      hours[1] = "5:00PM";
      emp.normalHours = hours;
      return emp;
  }
}
