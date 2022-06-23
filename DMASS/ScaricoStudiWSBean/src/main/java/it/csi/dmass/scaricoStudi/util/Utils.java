package it.csi.dmass.scaricoStudi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {
	
 public static String normalizeClassName(String classname){
		if (classname == null) return null;
		int pos = classname.lastIndexOf('.');
		if (pos >= 0) return classname.substring(pos+1);
		return classname;
	}
 
	public static final Date DT_NULL = Utils.toDate("01/01/1000");
	public static final Timestamp TS_NULL = Utils.toTimestamp("01/01/1000 00:00:00");
	
	public static final Date DT_NOT_NULL = Utils.toDate("01/01/1800");
	public static final Timestamp TS_NOT_NULL = Utils.toTimestamp("01/01/1800 00:00:00");

	public final static long SECOND_MILLIS = 1000;
 public final static long MINUTE_MILLIS = SECOND_MILLIS*60;
 public final static long HOUR_MILLIS = MINUTE_MILLIS*60;
 public final static long DAY_MILLIS = HOUR_MILLIS*24;
 public final static long YEAR_MILLIS = DAY_MILLIS*365;
 
 public final static String SEPARATORE_COLLOCAZIONE_FSE = "-";
 public final static String SEPARATORE_COLLOCAZIONE_PUA = "@";
 
 private static final int INITIAL_SIZE = 1024 * 1024;
 private static final int BUFFER_SIZE = 1024;

	public static final Pattern CF_REGEX = Pattern.compile("^[A-Za-z]{6}[0-9LMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST]{1}[0-9LMNPQRSTUV]{2}[A-Za-z]{1}[0-9LMNPQRSTUV]{3}[A-Za-z]{1}$");
	public static final Pattern CF_11_REGEX = Pattern.compile("^[0-9]*$");
 public static String concatenate(String ... str) {
 	String conStr = "";
 	
 	if(str != null) {
 		
 		for (String el : str) {
 			
 			if (isNotEmpty(el)) {
 				conStr += " " + el;
 			}
   }
 	}
 	
 	return conStr.trim();
 }
 
 public static String replaceDescrizioneCollocazionePUA(String descCollocazionePUA, String oldChar,String newChar) {
	 
	 if(descCollocazionePUA!=null) {
		 descCollocazionePUA = descCollocazionePUA.replace(oldChar, newChar);
	 }
	 
	 return descCollocazionePUA;
 }
	
 
 public static String getValueFromHeader(NodeList nodeList, String value) {
		if(nodeList != null && nodeList.getLength() > 0) {
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				
				if(node.getLocalName() != null) {
					if(node.getLocalName().equals(value)) {
		            	return node.getTextContent();
		            }
		            
		            String valueFound =  getValueFromHeader(node.getChildNodes(), value);
		            if(valueFound != null) {
		            	return valueFound;
		            }
				}
		    }
		}
		return null;
	}
 
 
	public static long dateDiff(Date date1, Date date2, long umes) {
		
		if (date1 == null) {
			date1 = sysdate();
		}		
		
		if (date2 == null) {
			date2 = sysdate();
		}

		long elapsed = (date1.getTime() / umes) - (date2.getTime() / umes);
		return elapsed;
	}
	
	
	public static String unaccent(String src) {
		if(src == null)
			return null;
		
		
		
		src = src.replace("\u00E0", "a'");
		src = src.replace("\u00E1", "a'");
		src = src.replace("\u00E2", "a'");
		src = src.replace("\u00E3", "a'");
		src = src.replace("\u00E4", "a'");
		src = src.replace("\u00E5", "a'");
		src = src.replace("\u00E8", "e'");
		src = src.replace("\u00E9", "e'");
		src = src.replace("\u00EA", "e'");
		src = src.replace("\u00EB", "e'");
		src = src.replace("\u00EC", "i'");
		src = src.replace("\u00ED", "i'");
		src = src.replace("\u00EE", "i'");
		src = src.replace("\u00EF", "i'");
		src = src.replace("\u00F2", "o'");
		src = src.replace("\u00F3", "o'");
		src = src.replace("\u00F4", "o'");
		src = src.replace("\u00F5", "o'");
		src = src.replace("\u00F6", "o'");
		src = src.replace("\u00F9", "u'");
		src = src.replace("\u00FA", "u'");
		src = src.replace("\u00FB", "u'");
		src = src.replace("\u00FC", "u'");
		
		src = src.replace("\u00C0", "A'");
		src = src.replace("\u00C1", "A'");
		src = src.replace("\u00C2", "A'");
		src = src.replace("\u00C3", "A'");
		src = src.replace("\u00C4", "A'");
		src = src.replace("\u00C5", "A'");
		src = src.replace("\u00C8", "E'");
		src = src.replace("\u00C9", "E'");
		src = src.replace("\u00CA", "E'");
		src = src.replace("\u00CB", "E'");
		src = src.replace("\u00CC", "I'");
		src = src.replace("\u00CD", "I'");
		src = src.replace("\u00CE", "I'");
		src = src.replace("\u00CF", "I'");
		src = src.replace("\u00D2", "O'");
		src = src.replace("\u00D3", "O'");
		src = src.replace("\u00D4", "O'");
		src = src.replace("\u00D5", "O'");
		src = src.replace("\u00D6", "O'");
		src = src.replace("\u00D9", "U'");
		src = src.replace("\u00DA", "U'");
		src = src.replace("\u00DB", "U'");
		src = src.replace("\u00DC", "U'");
		
		
		
//		U+00C0	�	0192	&Agrave;	Latin Capital Letter A with grave	0128
//		U+00C1	�	0193	&Aacute;	Latin Capital letter A with acute	0129
//		U+00C2	�	0194	&Acirc;	Latin Capital letter A with circumflex	0130
//		U+00C3	�	0195	&Atilde;	Latin Capital letter A with tilde	0131
//		U+00C4	�	0196	&Auml;	Latin Capital letter A with diaeresis	0132
//		U+00C5	�	0197	&Aring;	Latin Capital letter A with ring above	0133
//		U+00C8	�	0200	&Egrave;	Latin Capital letter E with grave	0136
//		U+00C9	�	0201	&Eacute;	Latin Capital letter E with acute	0137
//		U+00CA	�	0202	&Ecirc;	Latin Capital letter E with circumflex	0138
//		U+00CB	�	0203	&Euml;	Latin Capital letter E with diaeresis	0139
//		U+00CC	�	0204	&Igrave;	Latin Capital letter I with grave	0140
//		U+00CD	�	0205	&Iacute;	Latin Capital letter I with acute	0141
//		U+00CE	�	0206	&Icirc;	Latin Capital letter I with circumflex	0142
//		U+00CF	�	0207	&Iuml;	Latin Capital letter I with diaeresis	0143
//		U+00D2	�	0210	&Ograve;	Latin Capital letter O with grave	0146
//		U+00D3	�	0211	&Oacute;	Latin Capital letter O with acute	0147
//		U+00D4	�	0212	&Ocirc;	Latin Capital letter O with circumflex	0148
//		U+00D5	�	0213	&Otilde;	Latin Capital letter O with tilde	0149
//		U+00D6	�	0214	&Ouml;	Latin Capital letter O with diaeresis	0150
//		U+00D9	�	0217	&Ugrave;	Latin Capital letter U with grave	0153
//		U+00DA	�	0218	&Uacute;	Latin Capital letter U with acute	0154
//		U+00DB	�	0219	&Ucirc;	Latin Capital Letter U with circumflex	0155
//		U+00DC	�	0220	&Uuml;	Latin Capital Letter U with diaeresis	0156
//		
		
//		src = src.replace("�", "a' ");
//		src = src.replace("�", "e' ");
//		src = src.replace("�", "e' ");
//		src = src.replace("�", "i' ");
//		src = src.replace("�", "o' ");
//		src = src.replace("�", "u' ");
		
		
//		224	00e0	�	340	0xE0	0xC3,0xA0	&agrave;	a grave
//		225	00e1	�	341	0xE1	0xC3,0xA1	&aacute;	a acute
//		226	00e2	�	342	0xE2	0xC3,0xA2	&acirc;	a circumflex
//		227	00e3	�	343	0xE3	0xC3,0xA3	&atilde;	a tilde
//		228	00e4	�	344	0xE4	0xC3,0xA4	&auml;	a diaeresis
//		229	00e5	�	345	0xE5	0xC3,0xA5	&aring;	a ring above, a ring

//		232	00e8	�	350	0xE8	0xC3,0xA8	&egrave;	e grave
//		233	00e9	�	351	0xE9	0xC3,0xA9	&eacute;	e acute
//		234	00ea	�	352	0xEA	0xC3,0xAA	&ecirc;	e circumflex
//		235	00eb	�	353	0xEB	0xC3,0xAB	&euml;	e diaeresis
		
//		236	00ec	�	354	0xEC	0xC3,0xAC	&igrave;	i grave
//		237	00ed	�	355	0xED	0xC3,0xAD	&iacute;	i acute
//		238	00ee	�	356	0xEE	0xC3,0xAE	&icirc;	i circumflex
//		239	00ef	�	357	0xEF	0xC3,0xAF	&iuml;	i diaeresis

//		242	00f2	�	362	0xF2	0xC3,0xB2	&ograve;	o grave
//		243	00f3	�	363	0xF3	0xC3,0xB3	&oacute;	o acute
//		244	00f4	�	364	0xF4	0xC3,0xB4	&ocirc;	o circumflex
//		245	00f5	�	365	0xF5	0xC3,0xB5	&otilde;	o tilde
//		246	00f6	�	366	0xF6	0xC3,0xB6	&ouml;	o diaeresis

//		249	00f9	�	371	0xF9	0xC3,0xB9	&ugrave;	u grave
//		250	00fa	�	372	0xFA	0xC3,0xBA	&uacute;	u acute
//		251	00fb	�	373	0xFB	0xC3,0xBB	&ucirc;	u circumflex
//		252	00fc	�	374	0xFC	0xC3,0xBC	&uuml;	u diaeresis
//		
		
		
		
       
		
		
		return Normalizer
				.normalize(src, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
	}
	
	public static String extractAfter(String str, String fnd) {
		
		if(isNotEmpty(str) && isNotEmpty(str)) {
			int index = str.indexOf(fnd);
			
			String extStr = str.substring(index + fnd.length());
			return extStr;
		}
		
		return null;
	}
	
	public static String emptyToNull(String str) {
  return (isEmpty(str) ? null : str);
 }

	public static boolean isPositiveNumber(Number nbr) {
		
		if(nbr != null) {
			return nbr.longValue() >= 0;
		}
		
		return false;
	}
	
	public static String checkAndDefault(String str, String defStr) {
		
		if(isEmpty(str)) {
			return defStr;
		}
		
		return str;
	}
	
	public static <E> void addToListNoDuplicate(List<E> list, E elem) {
		if(list != null && !list.contains(elem)) {
			list.add(elem);
		}		
	}
	
	public static boolean equalsOrDefault(String str, String defVal) {
		
		if(isNotEmpty(str)) {
			return str.equalsIgnoreCase(defVal);
		}
		
		return true;
	}

	public static String toString(Object obj) {
		return (obj != null ? obj.toString() : "");
	}
	
	public static <E> boolean isNotEmptyList(List<E> lst) {
		return (lst != null && lst.size() > 0);
	}

	public static <E> boolean isEmptyList(List<E> lst) {
		return (lst == null || lst.size() == 0);
	}

 public static byte[] saveFile(InputStream is, String filePath, String fileName) {
		byte[] buffer = null;
		
		try {
			buffer = IOUtils.toByteArray(is);
			saveFile(buffer, filePath, fileName);
		}
		catch (Exception e) {
			// nothing to do
		}
		
		return buffer;
	}
	
	public static void saveFile(byte[] content, String filePath, String fileName) {
		if(filePath != null) {
			try {
				File f = new File(filePath + "/" + fileName);
				OutputStream out = new FileOutputStream(f);
				
				out.write(content);
				out.close();
			}
			catch (Exception e) {
				// nothing to do
			}
		}	
	}

	public static int findStrInArray(String sFnd, String...array) {
		
		if(sFnd == null || array == null){
			return -1;
		}
		
		List<String> lst = Arrays.asList(array);
		int index = lst.indexOf(sFnd);
		
		return index;
	}
	
	public static <T>void copyObject(T orig, T dest) {
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			// nothing to do
		}
	}

	public static <T>void copyObjectWithConverter(T orig, T dest) {
		try {
		 org.apache.commons.beanutils.converters.SqlTimestampConverter conv1 = 
		  new org.apache.commons.beanutils.converters.SqlTimestampConverter(null);

		 org.apache.commons.beanutils.ConvertUtils.register(conv1, java.sql.Timestamp.class);

		 org.apache.commons.beanutils.converters.SqlDateConverter conv2 =
		 new org.apache.commons.beanutils.converters.SqlDateConverter(null);

		 org.apache.commons.beanutils.ConvertUtils.register(conv2, java.sql.Date.class);

		 org.apache.commons.beanutils.converters.LongConverter conv3 =
		  new org.apache.commons.beanutils.converters.LongConverter(null);

		 org.apache.commons.beanutils.ConvertUtils.register(conv3, java.lang.Long.class);

		 BeanUtils.copyProperties(dest, orig);			
		}
		catch (Exception e) {
			// nothing to do
		}
	}
	
	public static <T>T cloneObject(T orig) {
		T copy = null;
		
		try {
			copy = cloneX(orig);
		}
		catch (Exception e) {
			// nothing to do
		}
		
		return copy;
	}
	
	public static boolean isEmpty(String str) {
		return (str == null || str.trim().length() == 0);
	}

	public static boolean isNotEmpty(String str) {
		return (str != null && str.trim().length() > 0);
	}
	
	private static <T>T cloneX(T x) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		CloneOutput cout = new CloneOutput(bout);
		cout.writeObject(x);
		
		byte[] bytes = bout.toByteArray();
		
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		CloneInput cin = new CloneInput(bin, cout);
		
		T clone = (T) cin.readObject();
		return clone;
	}
	
	private static class CloneOutput extends ObjectOutputStream {
		Queue<Class<?>> classQueue = new LinkedList<Class<?>>();
		
		CloneOutput(OutputStream out) throws IOException {
			super(out);
		}
		
		@Override
		protected void annotateClass(Class<?> c) {
			classQueue.add(c);
		}
		
		@Override
		protected void annotateProxyClass(Class<?> c) {
			classQueue.add(c);
		}
	}
	
	private static class CloneInput extends ObjectInputStream {
		private final CloneOutput output;
		
		CloneInput(InputStream in, CloneOutput output) throws IOException {
			super(in);
			this.output = output;
		}
		
		@Override
		protected Class<?> resolveClass(ObjectStreamClass osc) throws IOException,
			ClassNotFoundException {
			Class<?> c = output.classQueue.poll();
			String expected = osc.getName();
			String found = (c == null) ? null : c.getName();
			
			if (!expected.equals(found)) {
				throw new InvalidClassException("Classes desynchronized: " + "found "
					+ found + " when expecting " + expected);
			}
			return c;
		}
		
		@Override
		protected Class<?> resolveProxyClass(String[] interfaceNames)
			throws IOException, ClassNotFoundException {
			return output.classQueue.poll();
		}
	}
	
	public static Long toLong(String val) {
		try {
			return Long.valueOf(val);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Float toFloat(String val) {
		try {
			return Float.valueOf(val);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static <T> T getFirstRecord(List<T> elenco) {
		
		if (elenco != null && elenco.size() > 0) {
			return elenco.get(0);
		}
		
		return null;
	}
	
	public static boolean listIsEmpty(List<?> elenco) {
		return (elenco == null || elenco.size() == 0);
	}
	
	public static boolean equals(String str1, String str2) {
		
		if (str1 != null && str2 != null) {
			return str1.equalsIgnoreCase(str2);
		}
		
		return false;
	}
	
	
	private static final String DT_COMPLEX = "dd/MM/yyyy HH:mm:ss";
	private static final String DT_SIMPLE = "dd/MM/yyyy";

	public static SimpleDateFormat getComplexDateFormat() {
		return createPatternDateFormat(DT_COMPLEX);
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		return createPatternDateFormat(DT_SIMPLE);
	}
	
	public static java.sql.Date toSqlDate(Timestamp time) {
		try {
			java.sql.Date date = new java.sql.Date(time.getTime());
			return date;			
		}
		catch (Exception e) {
			return null;
		}
	}

	public static java.sql.Date toSqlDate(Date date) {
		try {
			return new java.sql.Date(date.getTime());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Date onlyData(Date date) {
		try {
			Date dt = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
			return dt;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Date toDate(Timestamp tmp) {
		try {
			Date date = new Date(tmp.getTime());
			return date;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static String fromYYYYMMDDHHMMSStoDDMMYYYY(String date) {
		SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat myFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN);

		String reformattedStr = null;
		try {

			reformattedStr = myFormat.format(fromUser.parse(date));
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return reformattedStr;
	}

	public static Date toDate(String value) {
		try {
			return getSimpleDateFormat().parse(value);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Date toComplexDate(String value) {
		try {
			return getComplexDateFormat().parse(value);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Timestamp addDays(Timestamp date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); 
        return new Timestamp(cal.getTime().getTime());

    }
	
	public static Timestamp sysdate() {
		return new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	/**
	 * Converte da yyyy-MM-dd HH:mm:ss in formato dd/MM/yyyy
	 * Se il parametro e' vuoto, ritorna la data attuale convertita
	 * @param data
	 * @return
	 */
	public static String formatddMMyyyy(String data) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String reformattedStr = null;
		if(data == null || data.isEmpty()) {
			Date date = new Date();  
			return formatter.format(date);
		}
		try {
			reformattedStr = formatter.format(fromUser.parse(data));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reformattedStr;
	}
	
	
	/**
	 * Converte da dd/MM/yyyy HH:mm:ss in un Date
	 * Se il parametro e' vuoto, ritorna la data attuale convertita
	 * @param data
	 * @return
	 */
	public static Date formatyyyyMMddDate(String data) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ITALY);
	    Date myDate = null;
	    if(data == null || data.isEmpty()) {
			myDate = new Date();  
		} else {
			try {
				myDate = dateFormat.parse(data);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return myDate;
	}
	
	/**
	 * Converte da dd/MM/yyyy HH:mm:ss in formato yyyy-MM-dd HH:mm:ss
	 * Se il parametro e' vuoto, ritorna la data attuale convertita
	 * @param data
	 * @return
	 */
	public static String formatyyyyMMddHHmmss(String data) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat fromUser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String reformattedStr = null;
		if(data == null || data.isEmpty()) {
			Date date = new Date();  
			return formatter.format(date);
		}
		try {
			reformattedStr = formatter.format(fromUser.parse(data));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reformattedStr;
	}

	public static Date formatStringToyyyyMMddHHmmss(String data) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return formatter.parse(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Timestamp toTimestamp(Date dt, long hour  ) {
		try {
			long mstoadd = 1000 * 60 * 60 * hour;
			return new Timestamp(dt.getTime() + mstoadd);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static byte[] toBytes(DataHandler dh) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream(INITIAL_SIZE);
	    InputStream in = dh.getInputStream();
	    byte[] buffer = new byte[BUFFER_SIZE];
	    int bytesRead;
	    while ( (bytesRead = in.read(buffer)) >= 0 ) {
	        bos.write(buffer, 0, bytesRead);
	    }
	    return bos.toByteArray();
	}
	
	public static Date toDate(XMLGregorianCalendar calendar){
        if(calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }
	
	public static XMLGregorianCalendar toXmlGregorianCalendar(final Date date) {
        return toXmlGregorianCalendarFinal(date.getTime());
    }
	
	public static XMLGregorianCalendar toXmlGregorianCalendarFinal(final long date) {
		
		XMLGregorianCalendar res = null;
		
        try {
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);
            res =  DatatypeFactory.newInstance().newXMLGregorianCalendar(
                calendar);
        }
        catch (final DatatypeConfigurationException ex) {
            System.out.println("Unable to convert date '%s' to an XMLGregorianCalendar object");
        }
		return res;
    }
	
	public static Timestamp toTimestamp(Date dt) {
		try {
			return new Timestamp(dt.getTime());
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Timestamp toTimestamp(String value) {
		try {
			Date dt = getComplexDateFormat().parse(value);
			return toTimestamp(dt);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Timestamp toTimestampFromStringYYYYMMDDHHmmss(String value) {
		try {
		
			String anno = value.substring(0, 4);
			String mese = value.substring(4,6 );
			String giorno = value.substring(6, 8);
			String ora = value.substring(8, 10);
			String minuti = value.substring(10, 12);
			String secondi = value.substring(12, 14);
			
			SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat(
	                "yyyy-MM-dd hh:mm:ss.SSS");
			Date lFromDate1 = datetimeFormatter1.parse(anno +"-"+ mese +"-"+ giorno +" "+ ora +":"+ minuti +":"+ secondi +"."+"000");
			System.out.println("gpsdate :" + lFromDate1);
			Timestamp fromTS1 = new Timestamp(lFromDate1.getTime());
			
			return fromTS1;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	
	
	public static String dateToString(java.util.Date data, boolean complex) {
		try {
			SimpleDateFormat df = getSimpleDateFormat();
			
			if (complex) {
				df = getComplexDateFormat();
			}
			
			String dateStr = df.format(data);
			return dateStr;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static java.sql.Date removeTime(java.sql.Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return new java.sql.Date(cal.getTime().getTime());
	}
	
	public static String dateToString(java.util.Date data, String pattern) {
		try {
			SimpleDateFormat df = null;
			
			if (DT_SIMPLE.equalsIgnoreCase(pattern)) {
				df = getSimpleDateFormat();
			}
		 else if (DT_COMPLEX.equalsIgnoreCase(pattern)) {
				df = getComplexDateFormat();
			}
		 else {
				df = createPatternDateFormat(pattern);
		 }

			String dateStr = df.format(data);
			return dateStr;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static SimpleDateFormat createPatternDateFormat(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf;
	}
	
	public static boolean equals(Object obj1, Object obj2) {
		
		if (obj1 == null) {
			return (obj2 == null);
		}
		
		if (obj2 == null) {
			return false;
		}
		
		return obj1.equals(obj2);
	}
	
	public static <E> String listToString(List<E> lst, String attrs) {
		String concStr = listToString(lst, attrs, " ", " ");
		return concStr.trim();
	}
	
	public static <E> String listToString(List<E> lst, 
	                                      String attrs,
																																							String sepElem) {
		String concStr = listToString(lst, attrs, " ", sepElem);
		return concStr.trim();
	}

	public static <E> String listToString(List<E> lst, 
	                                      String attrs,
																																							String sepInfo, 
																																							String sepElem) {
		String rsStr = "";
		
		if (isNotEmptyList(lst)) {
			Class eCls = lst.get(0).getClass();
			String[] attrsLst = attrs.split(",");
			
			Map<String, Method> pMap = createMethodsMap(eCls, attrsLst);
			Iterator<E> iterator = lst.iterator();
			
			for (int j =0 ; iterator.hasNext(); ++j) {
				E elem = iterator.next();
				
				if(elem != null) {
					
					for (int i = 0; i < attrsLst.length; ++i) {
						String attrName = attrsLst[i];
						Method method = pMap.get(attrName);
						
						try {
							Object attrVal = method.invoke(elem);
							
							if (attrVal != null) {
								rsStr += attrVal;
							}
						}
						catch (Exception e) {
						}
						
						if (i != attrsLst.length - 1) {
							rsStr += sepInfo;
						}
					}
					
					if (j != lst.size() - 1) {
						rsStr += sepElem;
					}					
				}	
			}
		}
		
		return rsStr;
	}
	
	private static Map<String, Method> createMethodsMap(Class cls,
																																																					String[] attrsLst) {
		Map<String, Method> pMap = new HashMap<String, Method>();
		
		for (int i = 0; i < attrsLst.length; ++i) {
			String attrName = attrsLst[i];
			Method met = findGetMethodByName(cls, attrName);
			
			pMap.put(attrName, met);
		}
		
		return pMap;
	}
	
	public static <T> Method findGetMethodByName(Class<T> cls, String attrName) {
		Method method = null;
		
		try {
			method = findMethodByName(cls, "get" + attrName);
		}
		catch (Exception e) {
		}
		
		return method;
	}
	
	public static Object invokeMethod(Object obj, String methodName,
																																			InvokeParam...params) {
		try {
			Class[] cTypes = null;
			Object[] cValues = null;
			
			if (params != null) {
				List<Class> pTypes = new ArrayList<Class>();
				List<Object> pValues = new ArrayList<Object>();
				
				for (InvokeParam param : params) {
					
					if (param != null) {
						Class pCls = param.getCls();
						pTypes.add(pCls);
						
						Object pVal = param.getValue();
						pValues.add(pVal);
					}
				}
				
				cTypes = pTypes.toArray(new Class[pTypes.size()]);
				cValues = pValues.toArray(new Object[pValues.size()]);
			}
			
			Class cls = obj.getClass();
			Method method = cls.getDeclaredMethod(methodName, cTypes);
			
			return method.invoke(obj, cValues);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static Method findMethodByName(Class cls, String methName) {
		Method methFound = null;
		Method[] methods = cls.getMethods();
		
		for (int i = 0; i < methods.length; i++) {
			String metName = methods[i].getName();
			
			if (metName.compareToIgnoreCase(methName) == 0) {
				methFound = methods[i];
				break;
			}
		}
		
		return methFound;
	}
	
	public static Object getValueByCmpx(Object obj, String cmpx) {
		
		if (obj != null) {
			
			if (isEmpty(cmpx)) {
				return null;
			}
			
			Object obj2 = obj;
			StringTokenizer tkn = new StringTokenizer(cmpx, ".");
			
			if (tkn.countTokens() > 0) {
				
				while (tkn.hasMoreTokens()) {
					String attrName = tkn.nextToken();
					obj2 = invokeGetMethod(obj2, attrName);
					
					if (obj2 == null) {
						break;
					}
				}
			}
			else {
				obj2 = invokeGetMethod(obj, cmpx);
			}
			
			return obj2;
		}
		
		return null;
	}
	
	public static Object invokeGetMethod(Object obj, String attrName) {
		String tMetName = "invokeGetMethod";
		Object cvoAttrValue = null;
		
		try {
			Method method = findMethodByName(obj, "get" + attrName);
			
			if (method != null) {
				Class retCls = method.getReturnType();
				
				if (retCls.isInstance(java.util.Date.class.newInstance())) {
					java.util.Date data = (java.util.Date) method.invoke(obj);
					cvoAttrValue = dateToString(data, false);
				}
				else {
					cvoAttrValue = method.invoke(obj);
				}
			}
		}
		catch (Exception e) {
		}
		
		return cvoAttrValue;
	}
	
	public static Method findMethodByName(Object obj, String methName) {
		return findClassMethodByName(obj.getClass(), methName);
	}
	
	public static Method findClassMethodByName(Class cls, String methName) {
		Method methFound = null;
		Method[] methods = cls.getMethods();
		
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().compareToIgnoreCase(methName) == 0) {
				methFound = methods[i];
				break;
			}
		}
		
		return methFound;
	}
	
	public static Method findMethodByName(Object obj, String methName, Class parameter) {
		Method methFound = null;
		Method[] methods = obj.getClass().getMethods();
		
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().compareToIgnoreCase(methName) == 0) {
				Class[] parameters = methods[i].getParameterTypes();
				
				if (parameters != null && parameters.length == 1
					&& parameters[0].isAssignableFrom(parameter)) {
					methFound = methods[i];
					break;
				}
			}
		}
		
		return methFound;
	}
	
	public static class InvokeParam {
		private Class cls = null;
		private Object value = null;
		
		public InvokeParam(Class cls, Object value) {
			this.cls = cls;
			this.value = value;
  }		
		
		public Class getCls() {
			return cls;
		}
		
		public void setCls(Class cls) {
			this.cls = cls;
		}
		
		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}
	}
	

	public static boolean checkCodiceFiscale(String codiceFiscale) {
        
		if(codiceFiscale != null) {
	        Matcher m = CF_REGEX.matcher(codiceFiscale);
	        Matcher m_11 = CF_11_REGEX.matcher(codiceFiscale);
			if(codiceFiscale.length() == 16 && m.matches()) {
				return true;
			}
			if(codiceFiscale.length() == 11 && m_11.matches()) {
				return true;
			}
		}
		return false;
	}
	
	public static String importo2decimal(Float importo) {
		String result = String.valueOf(importo);
		if(result.contains(".") ) {
			if(result.indexOf(".")+3 > result.length()) {
				result=aggiungiStringa(result, "0", result.length()-(result.indexOf(".")+1));
			} else {
				result=result.substring(0, result.indexOf(".")+3);
			}
		}
		  
		
		return result;
	}
	
	public static String aggiungiStringa(String stringa, String valore, int numeroValori) {
		for(int i=0; i<numeroValori; i++) {
			stringa = stringa+valore;
		}
		return stringa;
	}
	
	public static String xmlMessageFromObject(Object obj) {
		String xmlString = null;
		if (obj != null) {
			try {
				JAXBContext context = JAXBContext.newInstance(obj.getClass());
				Marshaller m = context.createMarshaller();

				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				StringWriter sw = new StringWriter();
				m.marshal(obj, sw);
				xmlString = sw.toString();

			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		return xmlString;
	}

	public static Date yesterdayMidnightDate(){
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		cal.add(Calendar.DATE, -1);
		return cal.getTime();

	}

	public static Timestamp yesterdayMidnightTimestamp(){
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		cal.add(Calendar.DATE, -1);
		return new Timestamp(cal.getTimeInMillis());

	}

	public static Timestamp toTimestampByString(String value) {
		String character = value.substring(2, 3);
		if ("/".equals(character) || "-".equals(character) || "\\".equals(character)) {
			try {
				Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyHHmmss2).parse(value);
				return toTimestamp(dt);
			} catch (Exception excepti) {
				try {
					Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyyyHH).parse(value);
					return toTimestamp(dt);
				} catch (Exception exceptio) {
					try {
						Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyyyHH2).parse(value);
						return toTimestamp(dt);
					} catch (Exception exception) {
						try {
							Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyyyHHmmss).parse(value);
							return toTimestamp(dt);
						} catch (Exception exception1) {
							try {
								Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyyyHHmmssSSS)
										.parse(value);
								return toTimestamp(dt);
							} catch (Exception exception2) {
								try {
									Date dt = createPatternDateFormat(Constants.PATTERN_DATE_ddMMyyyyHHmmssSSS2)
											.parse(value);
									return toTimestamp(dt);
								} catch (Exception e) {
									return null;
								}
							}
						}
					}
				}
			}
		} else {
			try {
				Date dt = getComplexDateFormatWithDash().parse(value);
				return toTimestamp(dt);
			} catch (Exception e) {
				try {
					Date dt = getComplexDateFormatWithSlash().parse(value);
					return toTimestamp(dt);
				} catch (Exception ex) {
					try {
						Date dt = getSimpleDateFormatWithDash().parse(value);
						return toTimestamp(dt);
					} catch (Exception exc) {
						try {
							Date dt = getSimpleDateFormatWithSlash().parse(value);
							return toTimestamp(dt);
						} catch (Exception exce) {
							try {
								Date dt = getComplexDateFormat_yyyyMMddHHmmssSSS_WithDash().parse(value);
								return toTimestamp(dt);
							} catch (Exception excep) {
								try {
									Date dt = getComplexDateFormat_yyyyMMddHHmmssSSS_WithSlash().parse(value);
									return toTimestamp(dt);
								} catch (Exception except) {
									return null;
								}
							}
						}
					}
				}
			}
		}
	}

	public static SimpleDateFormat getComplexDateFormatWithDash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHHmmss2);
	}

	public static SimpleDateFormat getComplexDateFormatWithSlash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHHmmss);
	}

	public static SimpleDateFormat getSimpleDateFormatWithDash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHH2);
	}

	public static SimpleDateFormat getSimpleDateFormatWithSlash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHH);
	}

	public static SimpleDateFormat getComplexDateFormat_yyyyMMddHHmmssSSS_WithSlash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHHmmssSSS2);
	}

	public static SimpleDateFormat getComplexDateFormat_yyyyMMddHHmmssSSS_WithDash() {
		return createPatternDateFormat(Constants.PATTERN_DATE_yyyyMMddHHmmssSSS);
	}

	public static int getAge(Date dateOfBirth) {

		Calendar today = Calendar.getInstance();
		Calendar birthDate = Calendar.getInstance();

		int age = 0;

		birthDate.setTime(dateOfBirth);
		if (birthDate.after(today)) {
			throw new IllegalArgumentException("Can't be born in the future");
		}

		age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

		// If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year
		if ( (birthDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) > 3) ||
				(birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH ))){
			age--;

			// If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
		}else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH )) &&
				(birthDate.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH ))){
			age--;
		}

		return age;
	}

	public static boolean isMinorenne(Date dataDiNascita){
		boolean isMinorenne = false;
		int age = Utils.getAge(dataDiNascita);
		if(age < 18) isMinorenne = true;
		return isMinorenne;
	}
	
	public static Boolean isMaggiorenne(Date dataNascita) {
		Boolean isMagg = false;
		if (dataNascita != null) {

			GregorianCalendar dataDiNascita = new GregorianCalendar();
			dataDiNascita.setTime(dataNascita);

			GregorianCalendar dataMagg = new GregorianCalendar();

			int anno = dataMagg.get(GregorianCalendar.YEAR) - dataDiNascita.get(GregorianCalendar.YEAR);
			int mese = dataMagg.get(GregorianCalendar.MONTH) - dataDiNascita.get(GregorianCalendar.MONTH);
			int giorno = dataMagg.get(GregorianCalendar.DAY_OF_MONTH) - dataDiNascita.get(GregorianCalendar.DAY_OF_MONTH);

			isMagg = anno>18 || (anno==18 && (mese>0 || (mese==0 && giorno>=0))) ;
		}
		return isMagg;
	}

}

