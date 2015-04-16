package com.dgmltn.sonnet;

/**
 * Created by doug on 4/1/15. Static utils used throughout the app.
 */
public class Utils {

	public static String xmlEnocode(CharSequence text) {
		StringBuilder out = new StringBuilder();
		int start = 0;
		int end = text.length();

		for (int i = start; i < end; i++) {
			char c = text.charAt(i);

			if (c == '<') {
				out.append("&lt;");
			}
			else if (c == '>') {
				out.append("&gt;");
			}
			else if (c == '&') {
				out.append("&amp;");
			}
			else if (c == '"') {
				out.append("&quot;");
			}
			else if (c == '\'') {
				out.append("&apos;");
			}
			else {
				out.append(c);
			}
		}

		return out.toString();
	}

}
