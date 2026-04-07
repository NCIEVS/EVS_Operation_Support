package gov.nih.nci.evs.restapi.util;
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
			//Example: "SRS_Base.txt" 1 C1909 2"
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

//0: datafile
//1: mode
//2: col_str    root     root
//3: termfile   col_str

//java -Xms512m -Xmx4g -classpath %CLASSPATH% ExactMatchRunner agents.txt 0 1 axioms.txt
//java -Xms512m -Xmx4g -classpath %CLASSPATH% ExactMatchRunner agents.txt 1 root col_str
//java -Xms512m -Xmx4g -classpath %CLASSPATH% ExactMatchRunner agents.txt 2 root