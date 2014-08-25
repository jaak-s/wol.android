package org.po.wol.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.po.wheeloflife.R;

import android.content.res.Resources;

public class World {
	private static final Pattern linePattern = Pattern
			.compile("L\\(([^)]+)\\)");
	private static final Pattern parameterPattern = Pattern
			.compile("(-?[\\d.]+)");

	private List<Line> lines;
	private Resources resources;

	public World(Resources resources) {
		this.resources = resources;
	}

	public List<Line> getLines() {
		return lines;
	}

	public void load() {
		InputStreamReader in = new InputStreamReader(resources.openRawResource(R.raw.world));
		
		lines = new ArrayList<Line>();
		BufferedReader r = null;
		try {
			r = new BufferedReader(in);
			String fileLine;
			while ((fileLine = r.readLine()) != null) {
				// remove comments starting with hash
				fileLine = fileLine.replaceAll("#.*", "");
				parseFileLine(fileLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (r != null) {
			try {
				r.close();
			} catch (IOException e) {
			}
		}
	}

	private void parseFileLine(String fileLine) {
		Matcher m = linePattern.matcher(fileLine);
		while (m.find()) {
			parseParameters(m.group(1));
		}
	}

	private void parseParameters(String params) {
		Matcher m = parameterPattern.matcher(params);
		float[] p = new float[4];
		int i = 0;
		while (m.find() && i < p.length) {
			try {
				p[i++] = parseParam(m.group(1));
			} catch (Exception e) {
			}
		}
		if (p[0] < p[1]) {
			lines.add(new Line(p[0], p[1], p[2], p[3]));
		} else {
			lines.add(new Line(p[0], 361, p[2], p[3]));
			lines.add(new Line(-1, p[1], p[2], p[3]));
		}
	}

	private float parseParam(String paramString) {
		return Float.parseFloat(paramString.trim());
	}
}