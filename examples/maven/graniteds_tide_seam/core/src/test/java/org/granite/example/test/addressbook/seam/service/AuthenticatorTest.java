package org.granite.example.test.addressbook.seam.service;

import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.granite.example.addressbook.seam.service.Authenticator;


/**
 * Demonstrate on how to use embedded jboss to test seam component.
 *
 * @author edward.yakop@gmail.com
 * @since 2.1.0.GA
 */
public class AuthenticatorTest extends SeamTest
{
    @Test
    public void testAuthenticate()
        throws Exception
    {
        new ComponentTest()
        {
            @Override
            protected final void testComponents()
                throws Exception
            {
                Authenticator authenticator = (Authenticator) getInstance( "authenticator" );
                assertNotNull( authenticator );

                Identity identity = Identity.instance();
                Credentials credentials = identity.getCredentials();

                // Admin
                credentials.setUsername( "admin" );
                credentials.setPassword( "admin" );
                assertTrue( authenticator.authenticate() );
            }
        }.run();
    }
}
