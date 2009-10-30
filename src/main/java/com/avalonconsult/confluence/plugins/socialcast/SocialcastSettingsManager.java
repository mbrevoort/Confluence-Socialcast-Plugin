package com.avalonconsult.confluence.plugins.socialcast;

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


}
