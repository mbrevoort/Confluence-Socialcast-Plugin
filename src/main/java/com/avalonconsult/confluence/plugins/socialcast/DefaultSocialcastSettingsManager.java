package com.avalonconsult.confluence.plugins.socialcast;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.user.User;
import com.thoughtworks.xstream.XStream;
import com.avalonconsult.confluence.plugins.socialcast.actions.EditUserAuthInfoAction;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;

/**
 * User: mike
 * Date: Oct 25, 2009
 * Time: 2:28:23 PM
 * Persists and retrieves {@link com.avalonconsult.confluence.plugins.socialcast.SocialcastSettings} to/via Bandana.
 * The implementation caches the settings so you don't need to worry about object&lt;-&gt;XML serialization/deserialization overhead.
 */
public class DefaultSocialcastSettingsManager implements SocialcastSettingsManager {

  private static final Logger LOG = Logger.getLogger(DefaultSocialcastSettingsManager.class);

  private static final String CACHE_KEY = "com.avalonconsult.confluence.plugins.socialcast.settings";

  private static final String SETTINGS_KEY = "com.avalonconsult.confluence.plugins.socialcast.settings";

  private final BandanaManager bandanaManager;

  private final CacheManager cacheManager;

  private final ContentPropertyManager contentPropertyManager;

  private final PersonalInformationManager personalInformationManager;

  public DefaultSocialcastSettingsManager(BandanaManager bandanaManager, CacheManager cacheManager,
                                          ContentPropertyManager contentPropertyManager,
                                          PersonalInformationManager personalInformationManager) {
    this.bandanaManager = bandanaManager;
    this.cacheManager = cacheManager;
    this.personalInformationManager = personalInformationManager;
    this.contentPropertyManager = contentPropertyManager;
  }

  private String getCacheEntryKey() {
    return CACHE_KEY + ".global"; /* More logical, and just maybe we can expand on this to support personalized settings */
  }

  private SocialcastSettings getCachedSetings() {
    Cache socialcastSettingsCache = cacheManager.getCache(CACHE_KEY);
    String cacheEntryKey = getCacheEntryKey();
    try {
      return (SocialcastSettings) socialcastSettingsCache.get(cacheEntryKey);
    }
    catch (ClassCastException cce) {
      // If this happens, either someone stored something that is not a SocialcastSettings into the cache with the same key
      // or the plugin was upgraded and the SocialcastSettings loaded by the previous class loader can't be cast to
      // a SocialcastSettings.
      LOG.debug("Unable to cast the cached SocialcastSettings retrieved with key " + cacheEntryKey + " to a SocialcastSettings. It will be purged from the cache.", cce);
      socialcastSettingsCache.remove(cacheEntryKey);
    }

    return null;
  }

  private void cacheSettings(SocialcastSettings socialcastSettings) {
    Cache socialcastSettingsCache = cacheManager.getCache(CACHE_KEY);
    socialcastSettingsCache.put(getCacheEntryKey(), socialcastSettings);
  }

  public void save(SocialcastSettings socialcastSettings) {
    XStream xStream = new XStream();
    xStream.setClassLoader(getClass().getClassLoader());

    bandanaManager.setValue(
        ConfluenceBandanaContext.GLOBAL_CONTEXT,
        SETTINGS_KEY,
        xStream.toXML(socialcastSettings)
    );
    cacheSettings(new SocialcastSettings(socialcastSettings));
  }

  public SocialcastSettings getSocialcastSettings() {
    SocialcastSettings socialcastSettings = getCachedSetings();
    XStream xStream = new XStream();
    xStream.setClassLoader(getClass().getClassLoader());

    if (null != socialcastSettings)
      return new SocialcastSettings(socialcastSettings); /* Because SocialcastSettings is not immutable */

    String socialcastSettingsXml = (String) bandanaManager.getValue(ConfluenceBandanaContext.GLOBAL_CONTEXT, SETTINGS_KEY);

    socialcastSettings = StringUtils.isNotBlank(socialcastSettingsXml)
            ? (SocialcastSettings) xStream.fromXML(socialcastSettingsXml)
            : new SocialcastSettings();

    cacheSettings(new SocialcastSettings(socialcastSettings));

    return socialcastSettings;
  }


  public String getDefaultUsername() {
    return getSocialcastSettings().getDefaultUsername();
  }

  public String getDefaultPassword() {
    return getSocialcastSettings().getDefaultPassword();
  }

  public String getApiUrlRoot() {
    return getSocialcastSettings().getApiUrlRoot();
  }


  public Credentials getCredentials() {
    String username = null;
    String password = null;
    User loggedInUser = AuthenticatedUserThreadLocal.getUser();
    UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
    if (userAuthInfo != null && userAuthInfo.getUsername() != null && !userAuthInfo.getUsername().trim().equals("")) {
      username = userAuthInfo.getUsername();
      password = userAuthInfo.getPassword();
    } else {
      username = this.getSocialcastSettings().getDefaultUsername();
      password = this.getSocialcastSettings().getDefaultPassword();
    }

    return new UsernamePasswordCredentials(username, password);

  }


  public Credentials getUserCredentials() {
    String username = null;
    String password = null;
    User loggedInUser = AuthenticatedUserThreadLocal.getUser();
    UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
    if (userAuthInfo != null && userAuthInfo.getUsername() != null && !userAuthInfo.getUsername().trim().equals("")) {
      username = userAuthInfo.getUsername();
      password = userAuthInfo.getPassword();
      return new UsernamePasswordCredentials(username, password);
    } else {
      return null;
    }

  }

  public UserAuthInfo getUserAuthInfo(User user) {
    String userAuthInfoXml = contentPropertyManager.getTextProperty(personalInformationManager.getPersonalInformation(user),
            EditUserAuthInfoAction.USERAUTHINFO_PROPERTY_KEY);

    XStream xStream = new XStream();
    xStream.setClassLoader(UserAuthInfo.class.getClassLoader());
    xStream.alias("user-auth-info", UserAuthInfo.class);

    if (TextUtils.stringSet(userAuthInfoXml)) {
      try {
        return (UserAuthInfo) xStream.fromXML(userAuthInfoXml);
      }
      catch (Throwable t) {
        LOG.warn("Error unpacking user's personal information: " + user + ": " + t.getMessage(), t);
      }
    }

    return new UserAuthInfo();
  }



}
