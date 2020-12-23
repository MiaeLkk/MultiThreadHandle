package utils;

public class PrintUtil {
	public static String printExce(Exception e) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\r\n\t").append(e.toString()).append("\r\n");
		
		for(StackTraceElement ele : e.getStackTrace()) {
			sb.append("\t\t").append(ele.toString()).append("\r\n");
		}
		
		return sb.toString();
	}
}
