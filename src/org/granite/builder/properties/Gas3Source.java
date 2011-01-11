/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.builder.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.granite.builder.util.StringUtil;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Franck WOLFF
 */
@XStreamAlias(value="source")
public class Gas3Source implements Validable, Comparable<Gas3Source> {

	@XStreamAsAttribute
	private String path;
	
	@XStreamAsAttribute
	private String includes;
	
	@XStreamAsAttribute
	private String excludes;
	
	@XStreamAsAttribute
	private String output;

	private transient List<Pattern> includePatterns;
	private transient List<Pattern> excludePatterns;
	
	public Gas3Source(String path, String includes, String excludes, String output) {
		this.path = path;
		setIncludes(includes);
		setExcludes(excludes);
		this.output = output;
	}
	
	public boolean match(String path, String file) {
		if (!this.path.equals(path))
			return false;
		
		compilePatterns();
		
		for (Pattern pattern : excludePatterns) {
			if (pattern.matcher(file).matches())
				return false;
		}
		
		for (Pattern pattern : includePatterns) {
			if (pattern.matcher(file).matches())
				return true;
		}

		return includePatterns.isEmpty();
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getIncludes() {
		return includes;
	}
	public void setIncludes(String includes) {
		this.includes = includes;
		this.includePatterns = null;
	}

	public String getExcludes() {
		return excludes;
	}
	public void setExcludes(String excludes) {
		this.excludes = excludes;
		this.excludePatterns = null;
	}

	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutputDir() {
		if (output == null)
			return "";
		String[] dirs = StringUtil.split(output, ';');
		if (dirs.length > 0)
			return dirs[0];
		return "";
	}

	public String getBaseOutputDir() {
		return getBaseOutputDir(false);
	}

	public String getBaseOutputDir(boolean fallback) {
		if (output == null)
			return "";
		String[] dirs = StringUtil.split(output, ';');
		if (dirs.length > 1 && dirs[1].length() > 0)
			return dirs[1];
		return (fallback && dirs.length > 0 ? dirs[0] : "");
	}
	
	public void validate(ValidationResults results) {
		if (path == null || output == null)
			results.getErrors().add("source: path and output cannot be null");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Gas3Source))
			return false;
		Gas3Source g3s = (Gas3Source)obj;
		return (path == null ? g3s.path == null : path.equals(g3s.path));
	}

	@Override
	public int hashCode() {
		return path != null ? path.hashCode() : 0;
	}

	public int compareTo(Gas3Source o) {
		if (path == null)
			return -1;
		if (o.path == null)
			return 1;
		return path.compareTo(o.path);
	}
	
	private void compilePatterns() {
		if (includePatterns == null) {
			if (includePatterns == null)
				includePatterns = new ArrayList<Pattern>();
			else
				includePatterns.clear();
			if (includes != null) {
				for (String include : StringUtil.split(includes, ';')) {
					if (include.length() > 0)
						includePatterns.add(Pattern.compile(StringUtil.regexifyPathPattern(include)));
				}
			}
		}
	
		if (excludePatterns == null) {
			if (excludePatterns == null)
				excludePatterns = new ArrayList<Pattern>();
			else
				excludePatterns.clear();
			if (excludes != null) {
				for (String exclude : StringUtil.split(excludes, ';')) {
					if (exclude.length() > 0)
						excludePatterns.add(Pattern.compile(StringUtil.regexifyPathPattern(exclude)));
				}
			}
		}
	}
}
