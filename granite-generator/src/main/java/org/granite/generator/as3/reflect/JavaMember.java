/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.generator.as3.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * @author Franck WOLFF
 */
public abstract class JavaMember<T extends Member> {

    public static final String PRIVATE = "private";
    public static final String PUBLIC = "public";
    public static final String PROTECTED = "protected";

    private final T member;
    private final String access;

    public JavaMember(T member) {
        if (member == null)
            throw new NullPointerException("Parameter member cannot be null");

        this.member = member;

        if (Modifier.isPublic(member.getModifiers()))
            this.access = PUBLIC;
        else if (Modifier.isPrivate(member.getModifiers()))
            this.access = PRIVATE;
        else
            this.access = PROTECTED;
    }

    public T getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public String getAccess() {
        return access;
    }
}
