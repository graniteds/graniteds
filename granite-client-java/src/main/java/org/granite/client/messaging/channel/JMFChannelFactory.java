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
package org.granite.client.messaging.channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.granite.client.messaging.channel.amf.JMFRemotingChannel;
import org.granite.client.messaging.codec.JMFAMF0MessagingCodec;
import org.granite.client.messaging.codec.JMFAMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Reflection;
import org.granite.util.ContentType;
import org.granite.util.JMFAMFUtil;

/**
 * Implementation of a ChannelFactory using JMF serialization
 *
 * @author Franck WOLFF
 */
public class JMFChannelFactory extends AbstractChannelFactory {

	private ClientSharedContext sharedContext = null;
	
	private List<ExtendedObjectCodec> extendedCodecs = null;
	private List<String> defaultStoredStrings = null;
	private Reflection reflection = null;

    /**
     * Create a default JMF channel factory with a basic configuration
     */
	public JMFChannelFactory() {
		super(ContentType.JMF_AMF);
	}

    /**
     * Create a default JMF channel factory with the basic configuration and for the specified platform context
     * @param context platform context
     * @see org.granite.client.platform.Platform
     */
	public JMFChannelFactory(Object context) {
		super(ContentType.JMF_AMF, context);
	}

    /**
     * Create an AMF channel factory with the specified configuration and transports and for the specified platform context
     * @param context platform context
     * @param sharedContext serialization context
     * @param remotingTransport remoting transport
     * @param messagingTransport messaging transport
     */
	public JMFChannelFactory(Object context, ClientSharedContext sharedContext, Transport remotingTransport, Transport messagingTransport) {
		super(ContentType.JMF_AMF, context, remotingTransport, messagingTransport);
		
		this.sharedContext = sharedContext;
	}

    /**
     * Current shared serialization context
     * @return shared context
     */
	public ClientSharedContext getSharedContext() {
		return sharedContext;
	}

    /**
     * Set current shared serialization context
     * @param sharedContext shared context
     */
	public void setSharedContext(ClientSharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}

    /**
     * Current extended codecs
     * @return extended codecs
     */
	public List<ExtendedObjectCodec> getExtendedCodecs() {
		return extendedCodecs;
	}

    /**
     * Set custom extended codecs
     * @param extendedCodecs extended codecs
     */
	public void setExtendedCodecs(List<ExtendedObjectCodec> extendedCodecs) {
		this.extendedCodecs = extendedCodecs;
	}

    /**
     * Current stored strings
     * @return stored strings
     */
	public List<String> getDefaultStoredStrings() {
		return defaultStoredStrings;
	}

    /**
     * Set stored strings
     * @param defaultStoredStrings stored strings
     */
	public void setDefaultStoredStrings(List<String> defaultStoredStrings) {
		this.defaultStoredStrings = defaultStoredStrings;
	}

    /**
     * Current reflection provider
     * @return reflection
     */
	public Reflection getReflection() {
		return reflection;
	}

    /**
     * Set reflection provider
     * @param reflection
     */
	public void setReflection(Reflection reflection) {
		this.reflection = reflection;
	}

	@Override
	public void start() {
		super.start();
		
		if (sharedContext == null) {
			
			extendedCodecs = (extendedCodecs != null ? extendedCodecs : new ArrayList<ExtendedObjectCodec>(Arrays.asList(new ClientEntityCodec())));
			defaultStoredStrings = (defaultStoredStrings != null ? defaultStoredStrings : new ArrayList<String>(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS));
			reflection = (reflection != null ? reflection : Platform.reflection());
			
			sharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(extendedCodecs), defaultStoredStrings, reflection, aliasRegistry);
		}
	}

	@Override
	public void stop(boolean stopTransports) {
		try {
			super.stop(stopTransports);
		}
		finally {
			sharedContext = null;
			
			extendedCodecs = null;
			defaultStoredStrings = null;
			reflection = null;
		}
	}

	@Override
	protected Class<? extends RemotingChannel> getRemotingChannelClass() {
        return JMFRemotingChannel.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    protected <M> MessagingCodec<M> newMessagingCodec(Class<M> messageClass) {
        if (messageClass == flex.messaging.messages.Message[].class)
            return (MessagingCodec<M>)new JMFAMF3MessagingCodec(getSharedContext());
        else if (messageClass == AMF0Message.class)
            return (MessagingCodec<M>)new JMFAMF0MessagingCodec(getSharedContext());
        throw new IllegalArgumentException("Unknown message class " + messageClass);
    }
}
