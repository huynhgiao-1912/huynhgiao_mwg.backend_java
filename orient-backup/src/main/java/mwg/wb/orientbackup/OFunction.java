package mwg.wb.orientbackup;

import java.util.Arrays;

public class OFunction {
	public String code;
	public String name;
	public String[] parameters;
	public String language;

	public boolean isIdentical(OFunction other) {
		return other.code.equals(code) && other.language.equals(language)
				&& Arrays.equals(other.parameters, parameters);
	}
}
