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
			for (int j=0; j<u.size(); j++) {
				tableRow.getCell(j).setText((String) u.elementAt(j));
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

