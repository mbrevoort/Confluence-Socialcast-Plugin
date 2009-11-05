package com.avalonconsult.confluence.plugins.socialcast;

import org.apache.commons.httpclient.Credentials;
import com.atlassian.user.User;

/**
 * User: mike
 * Date: Oct 25, 2009
 * Time: 2:06:56 PM
 */
public interface SocialcastSettingsManager {

  public void save(SocialcastSettings socialcastSettings);

  public SocialcastSettings getSocialcastSettings();

  public String getDefaultUsername();

  public String getDefaultPassword();

  public String getApiUrlRoot();

  public Credentials getCredentials();

  public Credentials getUserCredentials();

  public UserAuthInfo getUserAuthInfo(User user);


}
