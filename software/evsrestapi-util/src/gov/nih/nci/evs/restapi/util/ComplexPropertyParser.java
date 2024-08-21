package gov.nih.nci.evs.restapi.util;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.XStream;
import gov.nih.nci.evs.restapi.bean.*;
import java.io.*;
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
public class ComplexPropertyParser {
    public static String reformat(String str) {
		str = str.replaceAll("< ncicp", "<ncicp");
		str = str.replaceAll("< /ncicp", "</ncicp");
		int n = str.indexOf("xmlns");
		str = str.substring(0, n-1) + str.substring(n+73, str.length());
		str = str.replaceAll("ncicp:", "");
		str = str.replaceAll("def-", "");
		str = str.replaceAll("term-", "");
		str = str.replaceAll("subsource-name", "subsource_name");
		str = str.replaceAll("source-", "");
		str = str.replaceAll("ComplexTerm", "gov.nih.nci.evs.restapi.bean.ComplexTerm");
		str = str.replaceAll("ComplexDefinition", "gov.nih.nci.evs.restapi.bean.ComplexDefinition");
		return str;
	}

    public static ComplexDefinition convertToComplexDefinition(String definition) {
		System.out.println(definition);
        XStream xStream = new XStream();
		xStream.alias("gov.nih.nci.evs.restapi.bean.ComplexDefinition", gov.nih.nci.evs.restapi.bean.ComplexDefinition.class);
		String xml = reformat(definition);
        gov.nih.nci.evs.restapi.bean.ComplexDefinition complexDefinition = (gov.nih.nci.evs.restapi.bean.ComplexDefinition) xStream.fromXML(xml);
        return complexDefinition;
	}

    public static ComplexTerm convertToComplexTerm(String full_syn) {
		System.out.println(full_syn);
        XStream xStream = new XStream();
		xStream.alias("gov.nih.nci.evs.sparqlbrowser.bean.ComplexTerm", gov.nih.nci.evs.restapi.bean.ComplexTerm.class);
		String xml = reformat(full_syn);
        gov.nih.nci.evs.restapi.bean.ComplexTerm complexTerm = (gov.nih.nci.evs.restapi.bean.ComplexTerm) xStream.fromXML(xml);
        return complexTerm;
	}


    public static void main(String[] args) {
		String full_syn = "<ncicp:ComplexTerm xmlns:ncicp=\"http://ncicb.nci.nih.gov/xml/owl/EVS/ComplexProperties.xsd#\"><ncicp:term-name>Activity</ncicp:term-name><ncicp:term-group>PT</ncicp:term-group><ncicp:term-source>BRIDG</ncicp:term-source></ncicp:ComplexTerm>";
		ComplexTerm term = convertToComplexTerm(full_syn);
		System.out.println(term.toString());
    }

}

