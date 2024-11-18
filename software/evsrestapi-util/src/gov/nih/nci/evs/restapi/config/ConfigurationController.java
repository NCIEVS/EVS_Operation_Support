package gov.nih.nci.evs.restapi.config;
import gov.nih.nci.evs.restapi.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

public class ConfigurationController {
	/** The sys prop. */
	private static Properties sysProp = System.getProperties();

	/** The dom. */
	private static Document dom;

	/** The properties. */
	private static Properties properties = loadProperties();

	/** The Constants. */
	public final static String axiomfile = properties.getProperty("axiomfile");
	public final static String conceptCodeColumnNumber = properties.getProperty("conceptCodeColumnNumber");
	public final static String downloadDirectory = properties.getProperty("downloadDirectory");
	public final static String hierfile = properties.getProperty("hierfile");
	public final static String mapping_datafile = properties.getProperty("mapping_datafile");
	public final static String mapping_filename = properties.getProperty("mapping_filename");
	public final static String mapping_name = properties.getProperty("mapping_name");
	public final static String mapping_source_graphname = properties.getProperty("mapping_source_graphname");
	public final static String mapping_source_id = properties.getProperty("mapping_source_id");
	public final static String mapping_source_shortname = properties.getProperty("mapping_source_shortname");
	public final static String mapping_target_graphname = properties.getProperty("mapping_target_graphname");
	public final static String mapping_target_id = properties.getProperty("mapping_target_id");
	public final static String mapping_target_shortname = properties.getProperty("mapping_target_shortname");
	public final static String mapping_version = properties.getProperty("mapping_version");
	public final static String masterConceptCodeColumnNumber = properties.getProperty("masterConceptCodeColumnNumber");
	public final static String masterSubsetCodeColumnNumber = properties.getProperty("masterSubsetCodeColumnNumber");
	public final static String match_file_heading = properties.getProperty("match_file_heading");
	public final static String namedGraph = properties.getProperty("namedGraph");
	public final static String ontology_description = properties.getProperty("ontology_description");
	public final static String ontology_release_date = properties.getProperty("ontology_release_date");
	public final static String ontology_version_info = properties.getProperty("ontology_version_info");
	public final static String owlfile = properties.getProperty("owlfile");
	public final static String password = properties.getProperty("password");
	public final static String reportGenerationDirectory = properties.getProperty("reportGenerationDirectory");
	public final static String rolefile = properties.getProperty("rolefile");
	public final static String serviceUrl = properties.getProperty("serviceUrl");
	public final static String serviceUrls = properties.getProperty("serviceUrls");
	public final static Vector serviceUrl_vec = StringUtils.parseData(serviceUrls, '|');
	public final static String source_coding_scheme = properties.getProperty("source_coding_scheme");
	public final static String source_coding_scheme_version = properties.getProperty("source_coding_scheme_version");
	public final static String source_ns = properties.getProperty("source_ns");
	public final static String subsetCodeColumnNumber = properties.getProperty("subsetCodeColumnNumber");
	public final static String target_coding_scheme = properties.getProperty("target_coding_scheme");
	public final static String target_coding_scheme_version = properties.getProperty("target_coding_scheme_version");
	public final static String target_ns = properties.getProperty("target_ns");
	public final static String term_column = properties.getProperty("term_column");
	public final static String term_file_delim = properties.getProperty("term_file_delim");
	public final static String term_file_heading = properties.getProperty("term_file_heading");
	public final static String termfiles = properties.getProperty("termfiles");
	public final static String username = properties.getProperty("username");
	public final static String primitiveConceptRoots = properties.getProperty("primitiveConceptRoots");
	public final static String branchfile = properties.getProperty("branchfile");
	public final static String branchroots = properties.getProperty("branchroots");

	/**
	 * To be implemented by each descendant testcase.
	 *
	 * @return String
	 */
	protected String getTestID(){
		return "Test Case";
	}


	/**
	 * Load properties.
	 *
	 * @return the properties
	 */
	private static Properties loadProperties() {
		try{
			Properties lproperties = new Properties();
			String propertyFile = "resources/Test.properties";
			File file = new File(propertyFile);
			if (!file.exists()) {
				System.out.println("WARNING: property file " + propertyFile + " does not exists.");
			} else {
				FileInputStream fis = new FileInputStream(file);
				lproperties.load(fis);
			}
			return lproperties;
		} catch (Exception e){
			System.out.println("Error reading properties file");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses the xml file.
	 *
	 * @param filename the filename
	 */
	private static void parseXMLFile(String filename)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom=db.parse(filename);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

}


