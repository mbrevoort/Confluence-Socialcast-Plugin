package com.brevoort.confluence.plugins.socialcast.actions;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.user.actions.AbstractUserProfileAction;
import com.atlassian.user.User;
import com.brevoort.confluence.plugins.socialcast.UserAuthInfo;
import com.opensymphony.util.TextUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Category;

/**
 * User: mike
 * Date: Oct 24, 2009
 * Time: 1:27:56 PM
 */

public class EditUserAuthInfoAction extends AbstractUserProfileAction {

  private static final Category log = Category.getInstance(EditUserAuthInfoAction.class);
  public static final String USERAUTHINFO_PROPERTY_KEY = "com.brevoort.confluence.plugins.socialcast.userauthinfo";

  private ContentPropertyManager contentPropertyManager;
  private XStream xStream;

  private void setUserAuthInfo(User user, UserAuthInfo userAuthInfo) {
    contentPropertyManager.setTextProperty(personalInformationManager.getPersonalInformation(user),
            USERAUTHINFO_PROPERTY_KEY, xStream.toXML(userAuthInfo));
  }

  private UserAuthInfo getUserAuthInfo(User user) {
    String userAuthInfoXml = contentPropertyManager.getTextProperty(personalInformationManager.getPersonalInformation(user),
            USERAUTHINFO_PROPERTY_KEY);

    if (TextUtils.stringSet(userAuthInfoXml)) {
      try {
        return (UserAuthInfo) xStream.fromXML(userAuthInfoXml);
      }
      catch (Throwable t) {
        log.warn("Error unpacking user's personal information: " + user + ": " + t.getMessage(), t);
      }
    }

    return new UserAuthInfo();
  }

  public String doDefault() throws Exception {
    userAuthInfo = getUserAuthInfo(getRemoteUser());
    System.out.println("test " + userAuthInfo);
    return super.doDefault();
  }

  public String execute() throws Exception {
    setUserAuthInfo(getRemoteUser(), userAuthInfo);
    System.out.println("test " + userAuthInfo);
    return super.execute();
  }


  public void setContentPropertyManager(ContentPropertyManager contentPropertyManager) {
    this.contentPropertyManager = contentPropertyManager;
  }

  public void setxStream(XStream xStream) {
    this.xStream = xStream;
  }

  private UserAuthInfo userAuthInfo;

  private String scUsername;
  private String scPassword;

  public String getScPassword() {
    if (userAuthInfo == null)
      userAuthInfo = new UserAuthInfo();
    return userAuthInfo.getPassword();
  }

  public void setScPassword(String scPassword) {
    if (userAuthInfo == null)
      userAuthInfo = new UserAuthInfo();
    userAuthInfo.setPassword(scPassword);
  }

  public String getScUsername() {
    if (userAuthInfo == null)
      userAuthInfo = new UserAuthInfo();

    return userAuthInfo.getUsername();
  }

  public void setScUsername(String scUsername) {
    if (userAuthInfo == null)
      userAuthInfo = new UserAuthInfo();
    userAuthInfo.setUsername(scUsername);
  }
}

