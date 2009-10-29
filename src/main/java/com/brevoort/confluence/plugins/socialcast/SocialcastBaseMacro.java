package com.brevoort.confluence.plugins.socialcast;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.brevoort.confluence.plugins.socialcast.actions.EditUserAuthInfoAction;
import com.opensymphony.util.TextUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Category;

/**
 * User: mike
 * Date: Oct 28, 2009
 * Time: 5:05:19 PM
 */
public abstract class SocialcastBaseMacro extends BaseMacro {

  // package private (on purpose)
  final PageManager pageManager;
  final SpaceManager spaceManager;
  SocialcastSettingsManager socialcastSettingsManager;
  PersonalInformationManager personalInformationManager;
  ContentPropertyManager contentPropertyManager;
  final CacheManager cacheManager;
  XStream xStream;
  static final Category log = Category.getInstance(EditUserAuthInfoAction.class);
  private static final String CACHE_KEY = "com.brevoort.confluence.plugins.socialcast.SocialcastBaseMacro";

  public SocialcastBaseMacro(PageManager pageManager, SpaceManager spaceManager, PersonalInformationManager personalInformationManager, ContentPropertyManager contentPropertyManager, CacheManager cacheManager) {
    this.pageManager = pageManager;
    this.spaceManager = spaceManager;
    this.contentPropertyManager = contentPropertyManager;
    this.personalInformationManager = personalInformationManager;
    this.socialcastSettingsManager = (SocialcastSettingsManager) ContainerManager.getComponent("socialcastSettingsManager");
    this.cacheManager = cacheManager;
  }

  public boolean isInline() {
    return false;
  }

  public boolean hasBody() {
    return false;
  }

  public RenderMode getBodyRenderMode() {
    return RenderMode.NO_RENDER;
  }

  public void setxStream(XStream xStream) {
    this.xStream = xStream;
  }

  protected Credentials getCredentials() {
    String username = null;
    String password = null;
    User loggedInUser = AuthenticatedUserThreadLocal.getUser();
    UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
    if (userAuthInfo != null && userAuthInfo.getUsername() != null && !userAuthInfo.getUsername().trim().equals("")) {
      username = userAuthInfo.getUsername();
      password = userAuthInfo.getPassword();
    } else {
      username = socialcastSettingsManager.getSocialcastSettings().getDefaultUsername();
      password = socialcastSettingsManager.getSocialcastSettings().getDefaultPassword();
    }

    return new UsernamePasswordCredentials(username, password);

  }

  protected UserAuthInfo getUserAuthInfo(User user) {
    String userAuthInfoXml = contentPropertyManager.getTextProperty(personalInformationManager.getPersonalInformation(user),
            EditUserAuthInfoAction.USERAUTHINFO_PROPERTY_KEY);

    if (xStream == null) {
      xStream = new XStream();
      xStream.setClassLoader(UserAuthInfo.class.getClassLoader());
      xStream.alias("user-auth-info", UserAuthInfo.class);
    }
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

  /* CACHE METHODS */
  protected void cache(String content, String key, long ttl) {
    Cache cache = cacheManager.getCache(CACHE_KEY);
    cache.put(key, new CachedObjectWrapper(content, ttl));
  }

  /* get a cached String value if it is not expired */
  protected String getCached(String key) {
    String returnValue = null;
    Cache cache = cacheManager.getCache(CACHE_KEY);
    CachedObjectWrapper cachedObject = null;
    try {
      cachedObject = (CachedObjectWrapper) cache.get(key);
    }
    catch (ClassCastException ex) {
      log.debug("Found an incompatable object in Socialcast API cache", ex);
      cache.remove(key);
    }

    if (cachedObject != null) {
      if (cachedObject.isExpired()) {
        cache.remove(key);
      } else {
        try {
          returnValue = (String) cachedObject.getContents();
          log.debug("FOUND CACHED VALUE: " + key);
        } catch (ClassCastException cce) {
          log.debug("Could not caste cached object to String", cce);
          cache.remove(key);
        }
      }
    }

    return returnValue;

  }


}
