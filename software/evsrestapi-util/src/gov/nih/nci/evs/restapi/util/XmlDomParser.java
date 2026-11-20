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
	public static String NCIT_NS = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";

    public static Document createDocument(String xmlfile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(xmlfile);
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

  	public static NodeList getNodeListByTagName(String xmlfile, String tagName) {
		try {
			Document document = createDocument(xmlfile);
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
		String line = null;
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				try {
					line = tempNode.getNodeName() + "|" + tempNode.getTextContent();
					if (pw != null) {
						pw.println(tempNode.getNodeName() + "|" + tempNode.getTextContent());
					} else {
						System.out.println(tempNode.getNodeName() + "|" + tempNode.getTextContent());
					}
			    } catch (Exception ex) {
					ex.printStackTrace();
				}

				if (tempNode.hasAttributes()) {
					// get attributes names and values
					NamedNodeMap nodeMap = tempNode.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); i++) {
						Node node = nodeMap.item(i);
						try {
							line = node.getNodeName() + "|" + node.getNodeValue();
							if (pw != null) {
								pw.println(line);
							} else {
								System.out.println(line);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				if (tempNode.hasChildNodes()) {
					printNode(pw, tempNode.getChildNodes());
				}
			}
		}
	}

	public static void printNode(NodeList nodeList) {
		printNode(null, nodeList);
	}

	public static InputStream readXmlFileIntoInputStream(String xmlfile) {
		String currentDir = System.getProperty("user.dir");
		xmlfile = currentDir + File.separator + xmlfile;
		System.out.println(xmlfile);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(xmlfile));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fis;
	}

	public static void dumpElement(Element element) {
		//printNode(PrintWriter pw, NodeList nodeList) {
		 NodeList nodeList = element.getElementsByTagName("owl:Class");
		 XmlDomParser.printNode(null, nodeList);
		 /*
         System.out.println(element.getTagName());
         String attrValue = element.getElementsByTagName("P108").item(0).getTextContent();
         System.out.println(attrValue);
         //attrValue = element.getElementsByTagName("rdfs:lable").item(0).getTextContent();
         //System.out.println(attrValue);
         attrValue = element.getElementsByTagName("P106").item(0).getTextContent();
         System.out.println(attrValue);
         */
	}



    public static String code2Id(String code) {
		return NCIT_NS + code;
	}

    public static Vector<Element> searchElements(String xmlfile, String tagName, String attrName, String attrValue) throws Exception {
        // Parse the document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlfile);
        doc.getDocumentElement().normalize();

        NodeList inputList = doc.getElementsByTagName(tagName);
        Vector<Element> elements = new Vector();
        for (int i = 0; i < inputList.getLength(); i++) {
            Element element = (Element) inputList.item(i);
            String value = element.getAttribute(attrName);
            if (attrValue.equals(value)) {
                elements.add(element);
                dumpElement(element);
            }
        }
        return elements;
	}

    public static void main(String[] args) {
		String xmlfile = args[0];
		String id = args[1];
		System.out.println("xmlfile: " + xmlfile);
		System.out.println("id: " + id);
		String tagName = "owl:Class";
		String attrName = "rdf:about";
		String code = "C10000";
		String attrValue = code2Id(code);
		System.out.println(attrValue);
		try {
        	Vector<Element> elements = searchElements(xmlfile, tagName, attrName, attrValue);
        	for (int i=0; i<elements.size(); i++) {
				Element element = (Element) elements.elementAt(i);
				NodeList nodeList = element.getElementsByTagName(tagName);
				printNode(null, nodeList);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}