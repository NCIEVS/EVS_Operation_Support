package gov.nih.nci.evs.restapi.util;

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

public class MSTable {
	static String EVS_EXPLORE_URL = "https://evsexplore.semantics.cancer.gov/evsexplore/concept/ncit/";

	public static String createHyperlink(String code) {
		return EVS_EXPLORE_URL + code;
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

    public static void addImage(XWPFDocument document, String logo, int height, int width) {
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

	public static void addHyperlinkToCell(XWPFTableCell cell, String url, String text) {
		XWPFParagraph paragraph = cell.addParagraph();
		XWPFHyperlinkRun run = createHyperlinkRun(paragraph, url);
		run.setText(text);
		run.setColor("0000FF");
		run.setUnderline(UnderlinePatterns.SINGLE);
	}

	public static void run(String datafile) throws IOException {
		int n = datafile.lastIndexOf(".");
		String docxfile = datafile.substring(0, n) + ".docx";
		String title = datafile.substring(0, n);
		title = title.replace("_", " ");
		Vector v = Utils.readFile(datafile);
		Vector u = StringUtils.parseData((String) v.elementAt(0), '|');

		//Blank Document
		XWPFDocument document = new XWPFDocument();
		Vector logo_vec = new Vector();
		logo_vec.add("evs-logo_1.png");

		if (logo_vec != null) {
			String logo_file = (String) logo_vec.elementAt(0);
			BufferedImage bimg = ImageIO.read(new File(logo_file));
			int width = bimg.getWidth() / 2;
			int height = bimg.getHeight() / 2;
			addImage(document, logo_file, height, width);
		}

		//Write the Document in file system
		FileOutputStream out = new FileOutputStream(new File(docxfile));

		//Write second Text after the table (by creating a new paragraph)
		XWPFParagraph para = document.createParagraph();
		para.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run2 = para.createRun();
		run2.setBold(true);
		run2.setText("Table 1: " + title + ".");
		para = document.createParagraph();
		para.setAlignment(ParagraphAlignment.CENTER);

		//create table
		XWPFTable table = document.createTable();
		//create first row
		XWPFTableRow tableRow = table.getRow(0);
		XWPFRun run = tableRow.getCell(0).addParagraph().createRun();
		run.setBold(true);
		run.setText((String) u.elementAt(0));
		for (int i=1; i<u.size(); i++) {
			run = tableRow.addNewTableCell().addParagraph().createRun();
			run.setBold(true);
			run.setText((String) u.elementAt(i));
		}

		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			u = StringUtils.parseData(line, '|');
            tableRow = table.createRow();
            tableRow.setHeight(10);
			for (int j=0; j<u.size(); j++) {
				String code = (String) u.elementAt(j);
				if (StringUtils.isNCItCode(code)) {
					String url = createHyperlink(code);
					String text = code;
					addHyperlinkToCell(tableRow.getCell(j), url, text);
				} else {
					run = tableRow.getCell(j).addParagraph().createRun();
					run.setText((String) u.elementAt(j));
				}
			}
		}
		document.write(out);
		out.close();
		System.out.println(docxfile + " written successully.");
	}

	public static void main(String[] args) throws IOException {
		String datafile = args[0];
		run(datafile);
	}
}

