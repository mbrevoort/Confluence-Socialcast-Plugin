package com.avalonconsult.confluence.plugins.socialcast;

import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * This very simple macro shows you the very basic use-case of displaying *something* on the Confluence page where it is used.
 * Use this example macro to toy around, and then quickly move on to the next example - this macro doesn't
 * really show you all the fun stuff you can do with Confluence.
 */
public class SocialcastMacro extends SocialcastBaseMacro {

  static final Category log = Category.getInstance(SocialcastMacro.class);


  public SocialcastMacro(PageManager pageManager, SpaceManager spaceManager, PersonalInformationManager personalInformationManager, ContentPropertyManager contentPropertyManager, CacheManager cacheManager, SocialcastSettingsManager socialcastSettingsManager) {
    super(pageManager, spaceManager, personalInformationManager, contentPropertyManager, cacheManager, socialcastSettingsManager);
  }


  /**
   * This method returns XHTML to be displayed on the page that uses this macro
   * TODO: comments :)
   */
  public String execute(Map params, String body, RenderContext renderContext)
          throws MacroException {

    //StringBuffer result = new StringBuffer();
    Map context = MacroUtils.defaultVelocityContext();
    HttpClient client = new HttpClient();

    User loggedInUser = AuthenticatedUserThreadLocal.getUser();


    String query = (params.get("query") != null) ? (String) params.get("query") : "";
    int maxLength = (params.get("maxLength") != null) ? Integer.parseInt((String)params.get("maxLength"))  : 100;
    String apiUrl = socialcastSettingsManager.getSocialcastSettings().getApiUrlRoot() + "/api/messages/search.xml?q=" + query;

    String cacheKey = apiUrl;
    long cacheTTLSeconds = 0;

    try {
      cacheTTLSeconds = (params.get("secondsToCache") != null) ? Long.parseLong((String) params.get("secondsToCache")) : 0;
    }
    catch (Exception ex) {
      log.debug("Problem parsing secondsToCache parameters " + params.get("secondsToCache"), ex);
    }

    String apiCallResult = getCached(cacheKey);

    if (apiCallResult == null) {
      UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
      Credentials creds = getCredentials();
      client.getState().setCredentials(AuthScope.ANY, creds);
      GetMethod get = new GetMethod(apiUrl);
      get.setDoAuthentication(true);

      try {
        // execute the GET
        int status = client.executeMethod(get);
        if (status == 200) {
          apiCallResult = get.getResponseBodyAsString();
        } else {
          log.error("Error calling Socialcast API (http status: " + status + ") " + apiUrl);
        }
      }
      catch (Exception ex) {
        log.error("Error calling Socialcast API " + apiUrl, ex);
        throw new MacroException(ex);
      }
      finally {
        // release any connection resources used by the method
        get.releaseConnection();
      }
    } else {
      context.put("cached", Boolean.TRUE);
    }

    if (apiCallResult != null) {
      //System.out.println(apiCallResult);
      if (params.get("title") != null)
        context.put("title", params.get("title"));

      try {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(apiCallResult.getBytes("UTF-8")));
        document.getDocumentElement().normalize();
        NodeList nodeLst = document.getElementsByTagName("message");

        ArrayList messages = new ArrayList();
        for (int s = 0; s < nodeLst.getLength(); s++) {

          Node node = nodeLst.item(s);
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            Map itemMap = new HashMap();

            String title = XmlUtils.getTagValue(node, "title");

            if (title == "")
              title = XmlUtils.getTagValue(node, "body");

            itemMap.put("title", dotdotdot(title, maxLength));
            itemMap.put("url", XmlUtils.getTagValue(node, "permalink-url"));
            itemMap.put("icon", XmlUtils.getTagValue(node, "icon"));

            itemMap.put("timeAgo", timeAgo(parseIso8601Date(XmlUtils.getTagValue(node, "created-at"))));
            
            Node userNode = XmlUtils.getNode(node, "user");
            itemMap.put("userUrl", XmlUtils.getTagValue(userNode, "url"));
            itemMap.put("user", XmlUtils.getTagValue(userNode, "username"));
            Node avatarNode = XmlUtils.getNode(userNode, "avatars");
            itemMap.put("userAvatar16", XmlUtils.getTagValue(avatarNode, "square16"));
            itemMap.put("userAvatar30", XmlUtils.getTagValue(avatarNode, "square30"));
            itemMap.put("userAvatar45", XmlUtils.getTagValue(avatarNode, "square45"));
            itemMap.put("userAvatar70", XmlUtils.getTagValue(avatarNode, "square70"));

            messages.add(itemMap);

          }
        }

        context.put("messages", messages);

        // if we get down to here this means that we were able to parse the resposne, so go ahead and cache the apiCallResult
        cache(apiCallResult, cacheKey, cacheTTLSeconds);

        return VelocityUtils.getRenderedTemplate("socialcast/messages.vm", context);

      }
      catch (UnsupportedEncodingException ex) {
        log.error("Socialcast API result is not compatible with UTF-8", ex);
        throw new MacroException(ex);
      }
      catch (ParserConfigurationException ex) {
        log.error("Could not set up XML Parser", ex);
        throw new MacroException(ex);
      }
      catch (SAXException ex) {
        log.error("Error parsing Socialcast API XML response", ex);
        throw new MacroException(ex);
      }
      catch (IOException ex) {
        log.error("Error parsing Socialcast API XML response", ex);
        throw new MacroException(ex);
      }
    }


    return "Error!";  //TODO something more elegant

  }


}