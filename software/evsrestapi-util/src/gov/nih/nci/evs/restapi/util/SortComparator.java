package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
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
public class SortComparator implements Comparator<Object> {
    //private static Logger _logger = Logger.getLogger(SortComparator.class);
    private static final int SORT_BY_NAME = 1;
    private static final int SORT_BY_CODE = 2;
    private int _sort_option = SORT_BY_NAME;

    public SortComparator() {

    }

    public SortComparator(int sort_option) {
        _sort_option = sort_option;
    }

    private String getKey(Object c, int sort_option) {
        if (c == null)
            return "NULL";

        if (c instanceof TreeItem) {
            TreeItem ti = (TreeItem) c;
            if (sort_option == SORT_BY_CODE)
                return ti._code;
            return ti._text;
        } else if (c instanceof String) {
            String s = (String) c;
            return s;
        } else if (c instanceof Atom) {
			Atom atom = (Atom) c;
            String s = atom.getStr();
            return s;

        } else if (c instanceof ExtendedMetaRelationship) {
			ExtendedMetaRelationship extendedMetaRelationship = (ExtendedMetaRelationship) c;
            String s = extendedMetaRelationship.getSortKey();
            return s;

        } else if (c instanceof gov.nih.nci.evs.restapi.bean.Property) {
			gov.nih.nci.evs.restapi.bean.Property p = (gov.nih.nci.evs.restapi.bean.Property) c;
            String s = p.getName() + "|" + p.getValue();
            return s;
        } else if (c instanceof gov.nih.nci.evs.restapi.bean.Synonym) {
			gov.nih.nci.evs.restapi.bean.Synonym p = (gov.nih.nci.evs.restapi.bean.Synonym) c;
            String s = p.getTermName() + "|" + p.getTermSource() + "|" + p.getTermGroup();
            return s;
        } else if (c instanceof gov.nih.nci.evs.restapi.bean.ResolvedConceptReference) {
			gov.nih.nci.evs.restapi.bean.ResolvedConceptReference p = (gov.nih.nci.evs.restapi.bean.ResolvedConceptReference) c;
            String s = p.getName();
            return s;
        } else if (c instanceof gov.nih.nci.evs.restapi.bean.MapEntry) {
			gov.nih.nci.evs.restapi.bean.MapEntry p = (gov.nih.nci.evs.restapi.bean.MapEntry) c;
            String s = p.getSourceCode() + "|" + p.getSourceName();
            return s;
		}
        return c.toString();
    }

    private String replaceCharacter(String s, char from, char to) {
		if (s == null) return null;
		int ascii_from = (int) from;
		int ascii_to   = (int) to;
		StringBuffer t = new StringBuffer();
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			int ascii_c = (int) c;

			if (ascii_c == ascii_from) {
				t.append(to);
			} else {
				t.append(c);
			}
		}
	    return t.toString();
	}


    public int compare(Object object1, Object object2) {
        // case insensitive sort
        String key1 = getKey(object1, _sort_option);
        String key2 = getKey(object2, _sort_option);

        if (key1 == null || key2 == null)
            return 0;
        key1 = getKey(object1, _sort_option).toLowerCase();
        key2 = getKey(object2, _sort_option).toLowerCase();

        return key1.compareTo(key2);
    }
}
