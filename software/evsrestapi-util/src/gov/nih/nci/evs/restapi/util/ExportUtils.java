package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import java.io.*;
import java.net.*;
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
public class ExportUtils {
	private OWLSPARQLUtils owlSPARQLUtils;

// Default constructor
	public ExportUtils() {
	}

// Constructor
	public ExportUtils(OWLSPARQLUtils owlSPARQLUtils) {
		this.owlSPARQLUtils = owlSPARQLUtils;
	}

	public OWLSPARQLUtils getOwlSPARQLUtils() {
		return this.owlSPARQLUtils;
	}

    public RelatedConcepts buildRelatedConcepts(String named_graph, String code) {
		ParserUtils parser = new ParserUtils();
        Vector superclass_vec = owlSPARQLUtils.getSuperclassesByCode(named_graph, code);
        HashMap superclasses_hmap = parser.parseSuperclasses(superclass_vec);
        List superconcepts = new ArrayList();
        Iterator it = superclasses_hmap.keySet().iterator();
        while (it.hasNext()) {
			String cd = (String) it.next();
			Vector w = (Vector) superclasses_hmap.get(cd);
			String name = null;
			if (w != null) {
				name = (String) w.elementAt(0);
			}
			superconcepts.add(new Superconcept(cd, name));
		}

        Vector subclass_vec = owlSPARQLUtils.getSubclassesByCode(named_graph, code);
        HashMap subclasses_hmap = parser.parseSubclasses(subclass_vec);
        List subconcepts = new ArrayList();
        it = subclasses_hmap.keySet().iterator();
        while (it.hasNext()) {
			String cd = (String) it.next();
			Vector w = (Vector) subclasses_hmap.get(cd);
			String name = null;
			if (w != null) {
				name = (String) w.elementAt(0);
			}
			subconcepts.add(new Subconcept(cd, name));
		}

//		buf.append("SELECT distinct ?p_label ?y_label ?y_code ").append("\n");
        Vector roles_vec = owlSPARQLUtils.getRolesByCode(named_graph, code);
        roles_vec = parser.toDelimited(roles_vec, 3, '|');
        List roles = new ArrayList();
        for (int i=0; i<roles_vec.size(); i++) {
			String line = (String) roles_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(0);
			String concept_code = (String) u.elementAt(2);
			String concept_label = (String) u.elementAt(1);
			roles.add(new Role(rel, concept_code, concept_label));
		}

//		buf.append("SELECT distinct ?x_label ?x_code ?p_label ").append("\n");
        Vector inv_roles_vec = owlSPARQLUtils.getInverseRolesByCode(named_graph, code);
        inv_roles_vec = parser.toDelimited(inv_roles_vec, 3, '|');
        List inverseRoles = new ArrayList();
        for (int i=0; i<inv_roles_vec.size(); i++) {
			String line = (String) inv_roles_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(2);
			String concept_code = (String) u.elementAt(1);
			String concept_label = (String) u.elementAt(0);
			inverseRoles.add(new InverseRole(rel, concept_code, concept_label));
		}

//		buf.append("SELECT ?y_label ?z_label ?z_code").append("\n");
        Vector associations_vec = owlSPARQLUtils.getAssociationsByCode(named_graph, code);
        associations_vec = parser.toDelimited(associations_vec, 3, '|');
        List associations = new ArrayList();
        for (int i=0; i<associations_vec.size(); i++) {
			String line = (String) associations_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(0);
			String concept_code = (String) u.elementAt(2);
			String concept_label = (String) u.elementAt(1);
			associations.add(new Association(rel, concept_code, concept_label));
		}

//		buf.append("SELECT ?x_label ?x_code ?y_label").append("\n");
        Vector inv_associations_vec = owlSPARQLUtils.getInverseAssociationsByCode(named_graph, code);
        inv_associations_vec = parser.toDelimited(inv_associations_vec, 3, '|');
        List inverseAssoications = new ArrayList();
        for (int i=0; i<inv_associations_vec.size(); i++) {
			String line = (String) inv_associations_vec.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String rel = (String) u.elementAt(2);
			String concept_code = (String) u.elementAt(1);
			String concept_label = (String) u.elementAt(0);
			inverseAssoications.add(new InverseAssociation(rel, concept_code, concept_label));
		}
		return new RelatedConcepts(superconcepts, subconcepts, associations, inverseAssoications, roles, inverseRoles);
	}


    public ConceptDetails buildConceptDetails(String named_graph, String code,
        List mainMenuAncestors,
        Boolean isMainType,
        Boolean isSubtype,
        Boolean isDiseaseStage,
        Boolean isDiseaseGrade,
        Boolean isDisease,
        Boolean isBiomarker,
        Boolean isReferenceGene
        ) {
		Vector label_vec = owlSPARQLUtils.getLabelByCode(named_graph, code);
		Vector property_vec = owlSPARQLUtils.getPropertiesByCode(named_graph, code, false);
		property_vec = ParserUtils.formatOutput(property_vec);
		property_vec = ParserUtils.excludePropertyType(property_vec, "#A|#R");
		Vector property_qualifier_vec = owlSPARQLUtils.getPropertyQualifiersByCode(named_graph, code);
		Vector synonym_vec = new ParserUtils().filterPropertyQualifiers(property_qualifier_vec, Constants.FULL_SYN);
		Vector superconcept_vec = owlSPARQLUtils.getSuperclassesByCode(named_graph, code);
		Vector subconcept_vec = owlSPARQLUtils.getSubclassesByCode(named_graph, code);
		return new ConceptDetails(
				label_vec,
				property_vec,
				property_qualifier_vec,
				synonym_vec,
				superconcept_vec,
		        subconcept_vec,
		        mainMenuAncestors,
		        isMainType,
		        isSubtype,
		        isDiseaseStage,
		        isDiseaseGrade,
		        isDisease,
		        isBiomarker,
		        isReferenceGene
		        );
	}

    public Paths buildPaths(String named_graph, String code, int direction) {
        TreeBuilder treeBuilder = new TreeBuilder(owlSPARQLUtils);
		Vector u = treeBuilder.generateTreeData(code, direction);
		Vector v = new Vector();
		for (int i=0; i<u.size(); i++) {
			String t = (String) u.elementAt(i);
			if (t.indexOf("@") == -1) {
				v.add(t);
			}
		}
		PathFinder pathFinder = new PathFinder(v);
		Paths paths = pathFinder.findPaths();
		return paths;
	}

}
