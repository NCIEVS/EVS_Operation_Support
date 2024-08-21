package gov.nih.nci.evs.restapi.appl;
import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nlm.nls.lvg.Api.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
public class AsciiApiUtils
{
	private ToAsciiApi api = null;

	public AsciiApiUtils() {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		String currentDirectory = System.getProperty("user.dir");
		properties.put("LVG_DIR", currentDirectory + "/");
		api = new ToAsciiApi(properties);
	}

	public AsciiApiUtils(String currentDirectory) {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		if (currentDirectory == null) {
			currentDirectory = System.getProperty("user.dir");
		}
		properties.put("LVG_DIR", currentDirectory + "/");
		api = new ToAsciiApi(properties);
	}


	public Vector readFromFile(String filename) {
		Vector v = new Vector();
		try {
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
				  new FileInputStream(filename)));
			String str;
			while ((str = in.readLine()) != null) {
				v.add(str);
			}
            in.close();
		} catch (Exception ex) {
            ex.printStackTrace();
		}
		return v;
	}

	public void writeToFile(String outputfile, String content) {
		try {
			File file = new File(outputfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String toAscii(String inTerm) {
		// mutate the inTerm
		String outTerm = api.Mutate(inTerm);
		return outTerm;
	}

	public String getFootNote() {
		return "<!-- Edited by LVG ToAsciiApi (version 2020) -->";
	}

    //scan non-ascii characters in a file
	public void scan(String inputfile, String outputfile) {
		try {
			File file = new File(outputfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			Vector lines = readFromFile(inputfile);
			for (int i=0; i<lines.size(); i++) {
				int j = i+1;
				String line = (String) lines.elementAt(i);
				String content = toAscii(line);
				String msg = null;
				if (line.compareTo(content) != 0) {
					msg = "WARNING: Line " + j + "  contains non--ascii characters: " + line;
					bw.write(msg);
					bw.write("\n");
				}
			}
			bw.write(getFootNote());
			bw.close();
			System.out.println("Output file " + outputfile + " generated.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void run(String inputfile, String outputfile) {
		try {
			File file = new File(outputfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			Vector lines = readFromFile(inputfile);
			for (int i=0; i<lines.size(); i++) {
				String line = (String) lines.elementAt(i);
				String content = toAscii(line);
				bw.write(content);
				bw.write("\n");
			}
			bw.write(getFootNote());
			bw.close();
			System.out.println("Output file " + outputfile + " generated.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args)
	{
		if(args.length != 1)
		{
			System.err.println("Usage: java AsciiApiUtils <inputfile>");
			System.exit(1);
		}
		AsciiApiUtils utils = new AsciiApiUtils();
		String inputfile = args[0];
		//Vector v = readFile(inputfile);
		int n = inputfile.lastIndexOf(".");
		String outputfile = inputfile.substring(0, n) + "_" + StringUtils.getToday() + inputfile.substring(n, inputfile.length());
		String warning_file = "warning_" + outputfile;
        new AsciiApiUtils().scan(inputfile, warning_file);
		new AsciiApiUtils().run(inputfile, outputfile);
	}
}
