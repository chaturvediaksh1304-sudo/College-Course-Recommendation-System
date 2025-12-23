package com.example.coursereco.desktop;

import java.util.ArrayList;
import java.util.List;

public class JsonMini {

    // ---------- Students ----------
    public static List<StudentItem> parseStudents(String json) {
        List<StudentItem> out = new ArrayList<>();
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return out;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return out;

        String[] objs = s.split("\\},\\s*\\{");
        for (String obj : objs) {
            String o = obj;
            if (!o.startsWith("{")) o = "{" + o;
            if (!o.endsWith("}")) o = o + "}";

            long id = safeLong(getRawValue(o, "id"));
            String name = getString(o, "name");
            String major = getString(o, "major");
            String year = getString(o, "year");
            out.add(new StudentItem(id, name, major, year));
        }
        return out;
    }

    // ---------- Agent Recommendations ----------
    public static List<AgentReco> parseAgentRecos(String json) {
        List<AgentReco> out = new ArrayList<>();
        String s = json.trim();
        if (!s.startsWith("[") || !s.endsWith("]")) return out;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return out;

        String[] objs = s.split("\\},\\s*\\{");
        for (String obj : objs) {
            String o = obj;
            if (!o.startsWith("{")) o = "{" + o;
            if (!o.endsWith("}")) o = o + "}";

            String code = getString(o, "courseCode");
            String title = getString(o, "title");
            int credits = (int) safeLong(getRawValue(o, "credits"));
            double score = safeDouble(getRawValue(o, "score"));
            String agentReason = getString(o, "agentReason");
            String whyFitMajor = getString(o, "whyFitMajor");
            String whyFitInterests = getString(o, "whyFitInterests");

            out.add(new AgentReco(code, title, credits, score, agentReason, whyFitMajor, whyFitInterests));
        }
        return out;
    }

    // ---------- helpers ----------
    private static String getRawValue(String json, String key) {
        int i = json.indexOf("\"" + key + "\"");
        if (i < 0) return "";
        int colon = json.indexOf(":", i);
        int end = json.indexOf(",", colon);
        if (end < 0) end = json.indexOf("}", colon);
        return json.substring(colon + 1, end).trim();
    }

    private static String getString(String json, String key) {
        String s = getRawValue(json, key);
        return s.replaceAll("^\\s*\"|\"\\s*$", "");
    }

    private static long safeLong(String s) {
        try { return Long.parseLong(s.replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private static double safeDouble(String s) {
        try { return Double.parseDouble(s.replace("\"","").trim()); }
        catch (Exception e) { return 0; }
    }

    // ---------- types ----------
    public record StudentItem(long id, String name, String major, String year) {
        @Override public String toString() { return id + " — " + name + " (" + major + ", " + year + ")"; }
    }

    public record AgentReco(String courseCode, String title, int credits, double score,
                            String agentReason, String whyFitMajor, String whyFitInterests) {}
}
