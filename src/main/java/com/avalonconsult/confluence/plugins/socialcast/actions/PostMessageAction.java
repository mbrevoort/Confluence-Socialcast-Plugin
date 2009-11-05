package com.avalonconsult.confluence.plugins.socialcast.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.renderer.v2.macro.MacroException;
import com.avalonconsult.confluence.plugins.socialcast.SocialcastSettingsManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Category;

/**
 * User: mike
 * Date: Nov 3, 2009
 * Time: 8:07:38 PM
 */
public class PostMessageAction  extends ConfluenceActionSupport {

  private static final Category log = Category.getInstance(PostMessageAction.class);
  private String title;
  private String url;
  private SocialcastSettingsManager socialcastSettingsManager;

  public String execute() throws Exception {
    // post the message
    System.out.println("In PostMessageAction");
    String result = SUCCESS;
    HttpClient client = new HttpClient();
    String apiUrl = socialcastSettingsManager.getSocialcastSettings().getApiUrlRoot() + "/api/messages.xml";
    String apiCallResult = null;

    Credentials creds = socialcastSettingsManager.getUserCredentials();
    client.getState().setCredentials(AuthScope.ANY, creds);
    PostMethod post = new PostMethod(apiUrl);
    post.setDoAuthentication(true);
    post.addParameter("message[title]", title);
    if(url != null && !url.trim().equals(""))
      post.addParameter("message[url]", url);

    try {
      // execute the GET
      int status = client.executeMethod(post);

      // expect a status of 201 (created)
      if (status == 201) {
        apiCallResult = post.getResponseBodyAsString();
        System.out.println(apiCallResult);
      } else {
        log.error("Error calling Socialcast API (http status: " + status + ") " + apiUrl);
        result = ERROR;
      }
    }
    catch (Exception ex) {
      log.error("Error calling Socialcast API " + apiUrl, ex);
      result = ERROR;
    }
    finally {
      // release any connection resources used by the method
      post.releaseConnection();
    }

    return result;
  }

  public void validate() {
    super.validate();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public SocialcastSettingsManager getSocialcastSettingsManager() {
    return socialcastSettingsManager;
  }

  public void setSocialcastSettingsManager(SocialcastSettingsManager socialcastSettingsManager) {
    this.socialcastSettingsManager = socialcastSettingsManager;
  }
}
