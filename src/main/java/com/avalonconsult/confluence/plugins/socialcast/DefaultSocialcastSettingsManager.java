package com.avalonconsult.confluence.plugins.socialcast;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
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

  private final XStream xStream;

  public DefaultSocialcastSettingsManager(BandanaManager bandanaManager, CacheManager cacheManager) {
    this.bandanaManager = bandanaManager;
    this.cacheManager = cacheManager;
    this.xStream = new XStream();
    xStream.setClassLoader(getClass().getClassLoader());
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
      LOG.warn("Unable to cast the cached SocialcastSettings retrieved with key " + cacheEntryKey + " to a SocialcastSettings. It will be purged from the cache.", cce);
      socialcastSettingsCache.remove(cacheEntryKey);
    }

    return null;
  }

  private void cacheSettings(SocialcastSettings socialcastSettings) {
    Cache socialcastSettingsCache = cacheManager.getCache(CACHE_KEY);
    socialcastSettingsCache.put(getCacheEntryKey(), socialcastSettings);
  }

  public void save(SocialcastSettings socialcastSettings) {
    bandanaManager.setValue(
            ConfluenceBandanaContext.GLOBAL_CONTEXT,
            SETTINGS_KEY,
            xStream.toXML(socialcastSettings)
    );
    cacheSettings(new SocialcastSettings(socialcastSettings));
  }

  public SocialcastSettings getSocialcastSettings() {
    SocialcastSettings socialcastSettings = getCachedSetings();

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


}
