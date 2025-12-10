package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Generates a report based on a report template file.
 *
 */
public class ReportTemplateGenerator {

    public ReportTemplateGenerator() {}


/*
name:Has_PCDC_Data_Type|type:Association|rootConceptCode:|association:Has_PCDC_Data_Type|level:0|sortColumn:2
label:NCIt Subset Code|display:code|propertyType:Associated Concept Code|property:NHC0|source:|group:|subsource:
label:NCIt Subset Name|display:property|propertyType:Associated Concept Code|property:P108|source:|group:|subsource:
label:NCIt Code|display:code|propertyType:code|property:NHC0|source:|group:|subsource:
label:NCIt NCIt PT|display:property|propertyType:FULL_SYN|property:P90|source:NCI|group:PT|subsource:
label:NCIt Synonyms|display:property|propertyType:FULL_SYN|property:P90|source:NCI|group:SY|subsource:

name: Has_PCDC_Data_Type
type: Association
rootConceptCode:
association: Has_PCDC_Data_Type
level: 0
sortColumn: 2
*/

    public void run(String inputfile, String outputfile) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			String indent = "    ";
			Vector v = Utils.readFile(inputfile);
			String heading = (String) v.elementAt(0);
			Vector u = StringUtils.parseData(heading, '|');
			for (int i=0; i<u.size(); i++) {
				String t = (String) u.elementAt(i);
				t = t.replace(":", ": ");
				pw.println(t);
			}
			pw.println("columns:");
			for (int i=1; i<v.size(); i++) {
				pw.println("  - columnNumber: " + i);
				String t = (String) v.elementAt(i);
				t = t.trim();
				u = StringUtils.parseData(t, '|');
				for (int j=0; j<u.size(); j++) {
					String s = (String) u.elementAt(j);
					s = s.replace(":", ": ");
					pw.println(indent + s);
				}
				if (i<v.size()-1) pw.print("\n");
			}
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


	public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String template_data_file = args[0];
		String template_file = args[1];
        new ReportTemplateGenerator().run(template_data_file, template_file);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

}
