package com.brevoort.confluence.plugins.socialcast;

import java.util.Date;

/**
 * User: mike
 * Date: Oct 29, 2009
 * Time: 11:07:20 AM
 * <p/>
 * Confluence cache management infterfaces don't appear to allow you to manage expiration time we'll use this object
 * as a wrapper around the cached object and store a creation timestamp with it so we can know whether the object
 * is still valid
 */
public class CachedObjectWrapper {
  private Date expiration;
  private Date created;
  private Object contents;

  public CachedObjectWrapper() {
    created = new Date();
  }

  public CachedObjectWrapper(Object o) {
    created = new Date();
    this.contents = o;
  }

  /**
   * @param o          object to be cached
   * @param expiration time to live (TTL) in seconds
   */
  public CachedObjectWrapper(Object o, long expiration) {
    created = new Date();
    this.contents = o;
    this.expiration = new Date(System.currentTimeMillis() + (expiration * 1000));
  }


  public Date getExpiration() {
    return expiration;
  }

  public void setExpiration(Date expiration) {
    this.expiration = expiration;
  }

  /**
   * Set the expiration TTL by the number of seconds from the current time
   *
   * @param expiration time to live (TTL) in seconds
   */
  public void setExpiration(long expiration) {
    this.expiration = new Date(System.currentTimeMillis() + (expiration * 1000));
  }

  public Object getContents() {
    return contents;
  }

  public void setContents(Object contents) {
    this.contents = contents;
  }

  public Date getCreated() {
    return created;
  }

  /**
   * Will return false if expiration not set ie never expires
   *
   * @return true if cache should be expired
   */
  public boolean isExpired() {
    if (expiration != null && expiration.before(new Date())) {
      return true;
    } else {
      return false;
    }
  }
}
