package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;

//Reference: https://en.wikipedia.org/wiki/List_of_typographical_symbols_and_punctuation_marks

public class SpecialCharHandler {
	static HashMap replacementHashMap = null;
	static String ONTOLOGY_INFO_FILE = "ontology_info.owl";
    String owlfile = null;
	OWLClassLoader loader = null;
	HashMap classDataHashMap = null;
	Vector classIdVec = null;

	static {
		replacementHashMap = new HashMap();
        replacementHashMap.put("â€™", "—"); //Emdash
	}

    public SpecialCharHandler(String owlfile) {
		this.owlfile = owlfile;
		initialize();
	}

	public void initialize() {
		loader = new OWLClassLoader(owlfile);
		classDataHashMap = loader.getClassDataHashMap();
		classIdVec = loader.getClassIdVec();
		extractOntologyInfo(Utils.readFile(owlfile));
	}

    public void extractOntologyInfo(Vector v) {
	    int istart = 0;
	    String target = "</owl:Ontology>";
	    int iend = TextFileExtractor.findLineNumber(v, target) + 3;
	    String outputfile = ONTOLOGY_INFO_FILE;
	    Vector w = TextFileExtractor.extractLines(v, istart, iend);
	    Utils.saveToFile(ONTOLOGY_INFO_FILE, w);
	}

	public String generateSampleOWL(String code) {
		Vector w = new Vector();
		w.addAll(Utils.readFile(ONTOLOGY_INFO_FILE));
		w.addAll((Vector) classDataHashMap.get(code));
		w.add("</rdf:RDF>");
		String outputfile = code + ".owl";
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}


	public static String run(String line) {
		Iterator it = replacementHashMap.keySet().iterator();
		while (it.hasNext()) {
			String replace = (String) it.next();
			String by = (String) replacementHashMap.get(replace);
			line = line.replace(replace, by);
		}
		return line;
	}

	public static Vector run(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = run(line);
			w.add(line);
		}
		return w;
	}

	public static Vector substituteSpecChars(String filename) {
		return run(Utils.readFile(filename));
	}

	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		//////////////////////////////////////////////////////////////////
		String owlfile = args[0];
		String code = args[1];
		System.out.println("NCIt owlfile: " + owlfile);
		System.out.println("code: " + code);
		SpecialCharHandler test = new SpecialCharHandler(owlfile);

		System.out.println("generating class owl: " + code + ".owl");
		String outputfile = test.generateSampleOWL(code);
		System.out.println("generated class owl: " + outputfile);
		//////////////////////////////////////////////////////////////////

		System.out.println("substituteSpecChars ... ");
		Vector w = substituteSpecChars(outputfile);
		Utils.saveToFile(outputfile, w);
        System.out.println("\tTotal run time (ms): " + (System.currentTimeMillis() - ms));
	}

}
