package gov.nih.nci.evs.restapi.appl;

import gov.nih.nci.evs.restapi.util.*;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.common.*;
import gov.nih.nci.evs.restapi.config.*;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.codec.binary.Base64;
import org.json.*;

public class MapsToReportWriterRunner {

    public static void main(String[] args) {
		String serviceUrl = ConfigurationController.serviceUrl;
		String namedGraph = ConfigurationController.namedGraph;
		String username = ConfigurationController.username;
		String password = ConfigurationController.password;

		System.out.println("serviceUrl: " + serviceUrl);
		System.out.println("namedGraph: " + namedGraph);

		long ms = System.currentTimeMillis();
		MapsToReportWriter mapsToReportWriter = new MapsToReportWriter(serviceUrl, namedGraph, username, password);

		String terminology_name = "ICDO3";
		String terminology_version = "3.1";
		mapsToReportWriter.run(terminology_name, terminology_version);

		terminology_version = "3.2";
		mapsToReportWriter.run(terminology_name, terminology_version);

        System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }
}


