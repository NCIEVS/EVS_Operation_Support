package gov.nih.nci.evs.restapi.util;
import gov.nih.nci.evs.restapi.config.*;
import gov.nih.nci.evs.restapi.bean.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
/*
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.commons.text.*;
import org.apache.poi.ss.usermodel.IndexedColors;
*/

public class ReflectionUtils {
    private final Field[] fields;
    private final Method[] methods;
    private final Constructor<?>[] constructors;
    private final Object object;

    public ReflectionUtils(final Object object) {
        this.object = object;
        final var objectClass = object.getClass();
        this.fields = objectClass.getDeclaredFields();
        this.methods = objectClass.getDeclaredMethods();
        this.constructors = objectClass.getConstructors();
        Arrays.stream(fields).forEach(f -> f.setAccessible(true));
    }

    ////////////////////////////////////////////////////////////////////////////////
    private static String getConstructorSimpleName(final Constructor<?> c) {
        final var array = c.getName().split("\\.");
        return array[array.length - 1];
    }

    private static String getParameterTypes(final Method m) {
        return Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
    }

    private static String getReturnType(final Method m) {
        return m.getReturnType().getSimpleName();
    }

    private static String getConstructorParamTypes(final Constructor<?> c) {
        return Arrays.stream(c.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", "));
    }

    ////////////////////////////////////////////////////////////////////////////////
    public String getClassName() {
		return object.getClass().getSimpleName();
	}

    public Vector getFieldNames() {
		Vector v = new Vector();
        for (final var f : fields) {
            try {
				v.add(f.getName());
            } catch (Exception e) {
                System.err.printf("Error accessing field %s: %s%n", f.getName(), e.getMessage());
            }
        }
        return v;
    }

    public HashMap getFieldValues() {
		HashMap hmap = new HashMap();
        for (final var f : fields) {
            try {
				String key = f.getName();
				String value = (String) f.get(object);
				if (value == null || value.compareTo("null") == 0) {
					value = "";
				}
				hmap.put(key, value);
            } catch (Exception e) {
                System.err.printf("Error accessing field %s: %s%n", f.getName(), e.getMessage());
            }
        }
        return hmap;
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void generateReflectionRunner(String parentClassName, String methodSignature) {
		int n = methodSignature.indexOf("(");
		String outputfile = methodSignature.substring(0, n) + ".java";

		String firstChar = outputfile.substring(0, 1);
		outputfile = firstChar.toUpperCase() + outputfile.substring(1, outputfile.length());

		PrintWriter pw = null;
    	try{
    	    pw = new PrintWriter(outputfile);
            generateReflectionRunner(pw, parentClassName, methodSignature);

    	} catch(IOException e){
    		e.printStackTrace();
    	} finally {
			try {
				pw.close();
				System.out.println(outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

    public static void generateReflectionRunner(PrintWriter out, String parentClassName, String methodSignature) {
		int n = methodSignature.indexOf("(");
		String methodName = methodSignature.substring(0, n);
		String firstChar = methodName.substring(0, 1);
		String className = firstChar.toUpperCase() + methodName.substring(1, methodName.length());
		String t = methodSignature.substring(n+1, methodSignature.length()-1);
		Vector u = null;
		Vector type_vec= new Vector();
		Vector param_vec = new Vector();
		if (t.length() > 0) {
			u = StringUtils.parseData(t, ',');
			if (u.size() > 0) {
				for (int i=0; i<u.size(); i++) {
					String s = (String) u.elementAt(i);
					s = s.trim();
					Vector u2 = StringUtils.parseData(s, ' ');
					String type = (String) u2.elementAt(0);
					String param = (String) u2.elementAt(1);
					type_vec.add(type);
					param_vec.add(param);
				}
			}
		}

		out.println("import java.lang.Class;");
		out.println("import java.lang.reflect.*;");
		out.println("import gov.nih.nci.evs.restapi.util.*;");

		out.println("");
		out.println("class " + className + " {");
		out.println("");
		out.println("	public static void main(String[] args) {");
		for (int i=0; i<param_vec.size(); i++) {
			String type = (String) type_vec.elementAt(i);
			String param = (String) param_vec.elementAt(i);
			if (type.compareTo("int") == 0) {
				out.println("		String " + param + "Str = args[" + i + "];");
				out.println("		int " + param + " = Integer.parseInt(" + param + "Str" + ");");
			} else {
				out.println("		String " + param + " = args[" + i + "];");
			}
		}
		out.println("		try {");
		out.println("			Class<?> clazz = " + parentClassName + ".class;");

		StringBuffer buf = new StringBuffer();
		for (int k=0; k<type_vec.size(); k++) {
			String type = (String) type_vec.elementAt(k);
			buf.append(", ").append(type + ".class");
		}
		String s = buf.toString();

		out.println("			Method method = clazz.getMethod(\"" + methodName + "\"" + s + ");");
		out.println("			" + parentClassName + " instance = new " + parentClassName + "();");
		if (param_vec.size() == 0) {
			out.println("			method.invoke(instance);");
		} else {
			buf = new StringBuffer();
			for (int k=0; k<param_vec.size(); k++) {
				String type = (String) type_vec.elementAt(k);
				String param = (String) param_vec.elementAt(k);
				if (type.compareTo("String[]") == 0) {
					param = "(Object) Utils.vector2Array(StringUtils.parseData(" + param + ", '|'))";
				}
				buf.append(param);
				if (k<param_vec.size()-1) {
					buf.append(", ");
				}
			}
			s = buf.toString();
			out.println("			method.invoke(instance, " + s + ");");
		}
		out.println("		} catch (Exception e) {");
		out.println("			e.printStackTrace();");
		out.println("		}");
		out.println("	}");
		out.println("}");
    }
}