package org.granite.config;

import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.granite.messaging.amf.io.AMF3DeserializerSecurizer;

public interface AMF3Config extends Config {

	public ObjectOutput newAMF3Serializer(OutputStream out);
	
	public ObjectInput newAMF3Deserializer(InputStream in);
	
	public AMF3DeserializerSecurizer getAmf3DeserializerSecurizer();
}
