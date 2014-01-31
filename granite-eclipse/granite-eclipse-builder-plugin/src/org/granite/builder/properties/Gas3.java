/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.builder.properties;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.granite.generator.TemplateUri;
import org.granite.generator.as3.reflect.JavaType.Kind;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author Franck WOLFF
 */
@XStreamAlias(value="gas3")
public class Gas3 implements Validable {

	@XStreamOmitField
	private PropertyChangeSupport _pcs = null;
	
	@XStreamAsAttribute
	private String uid;

	@XStreamAsAttribute
	private String as3TypeFactory;

	@XStreamAsAttribute
	private String entityFactory;

	@XStreamAsAttribute
	private String remoteDestinationFactory;

	@XStreamAsAttribute
	private boolean debugEnabled;

	@XStreamAsAttribute
	private boolean flexConfig;

	@XStreamAsAttribute
	private boolean externalizeLong;

	@XStreamAsAttribute
	private boolean externalizeBigInteger;

	@XStreamAsAttribute
	private boolean externalizeBigDecimal;
	
	@XStreamImplicit(itemFieldName="source")
	private TreeSet<Gas3Source> sources;
	
	@XStreamImplicit(itemFieldName="project")
	private TreeSet<Gas3Project> projects;
	
	@XStreamImplicit(itemFieldName="classpath")
	private List<Gas3Classpath> classpaths;
	
	@XStreamImplicit(itemFieldName="template")
	private Set<Gas3Template> templates;
	
	@XStreamImplicit(itemFieldName="transformer")
	private List<Gas3Transformer> transformers;
	
	@XStreamImplicit(itemFieldName="translator")
	private Set<Gas3Translator> translators;

	public Gas3() {
		_pcs = new PropertyChangeSupport(this);
	}

	public Gas3(String uid, String as3TypeFactory, String entityFactory, String remoteDestinationFactory) {
		this.uid = uid;
		this.as3TypeFactory = as3TypeFactory;
		this.entityFactory = entityFactory;
		this.remoteDestinationFactory = remoteDestinationFactory;
		
		_pcs = new PropertyChangeSupport(this);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getAs3TypeFactory() {
		return as3TypeFactory;
	}

	public void setAs3TypeFactory(String as3TypeFactory) {
		String old = this.as3TypeFactory;
		this.as3TypeFactory = as3TypeFactory;
		if (_pcs != null)
			_pcs.firePropertyChange("as3TypeFactory", old, as3TypeFactory);
	}

	public String getEntityFactory() {
		return entityFactory;
	}

	public void setEntityFactory(String entityFactory) {
		this.entityFactory = entityFactory;
	}

	public String getRemoteDestinationFactory() {
		return remoteDestinationFactory;
	}

	public void setRemoteDestinationFactory(String remoteDestinationFactory) {
		this.remoteDestinationFactory = remoteDestinationFactory;
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void setDebugEnabled(boolean debugEnabled) {
		this.debugEnabled = debugEnabled;
	}

	public boolean isFlexConfig() {
		return flexConfig;
	}

	public void setFlexConfig(boolean flexConfig) {
		this.flexConfig = flexConfig;
	}

	public boolean isExternalizeLong() {
		return externalizeLong;
	}

	public void setExternalizeLong(boolean externalizeLong) {
		this.externalizeLong = externalizeLong;
	}

	public boolean isExternalizeBigInteger() {
		return externalizeBigInteger;
	}

	public void setExternalizeBigInteger(boolean externalizeBigInteger) {
		this.externalizeBigInteger = externalizeBigInteger;
	}

	public boolean isExternalizeBigDecimal() {
		return externalizeBigDecimal;
	}

	public void setExternalizeBigDecimal(boolean externalizeBigDecimal) {
		this.externalizeBigDecimal = externalizeBigDecimal;
	}

	public TreeSet<Gas3Source> getSources() {
		if (sources == null)
			sources = new TreeSet<Gas3Source>();
		return sources;
	}

	public void setSources(TreeSet<Gas3Source> sources) {
		this.sources = sources;
	}
	
	public Gas3Source getMatchingSource(String path, String file) {
		if (sources != null) {
			for (Gas3Source source : sources) {
				if (source.match(path, file))
					return source;
			}
		}
		return null;
	}

	public TreeSet<Gas3Project> getProjects() {
		if (projects == null)
			projects = new TreeSet<Gas3Project>();
		return projects;
	}

	public void setProjects(TreeSet<Gas3Project> projects) {
		this.projects = projects;
	}

	public List<Gas3Classpath> getClasspaths() {
		if (classpaths == null)
			classpaths = new ArrayList<Gas3Classpath>();
		return classpaths;
	}

	public void setClasspaths(List<Gas3Classpath> classpaths) {
		this.classpaths = classpaths;
	}

	public Set<Gas3Template> getTemplates() {
		if (templates == null)
			templates = new HashSet<Gas3Template>();
		return templates;
	}

	public void setTemplates(Set<Gas3Template> templates) {
		this.templates = templates;
	}

	public Gas3Template getTemplate(Kind kind) {
		for (Gas3Template template : getTemplates()) {
			if (kind.equals(template.getKind()))
				return template;
		}
		return null;
	}

	public TemplateUri[] getMatchingTemplateUris(Kind kind) {
		if (templates != null) {
			for (Gas3Template template : templates) {
				if (kind.equals(template.getKind()))
					return template.getTemplateUris();
			}
		}
		return null;
	}
	
	public Gas3Transformer getTransformer() {
		return (getTransformers().isEmpty() ? null : transformers.get(0));
	}
	
	public void setTransformer(Gas3Transformer transformer) {
		Gas3Transformer old = getTransformer();
		getTransformers().clear();
		getTransformers().add(transformer);
		if (_pcs != null)
			_pcs.firePropertyChange("transformer", old, transformer);
	}

	protected List<Gas3Transformer> getTransformers() {
		if (transformers == null)
			transformers = new ArrayList<Gas3Transformer>();
		return transformers;
	}

	public Set<Gas3Translator> getTranslators() {
		if (translators == null)
			translators = new HashSet<Gas3Translator>();
		return translators;
	}

	@Override
	public void validate(ValidationResults results) {
		if (sources != null) {
			for (Validable validable : sources)
				validable.validate(results);
		}
		if (classpaths != null) {
			for (Validable validable : classpaths)
				validable.validate(results);
		}
		if (templates != null) {
			for (Validable validable : templates)
				validable.validate(results);
		}
		if (transformers != null) {
			for (Validable validable : transformers)
				validable.validate(results);
		}
		if (translators != null) {
			for (Validable validable : translators)
				validable.validate(results);
		}
	}

	public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
		if (_pcs == null)
			_pcs = new PropertyChangeSupport(this);
        _pcs.addPropertyChangeListener(name, listener);
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (_pcs == null)
			_pcs = new PropertyChangeSupport(this);
        _pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
		if (_pcs == null)
			_pcs = new PropertyChangeSupport(this);
        _pcs.removePropertyChangeListener(name, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (_pcs == null)
			_pcs = new PropertyChangeSupport(this);
        _pcs.removePropertyChangeListener(listener);
    }
}
