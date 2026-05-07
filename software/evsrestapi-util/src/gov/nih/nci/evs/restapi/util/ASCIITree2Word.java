package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.FileOutputStream;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.util.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ASCIITree2Word {
	static String EVS_EXPLORE_URL = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";
	static String PARENT_CHILD_FILE = ConfigurationController.reportGenerationDirectory + File.separator + ConfigurationController.hierfile; // "parent_child.txt";

	public HashMap data_hmap = null;
	public static String PROPERTY_FILE = "config.properties";
	String asciitreefile = null;
	String docxfile = null;
	String root = null;
	static HierarchyHelper hh = null;

	static {
		hh = new HierarchyHelper(Utils.readFile(PARENT_CHILD_FILE));
	}

	public ASCIITree2Word() {

	}

	static XWPFHyperlinkRun createHyperlinkRun(XWPFParagraph paragraph, String uri) {
		String rId = paragraph.getDocument().getPackagePart().addExternalRelationship(
		uri,
		XWPFRelation.HYPERLINK.getRelation()
		).getId();

		CTHyperlink cthyperLink=paragraph.getCTP().addNewHyperlink();
		cthyperLink.setId(rId);
		cthyperLink.addNewR();

		return new XWPFHyperlinkRun(
		cthyperLink,
		cthyperLink.getRArray(0),
		paragraph
		);
	}

	public static String extractCode(String line) {
		int n = line.lastIndexOf("(");
		String s = line.substring(n+1, line.length()-1);
		return s;
	}

	public static String extractLabel(String line) {
		int n = line.lastIndexOf("(");
		String s = line.substring(0, n);
		return s;
	}

	public static String createHyperlink(String code) {
		return EVS_EXPLORE_URL + code;
	}

	public static void createNCItRun(XWPFDocument document, String line) {
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();
		String label = extractLabel(line);
		run.setText(label + "(");
		String code = extractCode(line);
		String url = createHyperlink(code);
		XWPFHyperlinkRun hyperlinkrun = createHyperlinkRun(paragraph, url);
		hyperlinkrun.setText(code);
		hyperlinkrun.setColor("0000FF");
		hyperlinkrun.setUnderline(UnderlinePatterns.SINGLE);
		run = paragraph.createRun();
		run.setText(")");
	}

	public ASCIITree2Word(String asciitreefile) {
        this.asciitreefile = asciitreefile;
        int n = asciitreefile.lastIndexOf(".");
        this.docxfile = asciitreefile.substring(0, n) + ".docx";

        n = asciitreefile.lastIndexOf("_");
        this.root = asciitreefile.substring(0, n);
        initialize();
	}

	public String getRoot() {
		return this.root;
	}

    public void initialize() {
		data_hmap = new HashMap();
		Vector v = Utils.readFile(PROPERTY_FILE);
        for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = gov.nih.nci.evs.restapi.util.StringUtils.parseData(line, '=');
			String key = (String) u.elementAt(0);
			String value = (String) u.elementAt(1);
			Vector values = gov.nih.nci.evs.restapi.util.StringUtils.parseData(value, '|');
			data_hmap.put(key, values);
		}
	}

	public void writeParagraph(XWPFHeader header,
	                           String alignment, //Center
	                           String content,
	                           boolean bold,
	                           String underline, //single
	                           String font, //"Arial"
	                           int fontSize) { //14

		XWPFParagraph paragraph = header.createParagraph();
		XWPFRun run = paragraph.createRun();
		run.setText(content);
		if (bold) {
			run.setBold(true);
		} else {
			run.setBold(false);
		}
		if (underline != null) {
			if (underline.compareToIgnoreCase("single") == 0) {
				run.setUnderline(UnderlinePatterns.SINGLE);
			}
		}
		run.setFontFamily(font);
		run.setFontSize(fontSize);
	}

    public XWPFHeaderFooterPolicy createXWPFHeaderFooterPolicy(XWPFDocument document) {
		XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
		if (headerFooterPolicy == null) {
			headerFooterPolicy = document.createHeaderFooterPolicy();
		}
		return headerFooterPolicy;
	}

    public void createFooter(XWPFFooter footer, String footerTitle, boolean bold, String font, int fontsize) {
		XWPFParagraph footerParagraph = footer.createParagraph();
		footerParagraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun footerRun=footerParagraph.createRun();
		footerRun.setText(footerTitle);
		footerRun.setBold(bold);
		footerRun.setFontFamily(font);
		footerRun.setFontSize(fontsize);
	}

    public void writeParagraph(XWPFDocument document, Vector content) {
		XWPFParagraph para = document.createParagraph();
		//para.setAlignment(ParagraphAlignment.LEFT);
		for (int i=0; i<content.size(); i++) {
			String line = (String) content.elementAt(i);
			XWPFRun paraRun = para.createRun();
			paraRun.setText(line);
			paraRun.addBreak();
			paraRun.setFontFamily("Arial");
			paraRun.setFontSize(11);
			paraRun.setBold(false);
			paraRun.setItalic(false);
		}
	}

    public void addImage(XWPFDocument document, String logo, int height, int width) {
		XWPFParagraph image = document.createParagraph();
		image.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun imageRun = image.createRun();
		imageRun.setTextPosition(20);
		try {
			Path imagePath = Paths.get(ClassLoader.getSystemResource(logo).toURI());
			imageRun.addPicture(Files.newInputStream(imagePath),
			XWPFDocument.PICTURE_TYPE_PNG, imagePath.getFileName().toString(),
				Units.toEMU(width), Units.toEMU(height));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    public String getLabel(String code) {
		return hh.getLabel(code);
	}

    public String getToday() {
		String t = gov.nih.nci.evs.restapi.util.StringUtils.getToday();
		return t;
	}

	public static void run(String asciitreefile) {
		ASCIITree2Word writer = new ASCIITree2Word(asciitreefile);
		try
		{
			XWPFDocument document = new XWPFDocument();
			XWPFHeaderFooterPolicy headerFooterPolicy = writer.createXWPFHeaderFooterPolicy(document);
			XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
			XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

			Vector logo_vec = new Vector();
			logo_vec.add("evs-logo_1.png");
			if (logo_vec != null) {
				String logo_file = (String) logo_vec.elementAt(0);
				BufferedImage bimg = ImageIO.read(new File(logo_file));
				int width = bimg.getWidth() / 2;
				int height = bimg.getHeight() / 2;
				writer.addImage(document, logo_file, height, width);
			}

			String headerText = writer.getLabel(writer.getRoot()) + " (" + writer.getRoot() + ")";
			writer.writeParagraph(header, "center", "\n" + headerText, true, "single", "Arial", 12);

			Vector w = Utils.readFile(asciitreefile);

			for (int i=0; i<w.size(); i++) {
				String line = (String) w.elementAt(i);
				line = " " + line;
				writer.createNCItRun(document, line);
			}

			FileOutputStream out = new FileOutputStream(writer.docxfile);
			document.write(out);
			out.close();
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String asciitreefile = args[0];
	    run(asciitreefile);
	}
}
