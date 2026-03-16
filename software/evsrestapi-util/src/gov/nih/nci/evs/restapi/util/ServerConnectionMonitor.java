package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class ServerConnectionMonitor {
	String serviceUrl = null;
	String named_graph = null;
	String username = null;
	String password = null;
    OWLSPARQLUtils owlSPARQLUtils = null;
    String TEST_QUERY_FILE = "test_query.txt";
    String CODE = "C12345";

	public ServerConnectionMonitor(String serviceUrl) {
		this.serviceUrl = serviceUrl;
    	this.named_graph = null;
    	this.username = ConfigurationController.username;
    	this.password = ConfigurationController.password;
        this.owlSPARQLUtils = new OWLSPARQLUtils(serviceUrl, username, password);
        this.owlSPARQLUtils.set_named_graph(named_graph);
	}

	public void set_code(String code) {
		this.CODE = code;
	}

	public String get_named_graph() {
		return named_graph;
	}

	public Vector getReleaseSchedule(int year, int month) throws Exception {
        Vector w = new Vector();
		String weekDay = "Monday";
		Vector weekday_vec = DateUtils.getWeekdayOfAMonth(year, month, weekDay);
		for (int j=0; j<weekday_vec.size(); j++) {
			Integer int_obj = (Integer) weekday_vec.elementAt(j);
			String version = DateUtils.getNCItVersion(year, month, j);
			String graphName = DateUtils.getNCIThesaurusGraphName(year, month, j);
			w.add("" + year + "|" + DateUtils.getMonthString(month) + "|" + version + "|" + graphName + "|" + DateUtils.getNCItReleaseDate(year, month, j));
		}
        return w;
	}

	public Vector getCurrentMonthReleaseSchedule() throws Exception {
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		return getReleaseSchedule(year, month);
	}

	public Vector getPreviousMonthReleaseSchedule() throws Exception {
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		if (month == 1) {
			year = year - 1;
			month = 12;
		} else {
			month = month - 1;
		}
		return getReleaseSchedule(year, month);
	}

    public void generateTestQuery() {
		try {
        	Vector w = getPreviousMonthReleaseSchedule();
        	String line = (String) w.elementAt(w.size()-1);
        	//(4) 2026|February|26.02d|http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus_26.02d.owl|02/23/2026
        	Vector u = StringUtils.parseData(line, '|');
        	named_graph = (String) u.elementAt(3);
        	this.owlSPARQLUtils.set_named_graph(named_graph);

		} catch (Exception ex) {

		}
		String outputfile = TEST_QUERY_FILE;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
            generateTestQuery(pw, named_graph);
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

    public void generateTestQuery(PrintWriter out, String namedGraph) {
		out.println("PREFIX :<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>");
		out.println("PREFIX base:<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl>");
		out.println("PREFIX Thesaurus:<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>");
		out.println("PREFIX xml:<http://www.w3.org/XML/1998/namespace>");
		out.println("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		out.println("PREFIX owl:<http://www.w3.org/2002/07/owl#>");
		out.println("PREFIX owl2xml:<http://www.w3.org/2006/12/owl2-xml#>");
		out.println("PREFIX protege:<http://protege.stanford.edu/plugins/owl/protege#>");
		out.println("PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>");
		out.println("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>");
		out.println("PREFIX ncicp:<http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#>");
		out.println("PREFIX dc:<http://purl.org/dc/elements/1.1/>");
		out.println("select distinct ?x_label ?p_value");
		out.println("from <" + namedGraph + ">");
		out.println("where  {");
		out.println("                ?x a owl:Class .");
		out.println("                ?x :NHC0 ?x_code .");
		out.println("                ?x :NHC0 \"" + CODE + "\"^^xsd:string .");
		out.println("                ?x rdfs:label ?x_label .");
		out.println("                ?p :NHC0 \"P108\"^^xsd:string .");
		out.println("                ?x ?p ?p_value .");
		out.println("}");
	}

	public boolean runTestQuery() {
        String query = owlSPARQLUtils.loadQuery(TEST_QUERY_FILE);
        Vector v = owlSPARQLUtils.executeQuery(query);
        if (v != null && v.size() > 0) {
			Utils.dumpVector("test_results", v);
			return true;
		}
        return false;
	}

	public static void run(String serviceUrl) {
		//String serviceUrl = ConfigurationController.serviceUrl;
        ServerConnectionMonitor monitor = new ServerConnectionMonitor(serviceUrl);
        try {
			Vector w = monitor.getCurrentMonthReleaseSchedule();
			Utils.dumpVector("Currrent month release schedule", w);

			w = monitor.getPreviousMonthReleaseSchedule();
			Utils.dumpVector("Previous month release schedule", w);

			monitor.generateTestQuery();

			System.out.println("named_graph: " + monitor.get_named_graph());
			boolean bool = monitor.runTestQuery();
			if (bool) {
				System.out.println("Server " + serviceUrl + " available.");
			} else {
				System.out.println("Server " + serviceUrl + " NOT available.");
			}


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		run(serviceUrl);
	}


}