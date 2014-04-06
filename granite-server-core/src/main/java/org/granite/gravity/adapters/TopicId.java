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
package org.granite.gravity.adapters;

/**
 * Adapted from Greg Wilkins code (Jetty).
 * 
 * @author William DRAI
 */
public class TopicId {

    public final static String WILD = "*";
    public final static String WILDWILD = "**";

    private final static String[] ROOT = {};

    private final String name;
    private final String[] segments;
    private final int wild;

    public TopicId(String name) {
        this.name = name;
        if (name == null || name.length() == 0 || name.charAt(0) != '/')
            throw new IllegalArgumentException("Illegal topic name: " + name);

        if ("/".equals(name))
            segments = ROOT;
        else {
            if (name.charAt(name.length() - 1) == '/')
                throw new IllegalArgumentException("Illegal topic name (should not end with '/'): " + name);
            segments = name.substring(1).split("\\Q/\\E", -1);
        }

        if (segments.length > 0) {
            if (WILD.equals(segments[segments.length-1]))
                wild = 1;
            else if (WILDWILD.equals(segments[segments.length-1]))
                wild = 2;
            else
                wild = 0;
        }
        else
            wild = 0;
    }

    public boolean isWild() {
        return wild > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof TopicId) {
            TopicId other = (TopicId)obj;
            if (isWild()) {
                if (other.isWild())
                    return this.name.equals(other.name);
                return matches(other);
            }
            if (other.isWild())
                return other.matches(this);
            return name.equals(other.name);
        }
        else if (obj instanceof String) {
            if (isWild())
                return matches((String)obj);
            return name.equals(obj);
        }

        return false;
    }

    public boolean matches(TopicId name) {
        if (name.isWild())
            return equals(name);

        switch (wild) {
            case 0:
                return equals(name);
            case 1:
                if (name.segments.length != segments.length)
                    return false;
                for (int i = segments.length-1; i-- > 0; )
                    if (!segments[i].equals(name.segments[i]))
                        return false;
                return true;

            case 2:
                if (name.segments.length < segments.length)
                    return false;
                for (int i = segments.length-1; i-- > 0; )
                    if (!segments[i].equals(name.segments[i]))
                        return false;
                return true;
        }
        return false;
    }

    public boolean matches(String name) {
        if (wild == 0)
            return this.name.equals(name);

        // TODO more efficient?
        return matches(new TopicId(name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public int depth() {
        return segments.length;
    }

    public boolean isParentOf(TopicId id) {
        if (isWild() || depth() >= id.depth())
            return false;

        for (int i = segments.length-1; i-- >0; )
            if (!segments[i].equals(id.segments[i]))
                return false;

        return true;
    }

    public String getSegment(int i) {
        if (i > segments.length)
            return null;
        return segments[i];
    }
    
    public static String normalize(String topicId) {
    	if (topicId == null)
    		return "/";
    	if (topicId.indexOf('.') >= 0)
    		topicId = topicId.replace('.', '/');
    	return (topicId.startsWith("/") ? topicId : ("/" + topicId));
    }
}
