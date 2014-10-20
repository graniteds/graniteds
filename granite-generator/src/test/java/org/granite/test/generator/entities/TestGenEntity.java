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
package org.granite.test.generator.entities;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.granite.generator.Generator;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.generator.java.JavaGroovyTransformer;
import org.granite.generator.javafx.DefaultJavaFX8TypeFactory;
import org.granite.generator.javafx.DefaultJavaFXTypeFactory;
import org.granite.test.generator.MockJavaAs3GroovyConfiguration;
import org.granite.test.generator.MockJavaFXGroovyConfiguration;
import org.granite.test.generator.MockListener;
import org.granite.test.generator.Util;
import org.junit.Assert;
import org.junit.Test;

public class TestGenEntity {

	@Test
	public void testTideTemplateAS3Entity() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity1.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Entity1.class, 
				new File(Entity1.class.getClassLoader().getResource(Entity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains [Lazy]", base.indexOf("[Lazy]\n        public function get entities():ListCollectionView") >= 0);
	}

	@Test
	public void testTideTemplateAS3EntityGDS1046() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityGDS1046.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityGDS1046.class, 
				new File(EntityGDS1046.class.getClassLoader().getResource(EntityGDS1046.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains getter", base.indexOf("public function get isSomething():Boolean") >= 0);
	}

	@Test
	public void testTideTemplateAS3EntityGDS1049() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityGDS1049.class, Entity4.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityGDS1049.class, 
				new File(EntityGDS1049.class.getClassLoader().getResource(EntityGDS1049.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains setter", base.indexOf("public function set parent(value:Entity4):void") >= 0);
	}
	
	
	@Test
	public void testTideTemplateAS3AbstractEntityUid() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(AbstractEntity1.class, Entity3.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(AbstractEntity1.class, 
				new File(AbstractEntity1.class.getClassLoader().getResource(AbstractEntity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("AbstractEntityBase1 contains [Id]", base.indexOf("[Id]") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains _id", base.indexOf("private var _id:Number") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains _createdBy after _id", base.indexOf("private var _id:Number") < base.indexOf("private var _createdBy:String"));
		Assert.assertTrue("AbstractEntityBase1 contains _uid", base.indexOf("private var _uid:String") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains get uid", base.indexOf("public function get uid():String") >= 0);
	}

	
	@Test
	public void testTideTemplateJFXAbstractEntityUid() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(AbstractEntity1.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(AbstractEntity1.class, 
				new File(AbstractEntity1.class.getClassLoader().getResource(AbstractEntity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("AbstractEntityBase1 contains @Id", base.indexOf("@Id") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains id", base.indexOf("private ObjectProperty<Long> id") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains @Serialized", base.indexOf("@Serialized(propertiesOrder={ \"__initialized__\", \"__detachedState__\", \"id\", \"createdBy\", \"uid\", \"version\" })") > 0);
		Assert.assertTrue("AbstractEntityBase1 contains uid", base.indexOf("private StringProperty uid") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains get uid", base.indexOf("public String getUid() {") >= 0);
	}

    @Test
    public void testTideTemplateJFXEntityInclude() throws Exception {
        MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
        config.setTide(true);
        config.addFileSetClasses(Entity5.class);
        JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
            @Override
            public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
                return true;
            }
        };
        Generator generator = new Generator(config);
        generator.add(provider);
        JavaAs3Input input = new JavaAs3Input(Entity5.class,
                new File(Entity5.class.getClassLoader().getResource(Entity5.class.getName().replace('.', '/') + ".class").toURI()));
        JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);

        Assert.assertEquals("Output", 2, outputs.length);

        String sourceBase = Util.readFile(outputs[0].getFile());
        String source = Util.readFile(outputs[1].getFile());

        Assert.assertTrue("Entity5 contains generatedName", sourceBase.indexOf("private ReadOnlyStringWrapper generatedName =") >= 0);
        Assert.assertTrue("Entity5 contains generatedNameProperty", sourceBase.indexOf("public ReadOnlyStringProperty generatedNameProperty()") >= 0);

        checkCompile(new JavaSourceCodeObject("org.granite.test.generator.entities.Entity5Base", sourceBase),
                new JavaSourceCodeObject("org.granite.test.generator.entities.Entity5", source));
    }

	@Test
	public void testTideTemplateJFXBean() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Bean1.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Bean1.class, 
				new File(Bean1.class.getClassLoader().getResource(AbstractEntity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Bean1Base contains name", base.indexOf("private StringProperty name = new SimpleStringProperty(this, \"name\");") >= 0);
		Assert.assertTrue("Bean1Base contains list", base.indexOf("private ReadOnlyListWrapper<String> list = FXPersistentCollections.readOnlyObservablePersistentList(this, \"list\")") >= 0);
        Assert.assertTrue("Bean1Base contains list property", base.indexOf("public ReadOnlyListProperty<String> listProperty()") >= 0);
        Assert.assertTrue("Bean1Base contains list property", base.indexOf("return list.getReadOnlyProperty()") >= 0);
	}
	
	@Test
	public void testTideTemplateJFXEntityDate() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity6.class);
		config.setAs3TypeFactory(new DefaultJavaFX8TypeFactory());
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Entity6.class, 
				new File(Entity6.class.getClassLoader().getResource(Entity6.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Entity6Base contains name", base.indexOf("private StringProperty name = new SimpleStringProperty(this, \"name\");") >= 0);
		Assert.assertTrue("Entity6Base contains date", base.indexOf("private ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<LocalDateTime>(this, \"date\");") >= 0);
	}
	
	@Test
	public void testTideTemplateJFXEntityGDS1128() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity1NoUid.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Entity1NoUid.class, 
				new File(Entity1NoUid.class.getClassLoader().getResource(Entity1NoUid.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String sourceBase = Util.readFile(outputs[0].getFile());
		String source = Util.readFile(outputs[1].getFile());
		
		checkCompile(new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1NoUidBase", sourceBase),
				new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1NoUid", source));
	}
	
	@Test
	public void testTideTemplateJFXEntityGDS1128b() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity1Id.class, Entity1NoUidCompId.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input1 = new JavaAs3Input(Entity1NoUidCompId.class, 
				new File(Entity1NoUidCompId.class.getClassLoader().getResource(Entity1NoUidCompId.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs1 = (JavaAs3Output[])generator.generate(input1);
		JavaAs3Input input2 = new JavaAs3Input(Entity1Id.class, 
				new File(Entity1Id.class.getClassLoader().getResource(Entity1Id.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs2 = (JavaAs3Output[])generator.generate(input2);
		
		Assert.assertEquals("Output", 2, outputs1.length);
		
		String sourceBase = Util.readFile(outputs1[0].getFile());
		String source = Util.readFile(outputs1[1].getFile());
		
		checkCompile(
			new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1NoUidCompIdBase", sourceBase),
			new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1NoUidCompId", source),
			new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1IdBase", Util.readFile(outputs2[0].getFile())),
			new JavaSourceCodeObject("org.granite.test.generator.entities.Entity1Id", Util.readFile(outputs2[1].getFile()))
		);
	}
	
	@Test
	public void testTideTemplateJFXEntityMap() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityMap.class, Entity1.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityMap.class, 
				new File(EntityMap.class.getClassLoader().getResource(EntityMap.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("EntityMapBase contains readOnlyMap", base.indexOf("private ReadOnlyMapWrapper<String, Entity1> readOnlyMap = FXPersistentCollections.readOnlyObservablePersistentMap(this, \"readOnlyMap\");") >= 0);
		Assert.assertTrue("EntityMapBase contains map", base.indexOf("private ReadOnlyMapWrapper<String, Entity1> map = FXPersistentCollections.readOnlyObservablePersistentMap(this, \"map\");") >= 0);
	}
	
	@Test
	public void testTideTemplateJFXEntityByteArray() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity7.class);
		config.setAs3TypeFactory(new DefaultJavaFX8TypeFactory());
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Entity7.class, 
				new File(Entity7.class.getClassLoader().getResource(Entity6.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Entity7Base contains name", base.indexOf("private StringProperty name = new SimpleStringProperty(this, \"name\");") >= 0);
		Assert.assertTrue("Entity7Base contains byteArray", base.indexOf("private ObjectProperty<Byte[]> byteArray = new SimpleObjectProperty<Byte[]>(this, \"byteArray\");") >= 0);
		Assert.assertTrue("Entity7Base contains byteArray2", base.indexOf("private ObjectProperty<byte[]> byteArray2 = new SimpleObjectProperty<byte[]>(this, \"byteArray2\");") >= 0);
	}
	
	@Test
	public void testTideTemplateJFXEntityEnum() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityEnum.class);
		config.setAs3TypeFactory(new DefaultJavaFXTypeFactory());
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityEnum.class, 
				new File(EntityEnum.class.getClassLoader().getResource(EntityEnum.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("EntityEnumBase contains type", base.indexOf("private ObjectProperty<EntityEnum$Type> type = new SimpleObjectProperty<EntityEnum$Type>(this, \"type\");") >= 0);
	}
	
	@Test
	public void testTideTemplateJFXEntityEnumList() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityEnumList.class);
		config.setAs3TypeFactory(new DefaultJavaFXTypeFactory());
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityEnumList.class, 
				new File(EntityEnumList.class.getClassLoader().getResource(EntityEnumList.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("EntityEnumListBase contains types", base.indexOf("private ReadOnlyListWrapper<EntityEnumList$Type> types = FXPersistentCollections.readOnlyObservablePersistentList(this, \"types\");") >= 0);
	}


    public static void main(String[] args) {
        System.out.println(System.getProperty("java.home"));
    }
	
	private void checkCompile(JavaFileObject... sources) {
        File clientJavaJar = new File("granite-client-java/build/libs/").listFiles(new ArtifactFilenameFilter())[0];
        File clientJavaFXJar = new File("granite-client-javafx/build/libs/").listFiles(new ArtifactFilenameFilter())[0];
        File testClasses = new File("test-classes");
        testClasses.mkdirs();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String jfxJar = System.getProperty("java.home") + "/lib/jfxrt.jar";
		String[] options = new String[] {
			"-classpath", jfxJar + File.pathSeparator + clientJavaJar.getPath() + File.pathSeparator + clientJavaFXJar.getPath(),
			"-d", "test-classes"
		};
		Boolean compileOk = compiler.getTask(null, null, null, Arrays.asList(options), null, Arrays.asList(sources)).call();
		Assert.assertTrue("Compilation ok", compileOk);
	}	
	
	static class JavaSourceCodeObject extends SimpleJavaFileObject {
		
	    private String qualifiedName ;
	    private String sourceCode ;
	    
	    protected JavaSourceCodeObject(String name, String code) {
	        super(URI.create("string:///" +name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
	        this.qualifiedName = name ;
	        this.sourceCode = code ;
	    }
	    
	    @Override
	    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
	        return sourceCode ;
	    }
	    
	    public String getQualifiedName() {
	        return qualifiedName;
	    }
	    
	    public void setQualifiedName(String qualifiedName) {
	        this.qualifiedName = qualifiedName;
	    }
	    
	    public String getSourceCode() {
	        return sourceCode;
	    }
	 
	    public void setSourceCode(String sourceCode) {
	        this.sourceCode = sourceCode;
	    }
	}

    public static class ArtifactFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar") && (!name.endsWith("-sources.jar") && !name.endsWith("-javadoc.jar"));
        }
    }
}
