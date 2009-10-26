package com.brevoort.confluence.plugins.socialcast;

import java.io.Serializable;

/**
 * User: mike
 * Date: Oct 24, 2009
 * Time: 1:25:57 PM
 */
public class UserAuthInfo implements Serializable {

  private String username;
  private String password;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
