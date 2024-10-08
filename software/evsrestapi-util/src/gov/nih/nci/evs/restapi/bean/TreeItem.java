package gov.nih.nci.evs.restapi.bean;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.util.*;
import java.io.*;
import java.util.*;

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
public class TreeItem implements Serializable, Comparable<TreeItem> {
    public String _code = null;
    public String _text = null;

    public String _ns = null;//"na";
    public String _id = null;  // tree_node_id

    public String _auis = null;
    public boolean _expandable = false;

    public static String ROOT = "Root";
    public static String ROOT_CODE = "<Root>";

    public Map<String, List<TreeItem>> _assocToChildMap =
        new TreeMap<String, List<TreeItem>>();

    public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof TreeItem)) return false;
		TreeItem item = (TreeItem) o;
		if (_ns == null) {
			return _text.compareTo(item._text) == 0 && _code.compareTo(item._code) == 0;
		} else {
			if (item._ns == null) return false;
			return _text.compareTo(item._text) == 0 && _code.compareTo(item._code) == 0 && _ns.compareTo(item._ns) == 0;
		}
    }

    public int compareTo(TreeItem ti) {
        String c1 = _code;
        String c2 = ti._code;
        if (c1.startsWith("@"))
            return 1;
        if (c2.startsWith("@"))
            return -1;
        int i = c1.compareTo(c2);
        return i != 0 ? i : _text.compareTo(ti._text);
    }

    public TreeItem(String code, String text) {
        super();
        _code = code;
        _text = text;
        _auis = null;
        _ns = null;
    }

    public TreeItem(String code, String text, String auiText) {
        super();
        _code = code;
        _text = text;
        _auis = auiText;
        _ns = null;
    }

    public TreeItem(String code, String text, String ns, String auiText) {
        super();
        _code = code;
        _ns = ns;
        _text = text;
        _auis = auiText;
    }

    public TreeItem(String code, String text, String ns, String id, String auiText) {
        super();
        _code = code;
        _ns = ns;
        _text = text;
        _id = id;
        _auis = auiText;
    }

    public void setNs(String ns) {
		_ns = ns;
	}

    public void setId(String id) {
		_id = id;
	}

    public void addAll(String assocText, List<TreeItem> children) {
        for (TreeItem item : children)
            addChild(assocText, item);
    }

    public void addChild(String assocText, TreeItem child) {
        List<TreeItem> children = _assocToChildMap.get(assocText);
        if (children == null) {
            children = new ArrayList<TreeItem>();
            _assocToChildMap.put(assocText, children);
        }
        int i;
        if ((i = children.indexOf(child)) >= 0) {
            TreeItem existingTreeItem = children.get(i);
            for (String assoc : child._assocToChildMap.keySet()) {
                List<TreeItem> toAdd = child._assocToChildMap.get(assoc);
                if (!toAdd.isEmpty()) {
                    existingTreeItem.addAll(assoc, toAdd);
                    existingTreeItem._expandable = false;
                }
            }
        } else
            children.add(child);
    }

    public int hashCode() {
		int hashcode = 0;
		if (_code != null) hashcode = hashcode + _code.hashCode();
		if (_ns != null && _ns.compareTo("na") != 0) hashcode = hashcode + _ns.hashCode();
		if (_id != null) hashcode = hashcode + _id.hashCode();
		if (_text != null) hashcode = hashcode + _text.hashCode();
		if (_auis != null) hashcode = hashcode + _auis.hashCode();
		if (_expandable) {
			hashcode = hashcode + "expandable".hashCode();
		}
	    return hashcode;
	}

    public String toString() {
    	String s = _text;
    	if (_code != null && _code.length() > 0)
    		s += " (" + _code + ")";

    	if (_ns != null && _ns.length() > 0 && _ns.compareTo("na") != 0)
    		s += " (" + _ns + ")";

    	return s;
    }

    public static void printTree(TreeItem ti, int depth) {
		printTree(ti, depth, true);
	}

    public static void printTree(TreeItem ti, int depth, boolean print_code_first) {
		if (ti == null) return;

        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            indent.append("\t");
		}

        StringBuffer codeAndText = new StringBuffer();

        codeAndText.append(indent);

        if (print_code_first) {
			codeAndText.append("").append(ti._code)
					.append(':').append(ti._text).append(ti._expandable ? " [+]" : "");
		} else {
			codeAndText.append("").append(ti._text).append(" (").append(ti._code).append(")");
		}
        System.out.println(codeAndText);

        for (String association : ti._assocToChildMap.keySet()) {
            List<TreeItem> children = ti._assocToChildMap.get(association);
            new SortUtils().quickSort(children);
            for (int i=0; i<children.size(); i++) {
				TreeItem childItem = (TreeItem) children.get(i);
                printTree(childItem, depth + 1, print_code_first);
			}
        }
    }

    public static void printTree(PrintWriter pw, TreeItem ti, int depth, boolean print_code_first) {
		if (ti == null) return;

        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            indent.append("\t");
		}

        StringBuffer codeAndText = new StringBuffer();

        codeAndText.append(indent);

        if (print_code_first) {
			codeAndText.append("").append(ti._code)
					.append(':').append(ti._text).append(ti._expandable ? " [+]" : "");
		} else {
			codeAndText.append("").append(ti._text).append(" (").append(ti._code).append(")");
		}
        pw.println(codeAndText);

        for (String association : ti._assocToChildMap.keySet()) {
            List<TreeItem> children = ti._assocToChildMap.get(association);
            new SortUtils().quickSort(children);
            for (int i=0; i<children.size(); i++) {
				TreeItem childItem = (TreeItem) children.get(i);
                printTree(pw, childItem, depth + 1, print_code_first);
			}
        }
    }

    public String getAssociation(TreeItem ti) {
		Iterator it = ti._assocToChildMap.keySet().iterator();
		return (String) it.next();
	}

    public TreeItem removeSuperRootNode(TreeItem ti) {
		if (ti._code.compareTo("<Root>") != 0) return ti;
		List<TreeItem> children = ti._assocToChildMap.get(getAssociation(ti));
		return (TreeItem) children.get(0);
	}

	public static TreeItem searchTree(TreeItem ti, String text) {
		if (ti._text.compareTo(text) == 0) return ti;
		TreeItem node = null;
        for (String association : ti._assocToChildMap.keySet()) {
            List<TreeItem> children = ti._assocToChildMap.get(association);
            if (children != null) {
				for (int i=0; i<children.size(); i++) {
					TreeItem childItem = (TreeItem) children.get(i);
					node = searchTree(childItem, text);
					if (node != null) {
						return node;
					}
				}
			}
        }
        return null;
	}

	public String toXML() {
		XStream xstream_xml = new XStream(new DomDriver());
		String xml = xstream_xml.toXML(this);
		xml = StringUtils.escapeDoubleQuotes(xml);
		xml = Constants.XML_DECLARATION + "\n" + xml;
		xml = StringUtils.removePackageNames(Constants.EVSRESTAPI_BEAN, xml);
        return xml;
	}

	public String toJson() {
		//return new Gson().toJson(this);
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
	}

}
