package gov.nih.nci.evs.restapi.util;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

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
public class TableViewer extends JFrame
{
	public String datafile = null;
	public String[] columns = null;
	public String title = null;
	public char delim = '\t';

	public String[] getTableHeadings() {
	    Vector v = Utils.readFile(datafile);
	    int m = datafile.lastIndexOf(".");
	    title = datafile.substring(0, m);
	    String firstLine = (String) v.elementAt(0);
	    if (firstLine.indexOf("\t") == -1) {
			delim = '|';
		}
	    Vector u = StringUtils.parseData(firstLine, delim);
	    String[] headings = new String[u.size()];
	    for (int i=0; i<u.size(); i++) {
			String heading = (String) u.elementAt(i);
			headings[i] = heading;
		}
		return headings;
	}

	public Object[][] getTableData() {
		int num_cols = columns.length;
		Vector v = Utils.readFile(datafile);
		int num_rows = v.size()-1;
		Object[][] data = new Object[num_rows][num_cols];
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = StringUtils.parseData(line, delim);
			for (int j=0; j<u.size(); j++) {
				data[i-1][j] = u.elementAt(j);
			}
		}
		return data;
	}


    public TableViewer(String datafile) {
		this.datafile = datafile;
        columns = getTableHeadings();
        Object[][] data = getTableData();
         JTable table = new JTable(data, columns);
        this.add(new JScrollPane(table));
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    public static void show(String datafile) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TableViewer(datafile);
            }
        });
    }

    public static void main(String[] args) {
		String datafile = args[0];
		show(datafile);
    }
}
