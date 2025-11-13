package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class JSONViewerTableGenerator {

	public JSONViewerTableGenerator() {

	}

	public static void writeScript(PrintWriter out) {
		out.println("<style>");
		out.println("");
		out.println("/* Root element */");
		out.println(".json-document {");
		out.println("  padding: 1em 2em;");
		out.println("}");
		out.println("");
		out.println("/* Syntax highlighting for JSON objects */");
		out.println("ul.json-dict, ol.json-array {");
		out.println("  list-style-type: none;");
		out.println("  margin: 0 0 0 1px;");
		out.println("  border-left: 1px dotted #ccc;");
		out.println("  padding-left: 2em;");
		out.println("}");
		out.println(".json-string {");
		out.println("  color: #0B7500;");
		out.println("}");
		out.println(".json-literal {");
		out.println("  color: #1A01CC;");
		out.println("  font-weight: bold;");
		out.println("}");
		out.println("");
		out.println("/* Toggle button */");
		out.println("a.json-toggle {");
		out.println("  position: relative;");
		out.println("  color: inherit;");
		out.println("  text-decoration: none;");
		out.println("}");
		out.println("a.json-toggle:focus {");
		out.println("  outline: none;");
		out.println("}");
		out.println("a.json-toggle:before {");
		out.println("  font-size: 1.1em;");
		out.println("  color: #c0c0c0;");
		out.println("  content: \"\25BC\"; /* down arrow */");
		out.println("  position: absolute;");
		out.println("  display: inline-block;");
		out.println("  width: 1em;");
		out.println("  text-align: center;");
		out.println("  line-height: 1em;");
		out.println("  left: -1.2em;");
		out.println("}");
		out.println("a.json-toggle:hover:before {");
		out.println("  color: #aaa;");
		out.println("}");
		out.println("a.json-toggle.collapsed:before {");
		out.println("  /* Use rotated down arrow, prevents right arrow appearing smaller than down arrow in some browsers */");
		out.println("  transform: rotate(-90deg);");
		out.println("}");
		out.println("");
		out.println("/* Collapsable placeholder links */");
		out.println("a.json-placeholder {");
		out.println("  color: #aaa;");
		out.println("  padding: 0 1em;");
		out.println("  text-decoration: none;");
		out.println("}");
		out.println("a.json-placeholder:hover {");
		out.println("  text-decoration: underline;");
		out.println("}");
		out.println("");
		out.println("");
		out.println("</style>");
		out.println("<script>");
		out.println("");
		out.println("/**");
		out.println(" * jQuery json-viewer");
		out.println(" * @author: Alexandre Bodelot <alexandre.bodelot@gmail.com>");
		out.println(" * @link: https://github.com/abodelot/jquery.json-viewer");
		out.println(" */");
		out.println("(function($) {");
		out.println("");
		out.println("  /**");
		out.println("   * Check if arg is either an array with at least 1 element, or a dict with at least 1 key");
		out.println("   * @return boolean");
		out.println("   */");
		out.println("  function isCollapsable(arg) {");
		out.println("    return arg instanceof Object && Object.keys(arg).length > 0;");
		out.println("  }");
		out.println("");
		out.println("  /**");
		out.println("   * Check if a string represents a valid url");
		out.println("   * @return boolean");
		out.println("   */");
		/*
		out.println("  function isUrl(string) {");
		out.println("    var urlRegexp = /^(https?:\/\/|ftps?:\/\/)?([a-z0-9%-]+\.){1,}([a-z0-9-]+)?(:(\d{1,5}))?(\/([a-z0-9\-._~:/?#[\]@!$&'()*+,;=%]+)?)?$/i;");
		out.println("    return urlRegexp.test(string);");
		out.println("  }");
		*/
		out.println("");
		out.println("  /**");
		out.println("   * Transform a json object into html representation");
		out.println("   * @return string");
		out.println("   */");
		out.println("  function json2html(json, options) {");
		out.println("    var html = '';");
		out.println("    if (typeof json === 'string') {");
		out.println("      // Escape tags and quotes");
		out.println("      json = json");
		out.println("        .replace(/&/g, '&amp;')");
		out.println("        .replace(/</g, '&lt;')");
		out.println("        .replace(/>/g, '&gt;')");
		out.println("        .replace(/'/g, '&apos;')");
		out.println("        .replace(/\"/g, '&quot;');");
		out.println("");
		//out.println("      if (options.withLinks && isUrl(json)) {");
		out.println("      if (options.withLinks) {");
		out.println("        html += '<a href=\"' + json + '\" class=\"json-string\" target=\"_blank\">' + json + '</a>';");
		out.println("      } else {");
		out.println("        // Escape double quotes in the rendered non-URL string.");
		out.println("        json = json.replace(/&quot;/g, '\\&quot;');");
		out.println("        html += '<span class=\"json-string\">\"' + json + '\"</span>';");
		out.println("      }");
		out.println("    } else if (typeof json === 'number') {");
		out.println("      html += '<span class=\"json-literal\">' + json + '</span>';");
		out.println("    } else if (typeof json === 'boolean') {");
		out.println("      html += '<span class=\"json-literal\">' + json + '</span>';");
		out.println("    } else if (json === null) {");
		out.println("      html += '<span class=\"json-literal\">null</span>';");
		out.println("    } else if (json instanceof Array) {");
		out.println("      if (json.length > 0) {");
		out.println("        html += '[<ol class=\"json-array\">';");
		out.println("        for (var i = 0; i < json.length; ++i) {");
		out.println("          html += '<li>';");
		out.println("          // Add toggle button if item is collapsable");
		out.println("          if (isCollapsable(json[i])) {");
		out.println("            html += '<a href class=\"json-toggle\"></a>';");
		out.println("          }");
		out.println("          html += json2html(json[i], options);");
		out.println("          // Add comma if item is not last");
		out.println("          if (i < json.length - 1) {");
		out.println("            html += ',';");
		out.println("          }");
		out.println("          html += '</li>';");
		out.println("        }");
		out.println("        html += '</ol>]';");
		out.println("      } else {");
		out.println("        html += '[]';");
		out.println("      }");
		out.println("    } else if (typeof json === 'object') {");
		out.println("      var keyCount = Object.keys(json).length;");
		out.println("      if (keyCount > 0) {");
		out.println("        html += '{<ul class=\"json-dict\">';");
		out.println("        for (var key in json) {");
		out.println("          if (Object.prototype.hasOwnProperty.call(json, key)) {");
		out.println("            html += '<li>';");
		out.println("            var keyRepr = options.withQuotes ?");
		out.println("              '<span class=\"json-string\">\"' + key + '\"</span>' : key;");
		out.println("            // Add toggle button if item is collapsable");
		out.println("            if (isCollapsable(json[key])) {");
		out.println("              html += '<a href class=\"json-toggle\">' + keyRepr + '</a>';");
		out.println("            } else {");
		out.println("              html += keyRepr;");
		out.println("            }");
		out.println("            html += ': ' + json2html(json[key], options);");
		out.println("            // Add comma if item is not last");
		out.println("            if (--keyCount > 0) {");
		out.println("              html += ',';");
		out.println("            }");
		out.println("            html += '</li>';");
		out.println("          }");
		out.println("        }");
		out.println("        html += '</ul>}';");
		out.println("      } else {");
		out.println("        html += '{}';");
		out.println("      }");
		out.println("    }");
		out.println("    return html;");
		out.println("  }");
		out.println("");
		out.println("  /**");
		out.println("   * jQuery plugin method");
		out.println("   * @param json: a javascript object");
		out.println("   * @param options: an optional options hash");
		out.println("   */");
		out.println("  $.fn.jsonViewer = function(json, options) {");
		out.println("    // Merge user options with default options");
		out.println("    options = Object.assign({}, {");
		out.println("      collapsed: false,");
		out.println("      rootCollapsable: true,");
		out.println("      withQuotes: false,");
		out.println("      withLinks: true");
		out.println("    }, options);");
		out.println("");
		out.println("    // jQuery chaining");
		out.println("    return this.each(function() {");
		out.println("");
		out.println("      // Transform to HTML");
		out.println("      var html = json2html(json, options);");
		out.println("      if (options.rootCollapsable && isCollapsable(json)) {");
		out.println("        html = '<a href class=\"json-toggle\"></a>' + html;");
		out.println("      }");
		out.println("");
		out.println("      // Insert HTML in target DOM element");
		out.println("      $(this).html(html);");
		out.println("      $(this).addClass('json-document');");
		out.println("");
		out.println("      // Bind click on toggle buttons");
		out.println("      $(this).off('click');");
		out.println("      $(this).on('click', 'a.json-toggle', function() {");
		out.println("        var target = $(this).toggleClass('collapsed').siblings('ul.json-dict, ol.json-array');");
		out.println("        target.toggle();");
		out.println("        if (target.is(':visible')) {");
		out.println("          target.siblings('.json-placeholder').remove();");
		out.println("        } else {");
		out.println("          var count = target.children('li').length;");
		out.println("          var placeholder = count + (count > 1 ? ' items' : ' item');");
		out.println("          target.after('<a href class=\"json-placeholder\">' + placeholder + '</a>');");
		out.println("        }");
		out.println("        return false;");
		out.println("      });");
		out.println("");
		out.println("      // Simulate click on toggle button when placeholder is clicked");
		out.println("      $(this).on('click', 'a.json-placeholder', function() {");
		out.println("        $(this).siblings('a.json-toggle').click();");
		out.println("        return false;");
		out.println("      });");
		out.println("");
		out.println("      if (options.collapsed == true) {");
		out.println("        // Trigger click to collapse all nodes");
		out.println("        $(this).find('a.json-toggle').click();");
		out.println("      }");
		out.println("    });");
		out.println("  };");
		out.println("})(jQuery);");
		out.println("");
		out.println("</script>");
    }

	public static void run(PrintWriter out, String json) {
		run(out, null, json);
	}


	public static void run(PrintWriter out, String title, String json) {
		out.println("<!doctype HTML>");
		out.println("<html>");
		out.println("  <head>");
		out.println("    <title>jQuery json-viewer</title>");
		out.println("    <meta charset=\"utf-8\" />");
		out.println("    <script src=\"https://code.jquery.com/jquery-3.3.1.min.js\"></script>");

		writeScript(out);

		out.println("");
		out.println("    <style type=\"text/css\">");
		out.println("body {");
		out.println("  margin: 0 100px;");
		out.println("  font-family: sans-serif;");
		out.println("}");
		out.println("p.options label {");
		out.println("  margin-right: 10px;");
		out.println("}");
		out.println("p.options input[type=checkbox] {");
		out.println("  vertical-align: middle;");
		out.println("}");
		out.println("textarea#json-input {");
		out.println("  width: 100%;");
		out.println("  height: 200px;");
		out.println("}");
		out.println("pre#json-renderer {");
		out.println("  border: 1px solid #aaa;");
		out.println("}");
		out.println("    </style>");
		out.println("");
		out.println("    <script>");
		out.println("$(function() {");
		out.println("  function renderJson() {");
		out.println("    try {");
		out.println("      var input = eval('(' + $('#json-input').val() + ')');");
		out.println("    }");
		out.println("    catch (error) {");
		out.println("      return alert(\"Cannot eval JSON: \" + error);");
		out.println("    }");
		out.println("    var options = {");
		out.println("      collapsed: $('#collapsed').is(':checked'),");
		out.println("      rootCollapsable: $('#root-collapsable').is(':checked'),");
		out.println("      withQuotes: $('#with-quotes').is(':checked'),");
		out.println("      withLinks: $('#with-links').is(':checked')");
		out.println("    };");
		out.println("    $('#json-renderer').jsonViewer(input, options);");
		out.println("  }");
		out.println("");
		out.println("  // Generate on click");
		out.println("  $('#btn-json-viewer').click(renderJson);");
		out.println("");
		out.println("  // Generate on option change");
		out.println("  $('p.options input[type=checkbox]').click(renderJson);");
		out.println("");
		out.println("  // Display JSON sample on page load");
		out.println("  renderJson();");
		out.println("});");
		out.println("    </script>");
		out.println("  </head>");
		out.println("  <body>");

		if (title == null) {
			out.println("    <h1><a href=\"https://github.com/abodelot/jquery.json-viewer\">jQuery json-viewer</a></h1>");
	    } else {
		    out.println("    <h1><a href=\"https://github.com/abodelot/jquery.json-viewer\">jQuery json-viewer</a></h1>");
	    }
		out.println("    <textarea id=\"json-input\" autocomplete=\"off\">");
		out.println(json);
		out.println("</textarea>");
		out.println("    <p class=\"options\">");
		out.println("      Options:");
		out.println("      <label title=\"Generate node as collapsed\">");
		out.println("        <input type=\"checkbox\" id=\"collapsed\">Collapse nodes");
		out.println("      </label>");
		out.println("      <label title=\"Allow root element to be collasped\">");
		out.println("        <input type=\"checkbox\" id=\"root-collapsable\" checked>Root collapsable");
		out.println("      </label>");
		out.println("      <label title=\"Surround keys with quotes\">");
		out.println("        <input type=\"checkbox\" id=\"with-quotes\">Keys with quotes");
		out.println("      </label>");
		out.println("      <label title=\"Generate anchor tags for URL values\">");
		out.println("        <input type=\"checkbox\" id=\"with-links\" checked>");
		out.println("        With Links");
		out.println("      </label>");
		out.println("    </p>");
		out.println("    <button id=\"btn-json-viewer\" title=\"run jsonViewer()\">Transform to HTML</button>");
		out.println("    <pre id=\"json-renderer\"></pre>");
		out.println("  </body>");
		out.println("</html>");
	}

    public static void run(String htmlfile, String json) {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(htmlfile, "UTF-8");
            run(pw, json);
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + htmlfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
    }

    public static String flattern(Vector v) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.trim();
			buf.append(line).append(" ");
		}
		return buf.toString();
	}

    public static void main(String[] args) {
		String jsonfile = args[0];
		String htmlfile = args[1];
		Vector w = Utils.readFile(jsonfile);
		String json = flattern(w);
		System.out.println(json);
		JSONViewerTableGenerator.run(htmlfile, json);
	}
}