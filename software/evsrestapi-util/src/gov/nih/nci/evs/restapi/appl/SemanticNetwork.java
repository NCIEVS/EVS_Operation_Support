package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;


public class SemanticNetwork {
//https://www.nlm.nih.gov/research/umls/licensedcontent/umlsarchives04.html
//https://web.archive.org/web/20241127183822/https://lhncbc.nlm.nih.gov/semanticnetwork/SemanticNetworkArchive.html
/*
<tr>
<td>2023AA</td>
<td><a href="/web/20241127183822/https://lhncbc.nlm.nih.gov/semanticnetwork/download/sn_archive/2023AA.tgz"> SN_2023AA</a></td>
*/

    public static void generateDynamicHTMLTree(String parent_child_file, String htmlfile) {
		Vector parent_child_vec = Utils.readFile(parent_child_file);
        TreeItem ti = new ASCIITreeUtils().createTreeItem(parent_child_vec);
        HashMap hmap = new HashMap();
        hmap.put("<Root>", ti);
        String url = null;
        //String url = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";
        new SimpleTreeUtils().writeTree2HTML(hmap, url, htmlfile);
    }

	public static void run(String dir) {
		Vector w1 = Utils.readFile(dir + File.separator + "SRDEF");
		System.out.println(w1.size());
		HashMap code2LabelMap = new HashMap();
		HashMap label2CodeMap = new HashMap();
		HashMap label2SCNMap = new HashMap();
		HashMap label2DefMap = new HashMap();
		for (int i=0; i<w1.size(); i++) {
			String line = (String) w1.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			code2LabelMap.put((String) u.elementAt(1), (String) u.elementAt(2));
			label2CodeMap.put((String) u.elementAt(2), (String) u.elementAt(1));
			label2SCNMap.put((String) u.elementAt(2), (String) u.elementAt(3));
			label2DefMap.put((String) u.elementAt(2), (String) u.elementAt(4));
		}

        //Carbohydrate Sequence|isa|Idea or Concept|
		Vector w2 = Utils.readFile(dir + File.separator + "SRSTRE2");
		System.out.println(w2.size());

		Vector w = new Vector();
		for (int i=0; i<w2.size(); i++) {
			String line = (String) w2.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(1);
			if (rel.compareTo("isa") == 0) {
				String child_label = (String) u.elementAt(0);
				String parent_label = (String) u.elementAt(2);
				w.add(parent_label + "|" + (String) label2CodeMap.get(parent_label) + "|" +
				      child_label + "|" + (String) label2CodeMap.get(child_label));
			}
		}
		Utils.saveToFile("semantic_network_parent_child.txt", w);
		HierarchyHelper hh = new HierarchyHelper(w);
		PrintWriter pw = null;
		String asciitreefile = "sty_tree.txt";
		try {
			pw = new PrintWriter(asciitreefile, "UTF-8");
			hh.printTree(pw);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + asciitreefile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		w = new Vector();
		w1 = new Vector();
		w.add("Semantic Type Label\tSemantic Type Label\tSCN\tDefinition");
		Iterator it = label2CodeMap.keySet().iterator();
		while (it.hasNext()) {
			String sty = (String) it.next();
			String line = sty + "\t" + (String) label2CodeMap.get(sty) + "\t" + (String) label2SCNMap.get(sty) + "\t" + (String) label2DefMap.get(sty);
			w1.add(line);
		}
		w1 = new SortUtils().quickSort(w1);
		w.addAll(w1);
		Utils.saveToFile("STY_2023AA.txt", w);

		//generateDynamicHTMLTree(asciitreefile, "html_" + asciitreefile);
	}

	public static void main(String[] args) {
		String dir = args[0];
		run(dir);
	}

}