package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.test.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
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
public class GraphGenerator {
	static float default_ranksep = (float) 1.0;
	static float default_nodesep = (float) 1.5;

	static Vector COLORS = new Vector();
	static {
		COLORS.add("black");
		COLORS.add("blue");
		COLORS.add("blueviolet");
		COLORS.add("brown");
		COLORS.add("brown4");
		COLORS.add("burlywood");
		COLORS.add("cadetblue4");
		COLORS.add("chocolate4");
		COLORS.add("coral3");
		COLORS.add("cyan4");
		COLORS.add("crimson");
		COLORS.add("darkgreen");
		COLORS.add("darkolivegreen");
		COLORS.add("darkgoldenrod");
		COLORS.add("darkorchid");
		COLORS.add("darklategray");
		COLORS.add("darkviolet");
		COLORS.add("deeppink3");
		COLORS.add("firebrick2");
		COLORS.add("forestgreen");
		COLORS.add("gold");
		COLORS.add("grey6");
		COLORS.add("green3");
		COLORS.add("green4");
		COLORS.add("lawngreen");
		COLORS.add("lightblue");
		COLORS.add("lightslateblue");
		COLORS.add("limegreen");
		COLORS.add("maroon");
		COLORS.add("magenta");
		COLORS.add("maroon");
		COLORS.add("midnightblue");
		COLORS.add("navyblue");
		COLORS.add("orange");
		COLORS.add("orangered");
		COLORS.add("orangered4");
		COLORS.add("orchid");
		COLORS.add("palevioletred");
		COLORS.add("pink");
		COLORS.add("purple");
		COLORS.add("red");
		COLORS.add("saddlebrown");
		COLORS.add("sienna");
		COLORS.add("slateblue");
		COLORS.add("snow");
		COLORS.add("springgreen");
		COLORS.add("tan");
		COLORS.add("turquoise3");
		COLORS.add("tomato");
		COLORS.add("violet");
		COLORS.add("wheat");
		COLORS.add("yellowgreen");
	}

    public GraphGenerator() {

	}

	public static String generateColor() {
		int n = new RandomVariateGenerator().uniform(0, COLORS.size()-1);
		return (String) COLORS.elementAt(n);
	}

	public static String toString(Vector v) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			t = t.trim();
			buf.append(t).append(" ");
		}
		String s = buf.toString();
		return s;
	}

	public static void createDotGraph(String dotFormat, String type, String dotfilename) {
	    GraphViz gv=new GraphViz();
	    gv.addln(gv.start_graph());
	    gv.add(dotFormat);
	    gv.addln(gv.end_graph());
	    gv.decreaseDpi();
	    gv.decreaseDpi();
	    File out = new File(dotfilename);
	    gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, dotfilename), out);
	}

	public static void createDotGraph(Vector data_vec, String filename, String type, String dotfilename) {
        float ranksep = default_ranksep;
        float nodesep = default_nodesep;
		String format = type;
		Vector nodes = getNodes(data_vec);
		Vector edges = getEdges(data_vec);
		Vector selected_nodes = nodes;
		generateGraphvizDataFile(nodes, edges, selected_nodes, filename, ranksep, nodesep);
		text2Dot(filename, type, dotfilename);
	}

	public static void text2Dot(String filename, String type, String dotfilename) {
		String dotFormat = toString(Utils.readFile(filename));
		createDotGraph(dotFormat, type, dotfilename);
	}

	public static void text2Dot(String filename, String type) {
		int n = filename.lastIndexOf(".");
		String dotfilename = filename.substring(0, n) + ".dot";
		text2Dot(filename, type, dotfilename);
	}

	public static Vector getNodes(Vector v) {
		int src_index = 0;
		int target_idx = 2;
		return getNodes(v, src_index, target_idx);
	}

	public static Vector getNodes(Vector data_vec, int src_index, int target_idx) {
        Vector nodes = new Vector();
        for (int i=0; i<data_vec.size(); i++) {
			String t = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String source = (String) u.elementAt(src_index);
			String target = (String) u.elementAt(target_idx);
			if (!nodes.contains(source)) {
				nodes.add(source);
			}
			if (!nodes.contains(target)) {
				nodes.add(target);
			}
		}
		nodes = new SortUtils().quickSort(nodes);
		return nodes;
    }

	public static Vector getEdgeLabels(Vector v) {
        int edge_idx = 1;
        return getEdgeLabels(v, edge_idx);
	}

	public static Vector getEdgeLabels(Vector v, int edge_idx) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String edge = (String) u.elementAt(edge_idx);
            if (!w.contains(edge)) {
				w.add(edge);
			}
		}
		w = new SortUtils().quickSort(w);
		return w;
	}

	public static Vector getEdges(Vector v) {
        int src_idx = 0;
        int edge_idx = 1;
        int target_idx = 2;
        return getEdges(v, src_idx, edge_idx, target_idx);
	}

	public static Vector getEdges(Vector data_vec, int src_idx, int edge_idx, int target_idx) {
        Vector edges = new Vector();
        for (int i=0; i<data_vec.size(); i++) {
			String t = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String source = (String) u.elementAt(src_idx);
			String edge = (String) u.elementAt(edge_idx);
			String target = (String) u.elementAt(target_idx);
			String link = edge + "|" + source + "|" + target;
			if (!edges.contains(link)) {
				edges.add(link);
			}
		}
		return edges;
	}

    public static void generateGraph(PrintWriter pw, Vector nodes, Vector edges, Vector selected_nodes) {
		HashMap label2IdMap = new HashMap();
		HashMap id2LabelMap = new HashMap();
		HashMap id2ColorMap = new HashMap();
		Vector w = nodes;

		if (selected_nodes != null) {
			w = selected_nodes;
		}

		int knt = 0;
		for (int i=0; i<w.size(); i++) {
			knt++;
			String label = (String) w.elementAt(i);
			String id = "node_" + knt;
			label2IdMap.put(label, id);
			id2LabelMap.put(id, label);
			String color = generateColor();

			if (nodes == null) {
				pw.println(id + " [label=\"" + label + "\", fontcolor=white, shape=box, style=filled, fillcolor=" + color + "];");
			} else {
				if (nodes.contains(label)) {
					pw.println(id + " [label=\"" + label + "\", fontcolor=white, shape=box, style=filled, fillcolor=" + color + "];");
				}
			}
			id2ColorMap.put(id, color);
		}

		w = edges;
		HashMap nodePair2EdgeMap = new HashMap();
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String role = (String) u.elementAt(0);
			String domain = (String) u.elementAt(1);
			String range = (String) u.elementAt(2);
			String domain_id = (String) label2IdMap.get(domain);
			String range_id = (String) label2IdMap.get(range);
			//String color = (String) id2ColorMap.get(domain_id);
			String key = domain_id + "|" + range_id;
			String roles = (String) nodePair2EdgeMap.get(key);
			if (roles == null) {
				roles = role;
			} else {
				roles = roles + "\\n" + role;
			}
			nodePair2EdgeMap.put(key, roles);
		}

		Iterator it = nodePair2EdgeMap.keySet().iterator();
		while (it.hasNext()) {
			String nodePair = (String) it.next();
			Vector u = StringUtils.parseData(nodePair);
			String domain_id = (String) u.elementAt(0);
			String range_id = (String) u.elementAt(1);
			String color = (String) id2ColorMap.get(domain_id);
			String role = (String) nodePair2EdgeMap.get(nodePair);

			String domain_label = (String) id2LabelMap.get(domain_id);
			String range_label = (String) id2LabelMap.get(range_id);
			if (selected_nodes == null) {
				pw.println(domain_id + " -> " + range_id
				   + " [label=" + "\"" + role + "\"" + " fontcolor=" + color + ", color=" + color + "];");
			} else {
				if (selected_nodes.contains(domain_label) && selected_nodes.contains(range_label)) {
					pw.println(domain_id + " -> " + range_id
					   + " [label=" + "\"" + role + "\"" + " fontcolor=" + color + ", color=" + color + "];");
				}
			}
		}
    }


	public static void generateGraphvizDataFile(Vector nodes, Vector edges, Vector selected_nodes, String outputfile) {
        float ranksep = (float) 1.0;
        float nodesep = (float) 1.5;
        generateGraphvizDataFile(nodes, edges, selected_nodes, outputfile, ranksep, nodesep);
	}

	public static void generateGraphvizDataFile(Vector nodes, Vector edges, Vector selected_nodes, String outputfile, float ranksep, float nodesep) {
         PrintWriter pw = null;
         try {
 			pw = new PrintWriter(outputfile, "UTF-8");
			pw.println("ranksep = \"" + ranksep + "\";");
			pw.println("nodesep = \"" + nodesep + "\";");
			pw.println("ratio=fill;");
			pw.println("overlap=scale;");
			pw.println("concentrate=true;");
			pw.println("ratio=auto;");
			pw.println("overlap = false;");
			pw.println("splines = true;");
			pw.println("node [style=filled,color=lightblue,shape=box];");
			pw.println("style=filled;");
			pw.println("color=lightgrey;");
            generateGraph(pw, nodes, edges, selected_nodes);

 		} catch (Exception ex) {

 		} finally {
 			try {
 				pw.close();
 				System.out.println("Output file " + outputfile + " generated.");
 		    } catch (Exception ex) {
 				ex.printStackTrace();
 			}
		}
	}



	public static GraphData createGraphData(String inputfile, String format) { //source|edge label|target
	    Vector data_vec = Utils.readFile(inputfile);
	    float ranksep = default_ranksep;
	    float nodesep = default_nodesep;
	    String filename = "graph_" + inputfile;
	    return createGraphData(data_vec, ranksep, nodesep, format, filename);
	}

	public static GraphData createGraphData(Vector data_vec, float ranksep, float nodesep, String format, String filename) { //source|edge label|target
		GraphData gd = new GraphData();
        Vector nodes = new Vector();
        Vector edges = new Vector();
        for (int i=0; i<data_vec.size(); i++) {
			String t = (String) data_vec.elementAt(i);
			Vector u = StringUtils.parseData(t, '|');
			String source = (String) u.elementAt(0);
			String edge = (String) u.elementAt(1);
			String target = (String) u.elementAt(2);
			if (!nodes.contains(source)) {
				nodes.add(source);
			}
			if (!nodes.contains(target)) {
				nodes.add(target);
			}
			String link = edge + "|" + source + "|" + target;
			if (!edges.contains(link)) {
				edges.add(link);
			}
		}
		Vector selectedNodes = nodes;
	    return new GraphData(
			nodes,
			edges,
			selectedNodes,
			format,
			ranksep,
			nodesep,
			filename);
	}

    public static void run(GraphData gd) {
		run(gd.getNodes(),
			gd.getEdges(),
			gd.getSelectedNodes(),
			gd.getFilename(),
			gd.getFormat(),
			gd.getRanksep(),
			gd.getNodesep());
	}

    public static void run(Vector nodes, Vector edges, Vector selected_nodes, String outputfile, String format, float ranksep, float nodesep) {
		generateGraphvizDataFile(nodes, edges, selected_nodes, outputfile, ranksep, nodesep);
		String dotFormat = toString(Utils.readFile(outputfile));
        int n = outputfile.lastIndexOf(".");
        String dotfilename = null;
        if (n != -1) {
        	dotfilename = outputfile.substring(0, n) + "." + format;
		}
		createDotGraph(dotFormat, format, dotfilename);
	}



//gif, svg, png
	public static void main(String[] args) throws Exception {
		String serviceUrl = args[0];
		String named_graph = args[1];
		System.out.println("serviceUrl: " + serviceUrl);
		System.out.println("named_graph: " + named_graph);
		OWLSPARQLUtils owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, null, null);
        owlSPARQLUtils.set_named_graph(named_graph);
        Vector v = owlSPARQLUtils.getDomainAndRangeData(named_graph);
        Vector data_vec = DelimitedDataExtractor.extract(v, "2|0|4", '|');
        float ranksep = (float) 1.0;
        float nodesep = (float) 1.5;
		String format = args[2];
		String filename = args[3];
	    GraphData gd = GraphGenerator.createGraphData(data_vec, ranksep, nodesep, format, filename);
	    GraphGenerator.run(gd);
	}



}
//https://stackoverflow.com/questions/19280229/graphviz-putting-a-caption-on-a-node-in-addition-to-a-label
