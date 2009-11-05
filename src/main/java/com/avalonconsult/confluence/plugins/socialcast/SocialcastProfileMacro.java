package com.avalonconsult.confluence.plugins.socialcast;

import com.atlassian.cache.CacheManager;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;
import com.avalonconsult.confluence.plugins.socialcast.util.XmlUtils;
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

/**
 * User: mike
 * Date: Oct 28, 2009
 * Time: 4:56:47 PM
 */
public class SocialcastProfileMacro extends SocialcastBaseMacro {

  static final Category log = Category.getInstance(SocialcastProfileMacro.class);


  public SocialcastProfileMacro(PageManager pageManager, SpaceManager spaceManager, CacheManager cacheManager, SocialcastSettingsManager socialcastSettingsManager) {
    super(pageManager, spaceManager, cacheManager, socialcastSettingsManager);
  }

  /**
   * This method returns XHTML to be displayed on the page that uses this macro
   * TODO: comments :)
   */
  public String execute(Map params, String body, RenderContext renderContext)
          throws MacroException {

    // TODO: cleanup and change to use velocity template
    StringBuffer result = new StringBuffer();

    HttpClient client = new HttpClient();

    User loggedInUser = AuthenticatedUserThreadLocal.getUser();


    String user = (params.get("user") != null) ? (String) params.get("user") : "";
    String apiUrl = socialcastSettingsManager.getSocialcastSettings().getApiUrlRoot() + "/api/users/" + user + ".xml";

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
      Credentials creds = socialcastSettingsManager.getCredentials();
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
      result.append("<!-- CACHED RESULT --> ");
    }

    if (apiCallResult != null) {
      if (params.get("title") != null)
        result.append("<h2>" + params.get("title") + "</h2>");

      try {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(apiCallResult.getBytes("UTF-8")));
        document.getDocumentElement().normalize();
        NodeList nodeLst = document.getElementsByTagName("user");

        if (0 < nodeLst.getLength()) {

          Node node = nodeLst.item(0);
          String name = XmlUtils.getTagValue(node, "name");
          if (name == "")
            name = XmlUtils.getTagValue(node, "username");

          //String url = XmlUtils.getTagValue(node, "permalink-url");
          Node statusNode = XmlUtils.getNode(node, "status-message");
          String lastStatus = XmlUtils.getTagValue(statusNode, "title");
          if (lastStatus == "")
            lastStatus = XmlUtils.getTagValue(node, "body");
          //String user = XmlUtils.getTagValue(statusNode, "username");

          if (node.getNodeType() == Node.ELEMENT_NODE) {
            result.append("Name: " + name + "<br/>"); //<a href='" + url + "'>" + title + "</a> by <a href='" + userUrl + "'>" + user + "</a><br/>");
            result.append("Last Status: " + lastStatus + "<br/>");
          }
        }

        // if we get down to here this means that we were able to parse the resposne, so go ahead and cache the apiCallResult
        cache(apiCallResult, cacheKey, cacheTTLSeconds);
        return result.toString();

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
