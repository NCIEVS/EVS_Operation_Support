package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class ClassRunner {

	public ClassRunner() {

	}

	public static void run(String className, String methodName) {
		try {
			Class params[] = {};
			Object paramsObj[] = {};
			Class thisClass = Class.forName(className);
			Object iClass = thisClass.newInstance();
			Method thisMethod = thisClass.getDeclaredMethod(methodName, params);
			thisMethod.invoke(iClass, paramsObj);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String className = args[0];
		String methodName = args[1];
		run(className, methodName);
	}
}

