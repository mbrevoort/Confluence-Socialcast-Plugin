package com.brevoort.confluence.plugins.socialcast;

import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.PersonalInformationManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;
import com.atlassian.spring.container.ContainerManager;
import com.brevoort.confluence.plugins.socialcast.actions.EditUserAuthInfoAction;
import com.opensymphony.util.TextUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Map;


/**
 * This very simple macro shows you the very basic use-case of displaying *something* on the Confluence page where it is used.
 * Use this example macro to toy around, and then quickly move on to the next example - this macro doesn't
 * really show you all the fun stuff you can do with Confluence.
 */
public class SocialcastMacro extends BaseMacro {

  // We just have to define the variables and the setters, then Spring injects the correct objects for us to use. Simple and efficient.
  // You just need to know *what* you want to inject and use.

  private final PageManager pageManager;
  private final SpaceManager spaceManager;
  private SocialcastSettingsManager socialcastSettingsManager;
  private PersonalInformationManager personalInformationManager;
  private ContentPropertyManager contentPropertyManager;
  private XStream xStream;
  private static final Category log = Category.getInstance(EditUserAuthInfoAction.class);


  public SocialcastMacro(PageManager pageManager, SpaceManager spaceManager, PersonalInformationManager personalInformationManager, ContentPropertyManager contentPropertyManager) {
    this.pageManager = pageManager;
    this.spaceManager = spaceManager;
    this.contentPropertyManager = contentPropertyManager;
    this.personalInformationManager = personalInformationManager;
    this.socialcastSettingsManager = (SocialcastSettingsManager) ContainerManager.getComponent("socialcastSettingsManager");
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

  /**
   * This method returns XHTML to be displayed on the page that uses this macro
   * we just do random stuff here, trying to show how you can access the most basic
   * managers and model objects. No emphasis is put on beauty of code nor on
   * doing actually useful things :-)
   */
  public String execute(Map params, String body, RenderContext renderContext)
          throws MacroException {

    // in this most simple example, we build the result in memory, appending HTML code to it at will.
    // this is something you absolutely don't want to do once you start writing plugins for real. Refer
    // to the next example for better ways to render content.
    StringBuffer result = new StringBuffer();

    HttpClient client = new HttpClient();

    User loggedInUser = AuthenticatedUserThreadLocal.getUser();
    UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
    System.out.println(userAuthInfo.getUsername());
    Credentials creds = getCredentials();
    client.getState().setCredentials(AuthScope.ANY, creds);
    String query = (params.get("query") != null) ? (String) params.get("query") : "a";
    GetMethod get = new GetMethod(socialcastSettingsManager.getSocialcastSettings().getApiUrlRoot() + "/api/messages/search.xml?q=" + query);
    get.setDoAuthentication(true);

    if (params.get("title") != null)
      result.append("<h2>" + params.get("title") + "</h2>");

    try {
      // execute the GET
      int status = client.executeMethod(get);
      //System.out.println(get.getResponseBodyAsString());

      if (status == 200) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(get.getResponseBodyAsStream());
        document.getDocumentElement().normalize();
        NodeList nodeLst = document.getElementsByTagName("message");

        for (int s = 0; s < nodeLst.getLength(); s++) {

          Node node = nodeLst.item(s);
          String title = getTagValue(node, "title");
          if (title == "")
            title = getTagValue(node, "body");

          String url = getTagValue(node, "permalink-url");

          if (node.getNodeType() == Node.ELEMENT_NODE) {
            result.append("<a href='" + url + "'>" + title + "</a><br/>");
          }
        }
      } else {
        result.append("Error retrieving results: " + status);
      }
    }
    catch (Exception ex) {
      System.out.println(ex);
    }
    finally {
      // release any connection resources used by the method
      get.releaseConnection();
    }


    return result.toString();
  }


  public void setxStream(XStream xStream) {
    this.xStream = xStream;
  }
  

  private UserAuthInfo getUserAuthInfo(User user) {
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

  private String getTagValue(Node node, String name) {

    if (node.getNodeType() == Node.ELEMENT_NODE) {

      Element el = (Element) node;
      NodeList nodeList = el.getElementsByTagName(name);
      if (nodeList.getLength() > 0) {
        el = (Element) nodeList.item(0);
        nodeList = el.getChildNodes();

        if (nodeList.getLength() > 0) {
          return ((Node) nodeList.item(0)).getNodeValue();
        }
      }

    }
    return "";
  }

  private Credentials getCredentials() {
    String username = null;
    String password = null;
    User loggedInUser = AuthenticatedUserThreadLocal.getUser();
    UserAuthInfo userAuthInfo = getUserAuthInfo(loggedInUser);
    if(userAuthInfo != null && userAuthInfo.getUsername() != null && !userAuthInfo.getUsername().trim().equals("")) {
      username = userAuthInfo.getUsername();
      password = userAuthInfo.getPassword();
    } else {
      username = socialcastSettingsManager.getSocialcastSettings().getDefaultUsername();
      password = socialcastSettingsManager.getSocialcastSettings().getDefaultPassword();
    }

    return new UsernamePasswordCredentials(username, password);

  }

}