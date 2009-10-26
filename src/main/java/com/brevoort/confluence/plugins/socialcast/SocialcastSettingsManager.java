package com.brevoort.confluence.plugins.socialcast;

/**
 * User: mike
 * Date: Oct 25, 2009
 * Time: 2:06:56 PM
 */
public interface SocialcastSettingsManager {

  SocialcastSettings getSocialcastSettings();

  String getDefaultUsername();

  String getDefaultPassword();

  String getApiUrlRoot();

  void save(SocialcastSettings socialcastSettings);
}
