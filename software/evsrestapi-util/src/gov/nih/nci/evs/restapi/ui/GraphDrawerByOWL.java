package gov.nih.nci.evs.restapi.ui;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.*;

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
public class GraphDrawerByOWL {

    public static int NODES_ONLY = 1;
    public static int EDGES_ONLY = 2;
    public static int NODES_AND_EDGES = 3;

    public static String ROOT = "<ROOT>";
    public static String PART_OF = "part_of";

	static String HIER_FILE = "parent_child.txt";
	static String ROLE_FILE = "roles.txt";
	static String PROPERTY_FILE = "properties.txt";
	static String OBJECT_PROPERTY_FILE = "objectProperies.txt";
	static String SEMANTIC_TYPE_FILE = "P106.txt";
	static String NCIT_OWL_FILE = "ThesaurusInferred_forTS.owl";
	//static String AXIOM_FILE = "axiom_ThesaurusInferred_forTS.txt";
	static String VS_FILE = "A8.txt";

    static String REQUIRED_DATA_FILE = ConfigurationController.requiredDataFile;
	static Vector req_data_vec = null;
    static String DATA_INFO_FILE = "data_map.txt";
    static HashMap data_info_hashmap = null;
    static HashMap dataMap = null;
    static HashMap propertyMap = null;
    static HashMap synonymMap = null;
    static OWLScanner scanner = null;
    public static Vector roots = null;

    Vector properties = null;
    Vector associations = null;
    //    public Vector extractAssociations(Vector class_vec) {


    HierarchyHelper hh = null;
    HashMap roleMap = null;
    HashMap inverseRoleMap = null;

    HashMap associationMap = null;
    HashMap inverseAssociationMap = null;

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile;

	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";
	static String RESTRICTION_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.rolefile; //"roles.txt";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;
	static String SUBSET_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.subsetfile;


    public static final String[] ALL_RELATIONSHIP_TYPES = {"type_superconcept",
                                                           "type_subconcept",
                                                           "type_role",
                                                           "type_inverse_role",
                                                           "type_association",
                                                           "type_inverse_association"};


	HashMap propertyCode2NameHashMap = new HashMap();
	HashMap propertyName2CodeHashMap = new HashMap();

	HashMap roleCode2NameHashMap = new HashMap();
	HashMap roleName2CodeHashMap = new HashMap();

	//static OWLScanner scanner = null;
	//HashMap propertyMap = null;

    public GraphDrawerByOWL() {
		initialize();
	}

	public void initialize() {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
        scanner = new OWLScanner(NCIT_OWL);
        properties = scanner.extractAnnotationProperties(scanner.get_owl_vec());

 		propertyCode2NameHashMap = new HashMap();
		propertyName2CodeHashMap = new HashMap();
        for (int i=0; i<properties.size(); i++) {
			String line = (String) properties.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String property_name = (String) u.elementAt(1);
			String property_code = (String) u.elementAt(0);
			propertyCode2NameHashMap.put(property_code, property_name);
			propertyName2CodeHashMap.put(property_name, property_code);
		}

        System.out.println("getObjectProperties ...");
		Vector supported_roles = scanner.extractObjectProperties(scanner.get_owl_vec());//getObjectProperties(named_graph);
		roleCode2NameHashMap = new HashMap();
		roleName2CodeHashMap = new HashMap();
        for (int i=0; i<supported_roles.size(); i++) {
			String line = (String) supported_roles.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String role_name = (String) u.elementAt(1);
			String role_code = (String) u.elementAt(0);
			roleCode2NameHashMap.put(role_code, role_name);
			roleName2CodeHashMap.put(role_name, role_code);
		}

		//=========================
        roleMap = new HashMap();
        inverseRoleMap = new HashMap();
        Vector v = Utils.readFile(RESTRICTION_FILE);

        System.out.println("NUMBER OF ROLES: " + v.size());

        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);

			Vector w = new Vector();
			if (roleMap.containsKey(src)) {
				w = (Vector) roleMap.get(src);
			}
			String roleName = (String) roleCode2NameHashMap.get(code);
			w.add(roleName + "|" + hh.getLabel(target) + "|" + target);

			roleMap.put(src, w);

			w = new Vector();
			if (inverseRoleMap.containsKey(target)) {
				w = (Vector) inverseRoleMap.get(target);
			}
			w.add(roleName + "|" + hh.getLabel(src) + "|" + src);
			inverseRoleMap.put(target, w);
		}

		v = scanner.extractAssociations(scanner.get_owl_vec());
		associationMap = new HashMap();
		inverseAssociationMap = new HashMap();

        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String src = (String) u.elementAt(0);
			String code = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);

			Vector w = new Vector();
			if (associationMap.containsKey(src)) {
				w = (Vector) associationMap.get(src);
			}
			String assoName = (String) propertyCode2NameHashMap.get(code);
			w.add(assoName + "|" + hh.getLabel(target) + "|" + target);
			associationMap.put(src, w);

			w = new Vector();
			if (inverseAssociationMap.containsKey(target)) {
				w = (Vector) inverseAssociationMap.get(target);
			}
			w.add(assoName + "|" + hh.getLabel(src) + "|" + src);
			inverseAssociationMap.put(target, w);
		}
	}


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getLabel(String code) {
		String name = hh.getLabel(code);
		StringBuffer buf = new StringBuffer();
		buf.append(name + " (" + code + ")");
		return buf.toString();
	}

    public String getLabel(String name, String code) {
		StringBuffer buf = new StringBuffer();
		buf.append(name + " (" + code + ")");
		return buf.toString();
	}

    public String getFieldValue(String line, int index) {
        Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line);
        return (String) u.elementAt(index);
	}

	public String encode(String t) {
		if (t == null) return null;
		t = t.replaceAll("'", "\'");
		return t;
	}

    public String getEntityDescriptionByCode(String code) {
		return hh.getLabel(code);
	}



    public String generateDiGraph(String scheme, String version, String namespace, String code) {
		boolean useNamespace = false;
		if (namespace != null) useNamespace = true;
		String name = "<NO DESCRIPTION>";
		String retstr = getEntityDescriptionByCode(code);
		if (retstr != null) {
			name = retstr;
		}
		name = encode(name);
		if (gov.nih.nci.evs.restapi.util.StringUtils.isNullOrBlank(namespace)) {
			namespace = "";
		}
		StringBuffer buf = new StringBuffer();
        buf.append("\ndigraph {").append("\n");
        buf.append("node [shape=oval fontsize=16]").append("\n");
        buf.append("edge [length=100, color=gray, fontcolor=black]").append("\n");

        String focused_node_label = "\"" + getLabel(name, code) + "\"" ;

        //RelationshipHelper relUtils = new RelationshipHelper(sparql_service);
        HashMap relMap = getRelationshipHashMap(code);

		String key = "type_superconcept";
		ArrayList list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label = "\"" + getLabel(t) + "\"" ; //getLabel(t);
				String rel_label = "is_a";
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_subconcept";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label = "\"" + getLabel(t) + "\"" ; //getLabel(t);
				String rel_label = "inverse_is_a";
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_role";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_inverse_role";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(rel_node_label + " -> " + focused_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_association";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(focused_node_label + " -> " + rel_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

		key = "type_inverse_association";
		list = (ArrayList) relMap.get(key);
		if (list != null) {
			for (int i=0; i<list.size(); i++) {
				String t = (String) list.get(i);
				String rel_node_label =  "\"" + getLabel(getFieldValue(t, 1), getFieldValue(t, 2)) + "\"";
				String rel_label = getFieldValue(t, 0);
				buf.append(rel_node_label + " -> " + focused_node_label).append("\n");
				buf.append("[label=" + rel_label + "];").append("\n");
			}
		}

        buf.append(focused_node_label + " [").append("\n");
        buf.append("fontcolor=white,").append("\n");
        buf.append("color=red,").append("\n");
        buf.append("]").append("\n");
        buf.append("}").append("\n");
        return buf.toString();
	}


    public Vector generateGraphScriptVector(String code, String[] types, int option, HashMap hmap) {
        if (types == null) {
			types = ALL_RELATIONSHIP_TYPES;
		}
        Vector graphData = generateGraphData(code, types, option, hmap);
        return GraphUtils.generateGraphScriptVector(graphData, option);
	}

	public Vector treeItem2GraphData(TreeItem root) {
	    Vector graphData = treeItem2GraphData(root, new Vector());
	    return graphData;
    }

	public Vector treeItem2GraphData(TreeItem ti, Vector v) {
		String focused_node_label = getLabel(ti._text, ti._code);
		for (String association : ti._assocToChildMap.keySet()) {
			List<TreeItem> children = ti._assocToChildMap.get(association);
			for (TreeItem childItem : children) {
				String code = childItem._code;
				String text = childItem._text;
				String rel_node_label = getLabel(text, code);
				v.add(focused_node_label + "|" + rel_node_label + "|" + association + "|7");
				v = treeItem2GraphData(childItem, v);
			}
		}
	    return v;
    }

    public Vector generateGraphData(String code, String[] types, int option, HashMap hmap) {
		Vector graphData = new Vector();
		List typeList = null;
		if (types != null) {
			typeList = Arrays.asList(types);
		} else {
			typeList = new ArrayList();
			typeList.add("type_superconcept");
			typeList.add("type_subconcept");
    	}

		String name = "<NO DESCRIPTION>";
		String retstr = getEntityDescriptionByCode(code);
		if (retstr != null) {
			name = retstr;
		}
		name = encode(name);
        String focused_node_label = getLabel(name, code);

        HashMap relMap = null;
        if (hmap == null) {
			//RelationshipHelper relUtils = new RelationshipHelper(sparql_service);
			relMap = getRelationshipHashMap(code);
	    } else {
			relMap = hmap;
		}

        HashSet nodes = new HashSet();
        nodes.add(focused_node_label);

        ArrayList list = null;

		String key = null;

		key = "type_superconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_subconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_inverse_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		key = "type_inverse_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					if (!nodes.contains(rel_node_label)) {
						nodes.add(rel_node_label);
					}
				}
			}
	    }

		Vector node_label_vec = new Vector();
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			String node_label = (String) it.next();
			node_label_vec.add(node_label);
		}

		key = "type_superconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					System.out.println("rel_node_label: " + rel_node_label);
					String rel_label = "is_a";
					if (focused_node_label.compareTo(rel_node_label) != 0) {
						graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|1");
						//System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|1");
				    }
				}
			}
	    }

		key = "type_subconcept";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(t);
					String rel_label = "is_a";
					if (focused_node_label.compareTo(rel_node_label) != 0) {
						graphData.add(rel_node_label + "|" + focused_node_label + "|" + rel_label + "|2");
						System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|2");
				    }
				}
			}
	    }

		key = "type_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|3");
					System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|3");
				}
			}
	    }

		key = "type_inverse_role";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(rel_node_label + "|" + focused_node_label + "|" +rel_label + "|4");
					System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|4");
				}
			}
		}

		key = "type_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|5");
					System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|5");
				}
			}
	    }

		key = "type_inverse_association";
		if (typeList.contains(key)) {
			list = (ArrayList) relMap.get(key);
			if (list != null) {
				for (int i=0; i<list.size(); i++) {
					String t = (String) list.get(i);
					String rel_node_label = getLabel(getFieldValue(t, 1), getFieldValue(t, 2));
					String rel_label = getFieldValue(t, 0);
					graphData.add(rel_node_label + "|" + focused_node_label + "|" +rel_label + "|6");
					System.out.println(focused_node_label + "|" + rel_node_label + "|" + rel_label + "|6");
				}
			}
		}
        return graphData;
	}

    public String generateGraphScript(String code, String[] types, int option, HashMap hmap) {
        if (types == null) {
			types = ALL_RELATIONSHIP_TYPES;
		}
        Vector graphData = generateGraphData(code, types, option, hmap);
        return GraphUtils.generateGraphScript(graphData, option);
	}

    public String generateGraphScript(String code, int option) {
        Vector graphData = generateGraphData(code, ALL_RELATIONSHIP_TYPES, option, null);
        return GraphUtils.generateGraphScript(graphData, option);
	}

    public String findCodeInGraph(String nodes_and_edges, String id) {
		String target = "{id: " + id + ", label:";
		int n = nodes_and_edges.indexOf(target);
		if (n == -1) return null;
		String t = nodes_and_edges.substring(n+target.length(), nodes_and_edges.length());
		target = ")'}";
		n = t.indexOf(target);
		t = t.substring(0, n);
		n = t.lastIndexOf("(");
		t = t.substring(n+1, t.length());
		return t;
	}

	public int countEdges(HashMap relMap, String[] types) {
		if (relMap == null || types == null) return 0;
		int knt = 0;
		List typeList = Arrays.asList(types);
		for (int k=0; k<VisualizationUtils.ALL_RELATIONSHIP_TYPES.length; k++) {
			String rel_type = (String) VisualizationUtils.ALL_RELATIONSHIP_TYPES[k];
			if (typeList.contains(rel_type)) {
				List list = (ArrayList) relMap.get(rel_type);
				if (list != null) {
					knt = knt + list.size();
				}
			}
		}
        return knt;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void view_graph(PrintWriter out, String code, String type) {
		HttpServletRequest request = null;
		HttpServletResponse response = null;
        view_graph(out, request, response, code, type);
	}

    public void view_graph(PrintWriter out, HttpServletRequest request, HttpServletResponse response,
        String code, String type) {
		String applicationName = "sparql";
        view_graph(out, request, response, applicationName, code, type);

	}


    public void view_graph(PrintWriter out, HttpServletRequest request, HttpServletResponse response, String applicationName, String code, String type) {
       	HashMap hmap = null;
       	if (out == null && response != null && request != null) {
			response.setContentType("text/html");
			hmap = (HashMap) request.getSession().getAttribute("RelationshipHashMap");
			if (hmap == null) {
				hmap = getRelationshipHashMap(code);
			}

			try {
			     out = response.getWriter();
			} catch (Exception ex) {
			     ex.printStackTrace();
			     return;
			}
		} else {
			hmap = getRelationshipHashMap(code);
		}

		// compute nodes and edges using hmap
		String[] types = null;
		if (type == null || type.compareTo("ALL") == 0) {
		    types = ALL_RELATIONSHIP_TYPES;
		} else {
		    types = new String[1];
		    types[0] = type;
		}

		int edge_count = countEdges(hmap, types);
		Vector v = null;
		try {
			v = generateGraphScriptVector(code, types, gov.nih.nci.evs.restapi.ui.VisUtils.NODES_AND_EDGES, hmap);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		String nodes_and_edges = null;
		String group_node_data = "";
		String group_node_id = null;
		String group_node_data_2 = "";
		String group_node_id_2 = null;
		boolean direction = true;
		HashMap group_node_id2dataMap = new HashMap();

		gov.nih.nci.evs.restapi.ui.GraphReductionUtils graphReductionUtils = new gov.nih.nci.evs.restapi.ui.GraphReductionUtils();
		int graph_size = graphReductionUtils.getNodeCount(v);

		//KLO, 02122016
		graphReductionUtils.initialize_group_node_id(graph_size);
		if (graph_size > graphReductionUtils.MINIMUM_REDUCED_GRAPH_SIZE) {

		  group_node_id = graphReductionUtils.getGroupNodeId(v);
		  int group_node_id_int = Integer.parseInt(group_node_id);
		  group_node_id_2 = Integer.valueOf(group_node_id_int+1).toString();
		  Vector w = graphReductionUtils.reduce_graph(v, direction);

		  boolean graph_reduced = graphReductionUtils.graph_reduced(v, w);
		  if (graph_reduced) {
			  group_node_data = graphReductionUtils.get_removed_node_str(v, direction);
			  Vector group_node_ids = graphReductionUtils.get_group_node_ids(w);
			  for (int k=0; k<group_node_ids.size(); k++) {
				  String node_id = (String) group_node_ids.elementAt(k);
				  if (!group_node_id2dataMap.containsKey(node_id)) {
					  group_node_id2dataMap.put(node_id, group_node_data);
					  break;
				  }
			  }

			  nodes_and_edges = GraphUtils.generateGraphScript(w);
			  v = (Vector) w.clone();
		  }


		  direction = false;
		  w = graphReductionUtils.reduce_graph(v, direction);
		  graph_reduced = graphReductionUtils.graph_reduced(v, w);
		  if (graph_reduced) {
			  group_node_data_2 = graphReductionUtils.get_removed_node_str(v, direction);
			  Vector group_node_ids = graphReductionUtils.get_group_node_ids(w);
			  for (int k=0; k<group_node_ids.size(); k++) {
				  String node_id = (String) group_node_ids.elementAt(k);
				  if (!group_node_id2dataMap.containsKey(node_id)) {
					  group_node_id2dataMap.put(node_id, group_node_data_2);
					  break;
				  }
			  }
			  nodes_and_edges =  GraphUtils.generateGraphScript(w);
			  v = (Vector) w.clone();
		  }
		}

		if (group_node_id2dataMap.keySet().size() == 0) {
		    nodes_and_edges = generateGraphScript(code, types, NODES_AND_EDGES, hmap);
		}

		Vector group_node_ids = graphReductionUtils.get_group_node_ids(v);
		boolean graph_available = true;
		if (nodes_and_edges.compareTo(GraphUtils.NO_DATA_AVAILABLE) == 0) {
		    graph_available = false;
		}

		out.println("<!doctype html>");
		out.println("<html>");
		out.println("<head>");
		out.println("  <title>View Graph</title>");
		out.println("");
		out.println("  <style type=\"text/css\">");
		out.println("    body {");
		out.println("      font: 10pt sans;");
		out.println("    }");
		out.println("    #conceptnetwork {");
		out.println("      width: 1200px;");
		if (edge_count > 50) {
		  out.println("      height: 800px;");
		} else {
		  out.println("      height: 600px;");
		}
		out.println("      border: 1px solid lightgray;");
		out.println("    }");
		out.println("    table.legend_table {");
		out.println("      border-collapse: collapse;");
		out.println("    }");
		out.println("    table.legend_table td,");
		out.println("    table.legend_table th {");
		out.println("      border: 1px solid #d3d3d3;");
		out.println("      padding: 10px;");
		out.println("    }");
		out.println("");
		out.println("    table.legend_table td {");
		out.println("      text-align: center;");
		out.println("      width:110px;");
		out.println("    }");
		out.println("  </style>");
		out.println("");
		out.println("  <script type=\"text/javascript\" src=\"/" + applicationName + "/css/vis/vis.js\"></script>");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/" + applicationName + "/css/vis/vis.css\" />");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"/" + applicationName + "/css/styleSheet.css\" />");

		out.println("");
		out.println("  <script type=\"text/javascript\">");
		out.println("    var nodes = null;");
		out.println("    var edges = null;");
		out.println("    var network = null;");
		out.println("");

		out.println("    function reset_graph(id) {");
		out.println("        window.location.href=\"/" + applicationName + "/ajax?action=reset_graph&id=\" + id;");
		out.println("    }");


		out.println("    function destroy() {");
		out.println("      if (network !== null) {");
		out.println("        network.destroy();");
		out.println("        network = null;");
		out.println("      }");
		out.println("    }");
		out.println("");

		out.println("    function draw() {");

		if (graph_available) {
		  out.println(nodes_and_edges);
		}

		out.println("      // create a network");
		out.println("      var container = document.getElementById('conceptnetwork');");
		out.println("      var data = {");
		out.println("        nodes: nodes,");
		out.println("        edges: edges");
		out.println("      };");

		if (type.endsWith("path")) {
		  out.println("            var directionInput = document.getElementById(\"direction\").value;");
		  out.println("            var options = {");
		  out.println("                layout: {");
		  out.println("                    hierarchical: {");
		  out.println("                        direction: directionInput");
		  out.println("                    }");
		  out.println("                }");
		  out.println("            };");


		} else {

		  out.println("      var options = {");
		  out.println("        interaction: {");
		  out.println("          navigationButtons: true,");
		  out.println("          keyboard: true");
		  out.println("        }");
		  out.println("      };");

		}

		out.println("      network = new vis.Network(container, data, options);");
		out.println("");
		out.println("      // add event listeners");


		out.println("      network.on('select', function(params) {");

		Iterator it = group_node_id2dataMap.keySet().iterator();
		while (it.hasNext()) {
		  String node_id = (String) it.next();
		  String node_data = (String) group_node_id2dataMap.get(node_id);
		  out.println("      if (params.nodes == '" + node_id + "') {");
		  out.println("         document.getElementById('selection').innerHTML = '" + node_data + "';");
		  out.println("      }");
		}

		out.println("      });");
		out.println("			network.on(\"doubleClick\", function (params) {");

		String node_id_1 = null;
		String node_id_2 = null;
		it = group_node_id2dataMap.keySet().iterator();
		int lcv = 0;
		while (it.hasNext()) {
		  String node_id = (String) it.next();
		  if (lcv == 0) {
			  node_id_1 = node_id;
		  } else if (lcv == 1) {
			  node_id_2 = node_id;
		  }
		  lcv++;
		}

		if (node_id_1 != null && node_id_2 != null) {
		  out.println("      if (params.nodes != '" + node_id_1 + "' && params.nodes != '" + node_id_2 + "') {");
		  out.println("				params.event = \"[original event]\";");
		  out.println("				var json = JSON.stringify(params, null, 4);");
		  out.println("				reset_graph(params.nodes);");
		  out.println("      }");
		} else if (node_id_1 != null && node_id_2 == null) {
		  out.println("      if (params.nodes != '" + node_id_1 + "') {");
		  out.println("				params.event = \"[original event]\";");
		  out.println("				var json = JSON.stringify(params, null, 4);");
		  out.println("				reset_graph(params.nodes);");
		  out.println("      }");
		} else if (node_id_2 != null && node_id_1 == null) {
		  out.println("      if (params.nodes != '" + node_id_2 + "') {");
		  out.println("				params.event = \"[original event]\";");
		  out.println("				var json = JSON.stringify(params, null, 4);");
		  out.println("				reset_graph(params.nodes);");
		  out.println("      }");
		} else if (node_id_2 == null && node_id_1 == null) {
		  out.println("				params.event = \"[original event]\";");
		  out.println("				var json = JSON.stringify(params, null, 4);");
		  out.println("				reset_graph(params.nodes);");
		}

		out.println("		    });");

		out.println("    }");
		out.println("  </script>");
		out.println("</head>");
		out.println("");
		out.println("<body onload=\"draw();\">");

		out.println("<div class=\"ncibanner\">");
		out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">     ");
		out.println("    <img src=\"/" + applicationName + "/images/logotype.gif\"");
		out.println("      width=\"556\" height=\"39\" border=\"0\"");
		out.println("      alt=\"National Cancer Institute\"/>");
		out.println("  </a>");
		out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">     ");
		out.println("    <img src=\"/" + applicationName + "/images/spacer.gif\"");
		out.println("      width=\"60\" height=\"39\" border=\"0\" ");
		out.println("      alt=\"National Cancer Institute\" class=\"print-header\"/>");
		out.println("  </a>");
		out.println("  <a href=\"http://www.nih.gov\" target=\"_blank\" >      ");
		out.println("    <img src=\"/" + applicationName + "/images/tagline_nologo.gif\"");
		out.println("      width=\"219\" height=\"39\" border=\"0\"");
		out.println("      alt=\"U.S. National Institutes of Health\"/>");
		out.println("  </a>");
		out.println("  <a href=\"http://www.cancer.gov\" target=\"_blank\">      ");
		out.println("    <img src=\"/" + applicationName + "/images/cancer-gov.gif\"");
		out.println("      width=\"125\" height=\"39\" border=\"0\"");
		out.println("      alt=\"www.cancer.gov\"/>");
		out.println("  </a>");
		out.println("</div>");
		out.println("<p></p>");

		if (!graph_available) {
		  out.println("<p class=\"textbodyred\">&nbsp;No graph data is available.</p>");
		}

		out.println("<form id=\"data\" method=\"post\" action=\"/" + applicationName + "/ajax?action=view_graph\">");

		out.println("Relationships");
		out.println("<select name=\"type\" >");
		if (type == null || type.compareTo("ALL") == 0) {
		  out.println("  <option value=\"ALL\" selected>ALL</option>");
		} else {
		  out.println("  <option value=\"ALL\">ALL</option>");
		}
		String rel_type = null;
		String option_label = null;

		for (int k=0; k<VisUtils.ALL_RELATIONSHIP_TYPES.length; k++) {
		  rel_type = (String) VisUtils.ALL_RELATIONSHIP_TYPES[k];
		  List list = (List) hmap.get(rel_type);
		  if (list != null && list.size() > 0) {
			  option_label = VisUtils.getRelatinshipLabel(rel_type);
			  if (type.compareTo(rel_type) == 0) {
				  out.println("  <option value=\"" + rel_type + "\" selected>" + option_label + "</option>");
			  } else {
				  out.println("  <option value=\"" + rel_type + "\">" + option_label + "</option>");
			  }
		  }
		}

		out.println("</select>");
		/*
		out.println("<input type=\"hidden\" id=\"scheme\" name=\"scheme\" value=\"" + scheme + "\" />");
		out.println("<input type=\"hidden\" id=\"version\" name=\"version\" value=\"" + version + "\" />");
		out.println("<input type=\"hidden\" id=\"ns\" name=\"ns\" value=\"" + namespace + "\" />");
		*/
		out.println("<input type=\"hidden\" id=\"code\" name=\"code\" value=\"" + code + "\" />");
		out.println("");
		out.println("&nbsp;&nbsp;");
		out.println("<input type=\"submit\" value=\"Refresh\"></input>");
		out.println("</form>");
		out.println("");

		if (type.endsWith("path")) {

		out.println("<p>");
		out.println("    <input type=\"button\" id=\"btn-UD\" value=\"Up-Down\">");
		out.println("    <input type=\"button\" id=\"btn-DU\" value=\"Down-Up\">");
		out.println("    <input type=\"button\" id=\"btn-LR\" value=\"Left-Right\">");
		out.println("    <input type=\"button\" id=\"btn-RL\" value=\"Right-Left\">");
		out.println("    <input type=\"hidden\" id='direction' value=\"UD\">");
		out.println("</p>");
		out.println("<script language=\"javascript\">");
		out.println("    var directionInput = document.getElementById(\"direction\");");
		out.println("    var btnUD = document.getElementById(\"btn-UD\");");
		out.println("    btnUD.onclick = function () {");
		out.println("        directionInput.value = \"UD\";");
		out.println("        draw();");
		out.println("    }");
		out.println("    var btnDU = document.getElementById(\"btn-DU\");");
		out.println("    btnDU.onclick = function () {");
		out.println("        directionInput.value = \"DU\";");
		out.println("        draw();");
		out.println("    };");
		out.println("    var btnLR = document.getElementById(\"btn-LR\");");
		out.println("    btnLR.onclick = function () {");
		out.println("        directionInput.value = \"LR\";");
		out.println("        draw();");
		out.println("    };");
		out.println("    var btnRL = document.getElementById(\"btn-RL\");");
		out.println("    btnRL.onclick = function () {");
		out.println("        directionInput.value = \"RL\";");
		out.println("        draw();");
		out.println("    };");
		out.println("</script>");
		}

		out.println("<div style=\"width: 800px; font-size:14px; text-align: justify;\">");
		out.println("</div>");
		out.println("");
		out.println("<div id=\"conceptnetwork\"></div>");
		out.println("");
		out.println("<p id=\"selection\"></p>");
		out.println("</body>");
		out.println("</html>");

		out.flush();

		if (response != null) {
			request.getSession().setAttribute("nodes_and_edges", nodes_and_edges);
			request.getSession().setAttribute("RelationshipHashMap", hmap);
		}

		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static String endPoint2ServiceUrl(String sparql_endpoint) {
		int n = sparql_endpoint.indexOf("?");
		if (n == -1) return sparql_endpoint;
		return sparql_endpoint.substring(0, n);
	}

    public static List vector2List(Vector v) {
		if (v == null) return null;
		ArrayList list = new ArrayList();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			list.add(t);
		}
		return list;
	}


    public HashMap getRelationshipHashMap(String code) {
		HashMap hmap = new HashMap();
		Vector v = hh.getSuperclassCodes(code);
		hmap.put(Constants.TYPE_SUPERCONCEPT, vector2List(v));
		v = hh.getSubclassCodes(code);
		hmap.put(Constants.TYPE_SUBCONCEPT, vector2List(v));

		v = (Vector) roleMap.get(code);
        if (v != null) {
			hmap.put(Constants.TYPE_ROLE, vector2List(v));
		}

		v = (Vector) inverseRoleMap.get(code);
        if (v != null) {
			hmap.put(Constants.TYPE_INVERSE_ROLE, vector2List(v));
		}

		v = (Vector) associationMap.get(code);
        if (v != null) {
			hmap.put(Constants.TYPE_ASSOCIATION, vector2List(v));
		}

		v = (Vector) inverseAssociationMap.get(code);
        if (v != null) {
			hmap.put(Constants.TYPE_INVERSE_ASSOCIATION, vector2List(v));
		}
		return hmap;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void globalReplacement(String filename, String replace, String by, String outputfile) {
		Vector w = new Vector();
		Vector v = Utils.readFile(filename);
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.replace(replace, by);
			w.add(line);
		}
		Utils.saveToFile(outputfile, w);
	}

	public static void run(String code, int idx) {
		GraphDrawerByOWL gd = new GraphDrawerByOWL();
		PrintWriter pw = null;

		String type = "";
		if (idx == 0) {
			type = "type_superconcept";
		} else if (idx == 1) {
			type = "type_subconcept";
		} else if (idx == 2) {
			type = "type_role";
		} else if (idx == 3) {
			type = "type_inverse_role";
		} else if (idx == 4) {
			type = "type_association";
		} else if (idx == 5) {
			type = "type_inverse_association";
		}

		String outputfile = code + "_" + idx + "_graph.html";
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
        	gd.view_graph(pw, code, type);

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();

        String code = args[0];
        String idx_str = args[1];
        int idx = Integer.parseInt(idx_str);

		GraphDrawerByOWL gd = new GraphDrawerByOWL();
		gd.run(code, idx);
		String outputfile = code + "_" + idx_str + "_graph.html";
		globalReplacement(outputfile, "/sparql/", "sparql/", outputfile);

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

