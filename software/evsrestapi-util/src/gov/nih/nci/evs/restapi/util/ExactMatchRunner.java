package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
import java.net.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import java.util.*;

public class ExactMatchRunner {
	static String AXIOM_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.axiomfile;

	public static void run(String termfile, int mode, String root, String datafile, int col, String outputfile) {
		if (termfile == null) {
			termfile = AXIOM_FILE;
		}
		if (mode == ExactMatch.DEFAULT) {
			new ExactMatch(termfile).run(datafile, outputfile, col, true);
		} else if (mode == ExactMatch.BRANCH) {
			new ExactMatch(termfile).mapToBranch(root, datafile, col);
		} else if (mode == ExactMatch.SUBSET) {
			new ExactMatch(termfile).mapToSubset(root, datafile);
		}
	}

	public static void run(String[] args) {
		String datafile = args[0];
		String outputfile = "results_" + datafile;
		String mode_str = args[1];
		int mode = Integer.parseInt(mode_str);
		if (mode == ExactMatch.DEFAULT) {
			String col_str = args[2];
			int col = Integer.parseInt(col_str);
			outputfile = "col_" + col + "_" + outputfile;
			if (args.length == 4) {
				String termfile = args[3];
				new ExactMatch(termfile).run(datafile, outputfile, col, true);
			} else {
				new ExactMatch().run(datafile, outputfile, col, true);
			}
		} else if (mode == ExactMatch.BRANCH) {
			String root = args[2];
			String col_str = args[3];
			int col = Integer.parseInt(col_str);
			ExactMatch.mapToBranch(root, datafile, col);
		} else if (mode == ExactMatch.SUBSET) {
			String root = args[2];
			ExactMatch.mapToSubset(root, datafile);
		}
	}

    public static void main(String[] args) {
		run(args);
	}
}

