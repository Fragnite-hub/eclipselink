/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved.
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
//     Martin Vojtek - 2.6 - Initial contribution
package org.eclipse.persistence.oxm.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <b>Purpose:</b> Provide a way to allow properties with XmlID annotation have different type than java.lang.String.
 * By default, all properties annotated with XmlID annotation are restricted to have java.lang.String type.
 * <p>If XmlIDExtension annotation is used, there is no restriction of used type with XmlID annotation.</p>
 * <p>Using XmlIDExtension provides way how to achieve backward compatibility with EclipseLink 2.5.x and before.</p>
 * <p>When using xml bindings with XmlID property, it has same behavior as when XmlIDExtension is used.
 * It means that there is no need to specify XmlIDExtension in xml, because the behavior is provided by default.
 * </p>
 * @see jakarta.xml.bind.annotation.XmlID
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface XmlIDExtension {
}
