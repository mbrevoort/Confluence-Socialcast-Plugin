package com.avalonconsult.confluence.plugins.socialcast;

import org.apache.log4j.Category;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.cache.CacheManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

/**
 * User: mike
 * Date: Nov 3, 2009
 * Time: 7:36:50 PM
 */
public class SocialcastPostMacro extends SocialcastBaseMacro {

  static final Category log = Category.getInstance(SocialcastPostMacro.class);


  public SocialcastPostMacro(PageManager pageManager, SpaceManager spaceManager, CacheManager cacheManager, SocialcastSettingsManager socialcastSettingsManager) {
    super(pageManager, spaceManager, cacheManager, socialcastSettingsManager);
  }

  /**
   * Render a form to post a message to Socialcast via AJAX
   * @param params
   * @param body
   * @param renderContext
   * @return
   * @throws MacroException
   */
  public String execute(Map params, String body, RenderContext renderContext)
          throws MacroException {

    Map context = MacroUtils.defaultVelocityContext();
    HttpClient client = new HttpClient();
    context.put("key", new Random().nextInt());

    User loggedInUser = AuthenticatedUserThreadLocal.getUser();

    Credentials creds = socialcastSettingsManager.getUserCredentials();

    if(creds != null) {
      return VelocityUtils.getRenderedTemplate("socialcast/postMessageForm.vm", context);
    } else {
      return VelocityUtils.getRenderedTemplate("socialcast/postMessageNoCred.vm", context);
    }
  }


}
