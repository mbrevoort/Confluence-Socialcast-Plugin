package com.avalonconsult.confluence.plugins.socialcast.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * User: mike
 * Date: Oct 28, 2009
 * Time: 5:02:24 PM
 */
public class XmlUtils {

  public static String getTagValue(Node node, String name) {

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

  public static Node getNode(Node node, String nodeName) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {

      Element el = (Element) node;
      NodeList nodeList = el.getElementsByTagName(nodeName);
      if (nodeList.getLength() > 0) {
        return nodeList.item(0);
      }

    }

    return null;
  }

  public static Node getChildNode(Node node, String nodeName) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {

      NodeList nodeList = node.getChildNodes();
      for(int i=0; i < nodeList.getLength(); i++) {
        Node n = nodeList.item(i);
        if(n.getNodeName().equals(nodeName)) {
          return n;
        }
      }
    }

    return null;
  }

  public static int getCountOfChildNodes(Node node, String nodeName) {
    int count = 0;

    if (node.getNodeType() == Node.ELEMENT_NODE) {

      NodeList nodeList = node.getChildNodes();
      for(int i=0; i < nodeList.getLength(); i++) {
        Node n = nodeList.item(i);
        if(n.getNodeName().equals(nodeName)) {
          count++;
        }
      }

    }

    return count;
  }

}
