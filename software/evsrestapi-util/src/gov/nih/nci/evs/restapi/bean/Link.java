package gov.nih.nci.evs.restapi.bean;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
public class Link {
	int id;
	String sourceCode;
	String sourceName;
	String targetCode;
	String targetName;


// Default constructor
	public Link() {
	}

// Constructor
	public Link(
		int id,
		String sourceCode,
		String sourceName,
		String targetCode,
		String targetName) {
		this.id = id;
		this.sourceCode = sourceCode;
		this.sourceName = sourceName;
		this.targetCode = targetCode;
		this.targetName = targetName;
	}

	public int getId() {
		return id;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public String getTargetName() {
		return targetName;
	}


	@XmlAttribute
	public void setId(int id) {
		this.id = id;
	}

	@XmlElement
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	@XmlElement
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	@XmlElement
	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	@XmlElement
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String toString() {
		return "Id: " + this.id + "\n\t" + "SourceCode: " + this.sourceCode + "\n\t" + "SourceName: " + this.sourceName + "\n\t" + "TargetCode: " + this.targetCode + "\n\t" + "TargetName: " + this.targetName;
	}
}
