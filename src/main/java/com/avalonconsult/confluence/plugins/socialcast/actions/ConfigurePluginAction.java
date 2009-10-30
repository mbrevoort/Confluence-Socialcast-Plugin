package com.avalonconsult.confluence.plugins.socialcast.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.avalonconsult.confluence.plugins.socialcast.SocialcastSettings;
import com.avalonconsult.confluence.plugins.socialcast.SocialcastSettingsManager;

/**
 * User: mike
 * Date: Oct 25, 2009
 * Time: 1:55:34 PM
 */
public class ConfigurePluginAction extends ConfluenceActionSupport {

  private String defaultUsername;
  private String defaultPassword;
  private String apiUrlRoot;
  private SocialcastSettingsManager socialcastSettingsManager;

  public String execute() {
    SocialcastSettings settings = getSocialcastSettings();
    settings.setDefaultUsername(defaultUsername);
    settings.setDefaultPassword(defaultPassword);
    settings.setApiUrlRoot(apiUrlRoot);
    socialcastSettingsManager.save(settings);

    addActionMessage("Configuration Saved"); //getText("webdav.config.saved"));

    return SUCCESS;
  }

  public String doDefault() {
    SocialcastSettings settings = getSocialcastSettings();

    setDefaultUsername(settings.getDefaultUsername());
    setDefaultPassword(settings.getDefaultPassword());
    setApiUrlRoot(settings.getApiUrlRoot());

    return SUCCESS;
  }

  public void validate() {
    super.validate();
  }

  public SocialcastSettings getSocialcastSettings() {
    return socialcastSettingsManager.getSocialcastSettings();
  }

  public String getDefaultUsername() {
    return defaultUsername;
  }

  public void setDefaultUsername(String defaultUsername) {
    this.defaultUsername = defaultUsername;
  }

  public String getDefaultPassword() {
    return defaultPassword;
  }

  public void setDefaultPassword(String defaultPassword) {
    this.defaultPassword = defaultPassword;
  }

  public String getApiUrlRoot() {
    return apiUrlRoot;
  }

  public void setApiUrlRoot(String apiUrlRoot) {
    this.apiUrlRoot = apiUrlRoot;
  }

  public SocialcastSettingsManager getSocialcastSettingsManager() {
    return socialcastSettingsManager;
  }

  public void setSocialcastSettingsManager(SocialcastSettingsManager socialcastSettingsManager) {
    this.socialcastSettingsManager = socialcastSettingsManager;
  }
}
