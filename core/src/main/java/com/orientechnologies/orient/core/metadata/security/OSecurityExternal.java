/*
 *
 *  *  Copyright 2014 Orient Technologies LTD (info(at)orientechnologies.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://www.orientechnologies.com
 *
 */
package com.orientechnologies.orient.core.metadata.security;

import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.security.OSecurityAuthenticator;
import com.orientechnologies.orient.core.storage.OStorageProxy;

/**
 * OSecurity implementation that extends OSecurityShared but uses an external authenticator.
 * 
 * @author S. Colin Leister
 * 
 */
public class OSecurityExternal extends OSecurityShared
{
	private OSecurityAuthenticator _Authenticator;
	
	public OSecurityExternal(OSecurityAuthenticator authenticator)
	{
		_Authenticator = authenticator;
	}

	@Override
	public OUser authenticate(final String iUsername, final String iUserPassword)
	{
		OUser user = null;
		final String dbName = getDatabase().getName();

		if(!(getDatabase().getStorage() instanceof OStorageProxy))
		{
			if(_Authenticator == null) throw new OSecurityAccessException(dbName, "Security Authenticator is null!");

			// username is returned if authentication is successful, otherwise null.
			String username = _Authenticator.authenticate(iUsername, iUserPassword);

			if(username != null)
			{
				user = getUser(username);
				
				if(user == null) throw new OSecurityAccessException(dbName, "User or password not valid for username: " + username + ", database: '" + dbName + "'");
				
				if(user.getAccountStatus() != OSecurityUser.STATUSES.ACTIVE) throw new OSecurityAccessException(dbName, "User '" + username + "' is not active");				
			}
			else
			{
				// WAIT A BIT TO AVOID BRUTE FORCE
				try
				{
					Thread.sleep(200);
				}
				catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
		
				throw new OSecurityAccessException(dbName, "User or password not valid for username: " + iUsername + ", database: '" + dbName + "'");
			}
		}
		
		return user;
	}	
}
