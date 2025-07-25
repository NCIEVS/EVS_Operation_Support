package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.io.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Code
Codelist Code
Codelist Extensible (Yes/No)
Codelist Name
ICH Submission Value
ICH Synonym(s)
ICH Definition
NCI Preferred Term
*/

public class CDISCSubsetReportQA {
	OWLScanner scanner = null;
	String textfile = null;
    Vector reportData = null;
    String heading = null;
    Vector cols = null;
    Vector req_data_vec = null;
    HashMap synonymMap = null;
    HashMap sourcePTMap = null;
    HashMap sourceSYMap = null;
    HashMap nciABMap = null;
    HashMap nciPTMap = null;
    HashMap sourceDefMap = null;
    static String NCI = "NCI";
    static String SOURCE = "ICH";

    Vector codelistcodes = null;
    HashMap codelistcode2LineMap = null;

    static String NCIT_OWL = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.owlfile; //"ThesaurusInferred_forTS.owl";
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	public CDISCSubsetReportQA(String textfile) {
		this.textfile = textfile;
		initialize();
	}

	public void initialize() {
		scanner = new OWLScanner(NCIT_OWL);
		this.reportData = Utils.readFile(textfile);
		this.heading = (String) reportData.elementAt(0);
		this.cols = StringUtils.parseData(this.heading, '\t');
		this.reportData.remove(0);
		synonymMap = AxiomParser.loadSynonyms(AXIOM_FILE);

		this.req_data_vec = new Vector();
		String sourcePT = "P90|P384$" + SOURCE + "|P383$PT";
		String sourceSY = "P90|P384$" + SOURCE + "|P383$SY";
		String nciAB = "P90|P384$" + NCI + "|P383$AB";
		String nciPT = "P90|P384$" + NCI + "|P383$PT";
		String sourceDef = "P325|P378$" + SOURCE;

		req_data_vec.add(sourcePT);
		req_data_vec.add(sourceSY);
		req_data_vec.add(nciAB);
		req_data_vec.add(nciPT);
		req_data_vec.add(sourceDef);

        DataRetrieval retrieval = new DataRetrieval(NCIT_OWL, req_data_vec);
        sourcePTMap = retrieval.createCode2ValuesMap(sourcePT);
        sourceSYMap = retrieval.createCode2ValuesMap(sourceSY);
        nciABMap = retrieval.createCode2ValuesMap(nciAB);
        nciPTMap = retrieval.createCode2ValuesMap(nciPT);
        sourceDefMap = retrieval.createCode2ValuesMap(sourceDef);

        getCodeListCodes();
	}

	public void getCodeListCodes() {
		codelistcodes = new Vector();
		codelistcode2LineMap = new HashMap();
		for (int i=0; i<reportData.size(); i++) {
			String line = (String) reportData.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			String codelistcode = (String) u.elementAt(1);
			if (code.length() > 0 && codelistcode.length() == 0) {
				codelistcodes.add(code);
				codelistcode2LineMap.put(code, line);
			}
		}
	}

    public void reviewCodeListNames() {
		// codelistname is the ICH SY of the codelistcode concept, it has nothing to do with any member concept of the code list.
		// All codelistcode should have exactly one ICH SY; there won't be any ambiguity in determining codelistname.
		int num_errors = 0;
		for (int i=0; i<codelistcodes.size(); i++) {
			String codelistcode = (String) codelistcodes.elementAt(i);
			Vector w = (Vector) sourceSYMap.get(codelistcode);
			if (w == null) {
				num_errors++;
				System.out.println("ERROR: " + SOURCE + " " + "SY" + " does not exist for codeListCode " + codelistcode);
			} else if (w.size() > 1) {
				num_errors++;
				System.out.println("ERROR: Multiple " + SOURCE + " " + "SY" + " found for codeListCode " + codelistcode);
				Utils.dumpVector(codelistcode, w);
			} else {
				String line = (String) codelistcode2LineMap.get(codelistcode);
				Vector u = StringUtils.parseData(line, '\t');
				String reportValue = (String) u.elementAt(3);
				String expectedValue = (String) w.elementAt(0);
				if (expectedValue.compareTo(reportValue) != 0) {
					num_errors++;
					System.out.println("ERROR: Incorrect codelistname in report found for codeListCode " + codelistcode);
					System.out.println("\t: Reported value: " + reportValue);
					System.out.println("\t: Expected value: " + expectedValue);
				}
			}
		}
		System.out.println("CodeListName QA produces " + num_errors + " errors.");
	}

    public void reviewSubmissionValues() {
		int num_errors = 0;
		for (int i=0; i<reportData.size(); i++) {
			String line = (String) reportData.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			String codelistcode = (String) u.elementAt(1);
			Vector w = (Vector) sourcePTMap.get(code);
			if (w == null) {
				num_errors++;
				System.out.println("ERROR: " + SOURCE + " " + "PT" + " does not exist for codeListCode " + codelistcode);
			} else if (w.size() == 1) {
				String expectedValue = (String) w.elementAt(0);
			    String reportValue = (String) u.elementAt(4);
			    if (expectedValue.compareTo(reportValue) != 0) {
					num_errors++;
					System.out.println("ERROR: Incorrect " + SOURCE + "submission value in report found for codeListCode " + codelistcode);
					System.out.println("\t: Reported value: " + reportValue);
					System.out.println("\t: Expected value: " + expectedValue);
				}
			} else {
				w = (Vector) nciABMap.get(codelistcode);
				if (w == null || w.size() == 0) {
					num_errors++;
					System.out.println("ERROR: " + NCI + " " + "AB" + " does not exist for codelistcode " + codelistcode);
				} else if (w.size() > 1) {
					num_errors++;
					System.out.println("ERROR: Multiple " + NCI + " " + "AB" +  " found for codeListCode " + codelistcode);
					Utils.dumpVector(codelistcode, w);
				} else {
					String nci_ab = (String) w.elementAt(0);
					// search for FULL_SYN in code that matches with ICH PT's source_code equaling nci_ab
					Vector syns = (Vector) synonymMap.get(code);
					boolean match_found = false;
					for (int j=0; j<syns.size(); j++) {
						Synonym syn = (Synonym) syns.elementAt(j);
						if ((syn.getSourceCode() != null && syn.getSourceCode().compareTo(nci_ab) == 0)
						  & (syn.getTermSource() != null && syn.getTermSource().compareTo(SOURCE) == 0)
						  & (syn.getTermGroup() != null && syn.getTermGroup().compareTo("PT") == 0)) {
							String expectedValue = syn.getTermName();
							String reportValue = (String) u.elementAt(4);
							if (expectedValue.compareTo(reportValue) != 0) {
								num_errors++;
								System.out.println("ERROR: Incorrect " + SOURCE + "submission value in report found for (codeListCode " + codelistcode + ", " +
								code + ")");
								System.out.println("\t: Reported value: " + reportValue);
								System.out.println("\t: Expected value: " + expectedValue);
								num_errors++;
							}
							if (!match_found) {
								match_found = true;
							}
						}
					}
					if (!match_found) {
						System.out.println("ERROR: Incorrect " + SOURCE + "submission value in report found for (codeListCode " + codelistcode + ", " +
						code + ") - expected value not found.");
						num_errors++;
					}
				}
			}
		}
		System.out.println(SOURCE + " Submission Value QA produces " + num_errors + " errors.");
	}

	public void reviewNCIPTs() {
		int num_errors = 0;
		for (int i=0; i<reportData.size(); i++) {
			String line = (String) reportData.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			Vector w = (Vector) nciPTMap.get(code);
			if (w == null) {
				num_errors++;
				System.out.println("ERROR: " + NCI + " " + "PT" + " does not exist for code " + code);
			} else if (w.size() == 1) {
				String expectedValue = (String) w.elementAt(0);
			    String reportValue = (String) u.elementAt(7);
			    if (expectedValue.compareTo(reportValue) != 0) {
					num_errors++;
					System.out.println("ERROR: Incorrect " + NCI + "PT value in report found for code " + code);
					System.out.println("\t: Reported value: " + reportValue);
					System.out.println("\t: Expected value: " + expectedValue);
				}
			} else {
				num_errors++;
				System.out.println("WARNING: Multiple " + NCI + " " + "PT" +  " found for code " + code);
				Utils.dumpVector(code, w);
			}
		}
		System.out.println("NCI PT QA produces " + num_errors + " errors.");
	}

	public void reviewSourceDefinitions() {
		int num_errors = 0;
		for (int i=0; i<reportData.size(); i++) {
			String line = (String) reportData.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			Vector w = (Vector) sourceDefMap.get(code);
			if (w == null) {
				num_errors++;
				System.out.println("ERROR: " + SOURCE + " " + "definition" + " does not exist for code " + code);
			} else if (w.size() == 1) {
				String expectedValue = (String) w.elementAt(0);
			    String reportValue = (String) u.elementAt(6);
			    if (expectedValue.compareTo(reportValue) != 0) {
					num_errors++;
					System.out.println("ERROR: Incorrect " + SOURCE + " " + "definition value in report found for code " + code);
					System.out.println("\t: Reported value: " + reportValue);
					System.out.println("\t: Expected value: " + expectedValue);
				}
			} else {
				num_errors++;
				System.out.println("WARNING: Multiple " + SOURCE + " " + "definition found for code " + code);
				Utils.dumpVector(code, w);
			}
		}
		System.out.println(SOURCE + " definition QA produces " + num_errors + " warnings.");
	}

	public void reviewSourceSynonyms() {
		int num_errors = 0;
		for (int i=0; i<reportData.size(); i++) {
			String line = (String) reportData.elementAt(i);
			Vector u = StringUtils.parseData(line, '\t');
			String code = (String) u.elementAt(0);
			Vector w = (Vector) sourceSYMap.get(code);
			if (w == null) {
				num_errors++;
				System.out.println("ERROR: " + SOURCE + " " + "synonyms" + " does not exist for code " + code);
			} else if (w.size() == 1) {
				String expectedValue = (String) w.elementAt(0);
			    String reportValue = (String) u.elementAt(5);
			    if (expectedValue.compareTo(reportValue) != 0) {
					num_errors++;
					System.out.println("ERROR: Incorrect " + SOURCE + " " + "synonyms value in report found for code " + code);
					System.out.println("\t: Reported value: " + reportValue);
					System.out.println("\t: Expected value: " + expectedValue);
				}
			}
		}
		System.out.println(SOURCE + " synonym QA produces " + num_errors + " messages.");
	}

    public static Vector merge(String indent, Vector v1 , Vector v2) {
		Vector w = new Vector();
		for (int i=0; i<v1.size(); i++) {
			w.add((String) v1.elementAt(i) + ": " + (String) v2.elementAt(i));
		}
		return w;
	}

	public Vector run(String line, int case_num) {
		Vector w = new Vector();
		w.add("\n*** Test case: " + case_num);
		w.add(line);
		Vector u = StringUtils.parseData(line, '\t');
		String code = (String) u.elementAt(0);
		String codelistcode = (String) u.elementAt(1);
		w.addAll(merge("\t", cols, u));
		w.add("\n");
		Vector v1 = scanner.getOWLClassDataByCode(codelistcode);
		if (v1 != null) {
			w.addAll(v1);
			w.add("\n");
		}
		v1 = scanner.getOWLClassDataByCode(code);
		if (v1 != null) {
			w.addAll(v1);
			w.add("\n");
		}
		return w;
	}

	public void run(int max_cases) throws IOException {
        try {
			Vector v = (Vector) this.reportData.clone();
			Vector w = new Vector();
			w.add(heading);
			w.addAll(cols);
			w.add("\n");

			int num_cases = 0;
			int i = 0;
			while (num_cases < max_cases && i < v.size()) {
				String line = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(line, '\t');
				String code = (String) u.elementAt(0);
				String codelistcode = (String) u.elementAt(1);
				if (code.length() > 0 && codelistcode.length() > 0) {
					num_cases++;
					Vector w1 = run(line, num_cases);
					w.addAll(w1);
				}
				i++;
			}
			Utils.saveToFile("sample_" + StringUtils.getToday() + ".txt", w);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String textfile = args[0];
		CDISCSubsetReportQA qa = new CDISCSubsetReportQA(textfile);
		String max_cases_str = args[1];
		int max_cases = Integer.parseInt(max_cases_str);
		long ms = System.currentTimeMillis();
		//qa.run(max_cases);
		qa.reviewCodeListNames();
		qa.reviewSubmissionValues();

		qa.reviewSourceDefinitions();
		qa.reviewSourceSynonyms();
		qa.reviewNCIPTs();

		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

