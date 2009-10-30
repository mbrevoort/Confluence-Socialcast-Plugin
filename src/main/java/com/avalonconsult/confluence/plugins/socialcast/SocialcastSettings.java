package com.avalonconsult.confluence.plugins.socialcast;

import java.io.Serializable;

/**
 * User: mike
 * Date: Oct 25, 2009
 * Time: 2:07:41 PM
 */
public class SocialcastSettings implements Serializable {

  private String defaultUsername;
  private String defaultPassword;
  private String apiUrlRoot;

  public SocialcastSettings() {
    this(null);
  }

  public SocialcastSettings(SocialcastSettings socialcastSettings) {
    defaultUsername = "";
    defaultPassword = "";
    apiUrlRoot = "";

    if (null != socialcastSettings) {
      setDefaultUsername(socialcastSettings.getDefaultUsername());
      setDefaultPassword(socialcastSettings.getDefaultPassword());
      setApiUrlRoot(socialcastSettings.getApiUrlRoot());
    }
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SocialcastSettings that = (SocialcastSettings) o;

    if (defaultUsername != that.defaultUsername) return false;
    if (defaultPassword != that.defaultPassword) return false;
    if (apiUrlRoot != that.apiUrlRoot) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = defaultUsername.hashCode();
    result = 31 * result + defaultPassword.hashCode();
    result = 31 * result + apiUrlRoot.hashCode();
    return result;
  }

}
