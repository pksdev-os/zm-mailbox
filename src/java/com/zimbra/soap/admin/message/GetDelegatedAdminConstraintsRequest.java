/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2011 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.3 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.soap.admin.message;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.TargetType;
import com.zimbra.soap.type.NamedElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name=AdminConstants.E_GET_DELEGATED_ADMIN_CONSTRAINTS_REQUEST)
public class GetDelegatedAdminConstraintsRequest {

    @XmlAttribute(name=AdminConstants.A_TYPE, required=true)
    private final TargetType type;

    @XmlAttribute(name=AdminConstants.A_ID, required=false)
    private final String id;

    @XmlAttribute(name=AdminConstants.A_NAME, required=false)
    private final String name;

    @XmlElement(name=AdminConstants.E_A, required=false)
    private List<NamedElement> attrs = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetDelegatedAdminConstraintsRequest() {
        this((TargetType) null, (String) null, (String) null);
    }

    public GetDelegatedAdminConstraintsRequest(
                TargetType type, String id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public void setAttrs(Iterable <NamedElement> attrs) {
        this.attrs.clear();
        if (attrs != null) {
            Iterables.addAll(this.attrs,attrs);
        }
    }

    public GetDelegatedAdminConstraintsRequest addAttr(NamedElement attr) {
        this.attrs.add(attr);
        return this;
    }

    public TargetType getType() { return type; }
    public String getId() { return id; }
    public String getName() { return name; }
    public List<NamedElement> getAttrs() {
        return Collections.unmodifiableList(attrs);
    }
}
