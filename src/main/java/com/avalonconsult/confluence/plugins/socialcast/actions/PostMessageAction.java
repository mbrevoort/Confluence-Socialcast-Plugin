package com.avalonconsult.confluence.plugins.socialcast.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.avalonconsult.confluence.plugins.socialcast.SocialcastSettingsManager;
import com.opensymphony.webwork.ServletActionContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Credentials;
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
  private String link;
  private SocialcastSettingsManager socialcastSettingsManager;
  private int status = 0;

  public String execute() throws Exception {
    // post the message

    String result = SUCCESS;
    HttpClient client = new HttpClient();
    String apiUrl = socialcastSettingsManager.getSocialcastSettings().getApiUrlRoot() + "/api/messages.xml";
    String apiCallResult = null;


    Credentials creds = socialcastSettingsManager.getUserCredentials();
    client.getState().setCredentials(AuthScope.ANY, creds);
    PostMethod post = new PostMethod(apiUrl);
    post.setDoAuthentication(true);
    post.addParameter("message[title]", title);
    if(link != null && !link.trim().equals(""))
      post.addParameter("message[url]", link);

    System.out.println(post.getParameter("message[url]"));

    try {
      // execute the GET
      status = client.executeMethod(post);
      ServletActionContext.getRequest().setAttribute("status", status);

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

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public SocialcastSettingsManager getSocialcastSettingsManager() {
    return socialcastSettingsManager;
  }

  public void setSocialcastSettingsManager(SocialcastSettingsManager socialcastSettingsManager) {
    this.socialcastSettingsManager = socialcastSettingsManager;
  }

  public int getStatus() {
    return status;
  }
}
