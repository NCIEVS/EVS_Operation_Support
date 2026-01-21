package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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

public class RESTClient {
	private static final Logger log = LogManager.getLogger(RESTClient.class);

	private String username = null;
	private String password = null;
	private Duration readTimeout;
	private Duration connectTimeout;
	private String apiKey = null;
	private String apiUrl = null;

	public static String API_KEY = null;
	public RESTUtils restUtils = null;

	public RESTClient () {}

    public RESTClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

	public RESTClient(String username, String password, long readTimeout, long connectTimeout) {
		restUtils = new RESTUtils(username, password, readTimeout, connectTimeout);
	}

	public static String flattenCurlCommand(String filename) {
	    StringBuffer buf = new StringBuffer();
	    Vector v = Utils.readFile(filename);
	    for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			buf.append(t).append(" ");
		}
		String query = buf.toString();
		return query;
	}


    public static String get(String url) {
		String json = null;
		Vector w = new Vector();
		try {
			URL urlObj = new URL(url);
			URLConnection urlConnection = urlObj.openConnection();
			Charset charset = Charset.forName("UTF8");
			InputStreamReader stream = new InputStreamReader(urlConnection.getInputStream(), charset);
			BufferedReader reader = new BufferedReader(stream);
			StringBuffer responseBuffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				w.add(line);
			}
			json = (String) w.elementAt(0);
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return json;
	}

	public String query(String query, String restURL) {
		return restUtils.runSPARQL(query, restURL);
	}

	public static String loadQuery(String filename) {
		return RESTUtils.loadQuery(filename, false);
	}

    public String generateCompletion(String json) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            System.out.println(json);
            post.setEntity(new StringEntity(json));
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
    }

    public static Vector parseJSON(String json) {
        JSONUtils jsonUtils = new JSONUtils();
		Vector w = jsonUtils.parseJSON(json);
        w = jsonUtils.getResponseValues(w);
        return w;
	}

    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String restURL = args[0];
		String queryfile = args[1];
		String username = null;
		String password = null;
		String query = loadQuery(queryfile);
		long readTimeout = 60000;
		long connectTimeout = 60000;

		RESTClient client =
		  new RESTClient(username, password, readTimeout, connectTimeout);

		String json = client.query(query, restURL);
		System.out.println(json);

		Vector w = parseJSON(json);
        Utils.dumpVector("w", w);
	}

}
