package gov.nih.nci.evs.restapi.util;

import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.XMLConstants;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class XmlDomParser {

    public static Document createDocument(String filename) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(filename);
            Document document = builder.parse(file);
            return document;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Element getRootElement(Document document) {
		try {
            document.getDocumentElement().normalize();
            // Step 3: Get the root element
            Element root = document.getDocumentElement();
            return root;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

 	public static NodeList getElementsByTagName(Element root, String tagName) {
		try {
            NodeList nodeList = root.getElementsByTagName(tagName);//"owl:Axiom");
            return nodeList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

  	public static NodeList getNodeListByTagName(String filename, String tagName) {
		try {
			Document document = createDocument(filename);
			return getNodeListByTagName(document, tagName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

 	public static NodeList getNodeListByTagName(Document document, String tagName) {
		Element root = getRootElement(document);
		NodeList nodeList = getElementsByTagName(root, tagName);
		return nodeList;
	}

	public static void printNode(PrintWriter pw, NodeList nodeList) {
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				String line = tempNode.getNodeName() + "|" + tempNode.getTextContent();
				if (line.length() > 0) {
					if (pw != null) {
						pw.println(tempNode.getNodeName() + "|" + tempNode.getTextContent());
					} else {
						System.out.println(tempNode.getNodeName() + "|" + tempNode.getTextContent());
					}
				}
				if (tempNode.hasAttributes()) {
					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node node = nodeMap.item(i);
						line = node.getNodeName() + "|" + node.getNodeValue();
						if (line.length() > 0) {
							if (pw != null) {
								pw.println(line);
							} else {
								System.out.println(line);
							}
						}
					}
				}
				if (tempNode.hasChildNodes()) {
					// loop again if has child nodes
					printNode(pw, tempNode.getChildNodes());
				}
			}
		}
	}

	public static void printNode(NodeList nodeList) {
		printNode(null, nodeList);
	}

	public static InputStream readXmlFileIntoInputStream(String fileName) {
		String currentDir = System.getProperty("user.dir");
		fileName = currentDir + File.separator + fileName;
		System.out.println(fileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(fileName));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fis;
	}

	public static Vector<Node> searchNode(NodeList nodeList, String nodeName) {
		if (nodeList.getLength() == 0) return null;
        Vector<Node> nodes = new Vector();
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeName().compareTo(nodeName) == 0) {
				nodes.add(tempNode);
			} else {
				Vector<Node> node_vec = searchNode(tempNode.getChildNodes(), nodeName);
				if (node_vec != null) {
					nodes.addAll(node_vec);
				}
			}
		}
		return nodes;
	}

    public static void searchNodes(String xmlfile, Vector nodeNames) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try (
			InputStream is = readXmlFileIntoInputStream(xmlfile)) {
			// parse XML file
			DocumentBuilder db = dbf.newDocumentBuilder();
			// read from a project's resources folder
			Document doc = db.parse(is);
			for (int i=0; i<nodeNames.size(); i++) {
				String nodeName = (String) nodeNames.elementAt(i);
				Vector<Node> node_vec = searchNode(doc.getChildNodes(), nodeName);
				if (node_vec != null) {
					for (int j=0; j<node_vec.size(); j++) {
						Node node = (Node) node_vec.elementAt(j);
					    System.out.println(nodeName + ": " + node.getTextContent());
					}
				} else {
					System.out.println(nodeName + ": NOT FOUND");
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

}