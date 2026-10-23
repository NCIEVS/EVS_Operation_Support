package gov.nih.nci.evs.restapi.util;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.util.*;

public class NCItReleaseSchedule {
	static String ALPHABETICS = "abcdefg";
	static String NCI_THESAURUS_URI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus";

	static int PORT = 3030;

	static String DEV_TIER = "DEV";
	static String QA_TIER = "QA";
	static String STAGE_TIER = "STAGE";
	static String PROD_TIER = "PROD";

	static String DEV_SVR = "d2175";
	static String QA_SVR = "q294";
	static String STAGE_SVR = "s308";
	static String PROD_SVR = "p335";

	static String[] SERVERS = new String[] {DEV_SVR, QA_SVR, STAGE_SVR, PROD_SVR};

	static String NCIT2 = "NCIT2";
	static String CTRP = "CTRP";

	public static HashMap tier2SvrMap = null;

	static {
		tier2SvrMap = new HashMap();
		tier2SvrMap.put(DEV_TIER, DEV_SVR);
		tier2SvrMap.put(QA_TIER, QA_SVR);
		tier2SvrMap.put(STAGE_TIER, STAGE_SVR);
		tier2SvrMap.put(PROD_TIER, PROD_SVR);
	}

	public static String getNCItMonthlyVersion() {
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
		return getNCItMonthlyVersion(currentYear, currentMonth);
	}

    public static String getNCIThesaurusGraphName(int year, int month) {
		 String version = getNCItMonthlyVersion(year, month);
		 return NCI_THESAURUS_URI + "_" + version + ".owl";
	}

    public static String getNCIThesaurusGraphName() {
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
		String version = getNCItMonthlyVersion(currentYear, currentMonth);
		return NCI_THESAURUS_URI + "_" + version + ".owl";
	}

	public static HashMap getReleaseScheduleHashMap(int year) {
		Vector w = getNCItReleaseSchedule(year);
		HashMap hmap = new HashMap();
		for (int i=1; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			hmap.put((String) u.elementAt(0) + "|" + (String) u.elementAt(1), line);
		}
		return hmap;
	}

    public static String getCurrNCItVersion() {
		int currentYear = DateUtils.getCurrentYear();
		int currentMonth = DateUtils.getCurrentMonth();
		int weekOfMonth = DateUtils.getWeekOfMonth()-1;
	    String version = getNCItVersion(currentYear, currentMonth, weekOfMonth);
	    return version;
	}

	public static String getNCItVersion(int year, int month, int week) {
		char c = ALPHABETICS.charAt(week);
		StringBuffer buf = new StringBuffer();
		String year_str = "" + year;
		String t = year_str.substring(2, 4);
		buf.append(t).append(".");
		if (month < 10) {
			buf.append("0").append("" + month);
			buf.append(c);
		} else {
			buf.append("" + month);
			buf.append(c);
		}
		return buf.toString();
	}

    public static String getNCIThesaurusGraphName(int year, int month, int week) {
		 String version = getNCItVersion(year, month, week);
		 return NCI_THESAURUS_URI + "_" + version + ".owl";
	}


    public static Vector getNCItReleaseSchedule(int year) {
		Vector w = new Vector();
		w.add("Year|Month|Version|Graph Name|Release Data");
		for (int i=1; i<=12; i++) {
			String version = getNCItMonthlyVersion(year, i);
			String graphName = getNCIThesaurusGraphName(year, i);
			w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getMonthlyNCItReleaseDate(year, i));
		}
		return w;
	}

	public static String getMonthlyNCItReleaseDate(int year, int month) {
		int n = DateUtils.getLastWeekdayOfAMonth(year, month, "Monday");
		StringBuffer buf = new StringBuffer();
		if (month < 10) {
			buf.append("0");
		}
        buf.append(month).append("/");
		if (n < 10) {
			buf.append("0");
		}
        buf.append(n).append("/");
        buf.append(year);
        return buf.toString();
	}

	public static Vector getNCItReleaseSchedule(int year, boolean monthlyOnly) {
		Vector w = new Vector();
		w.add("Year|Month|Version|Graph Name|Release Data");
		if (monthlyOnly) {
			for (int i=1; i<=12; i++) {
				String version = getNCItMonthlyVersion(year, i);
				String graphName = getNCIThesaurusGraphName(year, i);
				w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getMonthlyNCItReleaseDate(year, i));
			}
		} else {
			String weekDay = "Monday";
			for (int i=1; i<=12; i++) {
				Vector weekday_vec = DateUtils.getWeekdayOfAMonth(year, i, weekDay);
				for (int j=0; j<weekday_vec.size(); j++) {
					Integer int_obj = (Integer) weekday_vec.elementAt(j);
					String version = getNCItVersion(year, i, j);
					String graphName = getNCIThesaurusGraphName(year, i, j);
					w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getNCItReleaseDate(year, i, j));
				}

			}
		}
		return w;
	}

	public Vector getReleaseSchedule(int year, int month) throws Exception {
        Vector w = new Vector();
		String weekDay = "Monday";
		Vector weekday_vec = DateUtils.getWeekdayOfAMonth(year, month, weekDay);
		for (int j=0; j<weekday_vec.size(); j++) {
			Integer int_obj = (Integer) weekday_vec.elementAt(j);
			String version = getNCItVersion(year, month, j);
			String graphName = getNCIThesaurusGraphName(year, month, j);
			w.add("" + year + "|" + DateUtils.getMonthString(month) + "|" + version + "|" + graphName + "|" + getNCItReleaseDate(year, month, j));
		}
        return w;
	}

	public Vector getCurrentMonthReleaseSchedule() throws Exception {
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		return getReleaseSchedule(year, month);
	}

	public Vector getPreviousMonthReleaseSchedule() throws Exception {
		int year = DateUtils.getCurrentYear();
		int month = DateUtils.getCurrentMonth();
		if (month == 1) {
			year = year - 1;
			month = 12;
		} else {
			month = month - 1;
		}
		return getReleaseSchedule(year, month);
	}

	public static String getNCItMonthlyVersion(int year, int month) {
		int n = DateUtils.getNumberOfMondaysInAMonth(year, month);
		char c = ALPHABETICS.charAt(n-1);
		StringBuffer buf = new StringBuffer();
		String year_str = "" + year;
		String t = year_str.substring(2, 4);
		buf.append(t).append(".");
		if (month < 10) {
			buf.append("0").append("" + month);
			buf.append(c);
		} else {
			buf.append("" + month);
			buf.append(c);
		}
		return buf.toString();
	}

	public static Vector getNCItReleaseSchedule(int year, boolean monthlyOnly, String svr) {
		Vector w = new Vector();
		w.add("Year|Month|Version|Graph Name|Release Data|Service URL");
		String serviceUrla = generateServiceUrl(svr, true);
		String serviceUrlb = generateServiceUrl(svr, false);
		if (monthlyOnly) {
			for (int i=1; i<=12; i++) {
				String version = getNCItMonthlyVersion(year, i);
				String graphName = getNCIThesaurusGraphName(year, i);
				w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getMonthlyNCItReleaseDate(year, i) + "|" + serviceUrlb);
			}
		} else {
			String weekDay = "Monday";
			for (int i=1; i<=12; i++) {
				Vector weekday_vec = DateUtils.getWeekdayOfAMonth(year, i, weekDay);
				for (int j=0; j<weekday_vec.size(); j++) {
					Integer int_obj = (Integer) weekday_vec.elementAt(j);
					String version = getNCItVersion(year, i, j);
					String graphName = getNCIThesaurusGraphName(year, i, j);
					w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getNCItReleaseDate(year, i, j) + "|" + serviceUrla);
					if (j == weekday_vec.size()-1) {
						w.add("" + year + "|" + DateUtils.getMonthString(i) + "|" + version + "|" + graphName + "|" + getNCItReleaseDate(year, i, j) + "|" + serviceUrlb);
					}
				}
			}
		}
		return w;
	}

    public static String generateServiceUrl(String svr, boolean monthly) {
		String svr_id = NCIT2;
		if (monthly) {
			svr_id = CTRP;
		}
		return "http://ncias-" + svr + "-c.nci.nih.gov:" + PORT + "/" + svr_id + "/query";
	}

	public static Vector run(int year, String tier, boolean monthlyOnly) {
		String svr = (String) tier2SvrMap.get(tier);
        Vector v = getNCItReleaseSchedule(year, monthlyOnly, svr);
        return v;
	}

	public static String getNCItReleaseDate(int year, int month, int n) {
		String weekDay = "Monday";
		Vector weekday_vec = DateUtils.getWeekdayOfAMonth(year, month, weekDay);
		Integer int_obj = (Integer) weekday_vec.elementAt(n);
		n = int_obj.intValue();

		StringBuffer buf = new StringBuffer();
		if (month < 10) {
			buf.append("0");
		}
        buf.append(month).append("/");
		if (n < 10) {
			buf.append("0");
		}
        buf.append(n).append("/");
        buf.append(year);
        return buf.toString();
	}

	public static void main(String[] args) {
		String yearStr = args[0];
		int year = Integer.parseInt(yearStr);
		String tier = args[1];
		String boolStr = args[2];
		boolean monthlyOnly = true;
		if (boolStr.compareToIgnoreCase("false") == 0) {
			monthlyOnly = false;
		}
        Vector v = run(year, tier, monthlyOnly);
        Utils.saveToFile("NICT_release_schedule_" + year + ".txt", v);
	}
}
