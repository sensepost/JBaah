/*
SensePost J-Baah - Generic HTTP Fuzzer

Copyright (C) 2010 SensePost <ian@sensepost.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sensepost.jbaah.engines;

public class GetHttpDiffs {

    public static String CropHeader(String s1) {
        int i = s1.indexOf("\r\n\r\n");
        if (i == -1) {
            i = 0;
        }
        return s1.substring(i);
    }

    public static double getDiffs(String s1, String s2) {
        double d = 0.00000;
        String sz_b = CropHeader(s1);
        String sz_r = CropHeader(s2);
        sz_b = sz_b.replaceAll("\r", "");
        sz_b = sz_b.replaceAll("\n", " ");
        sz_r = sz_r.replaceAll("\r", "");
        sz_r = sz_r.replaceAll("\n", " ");
        if (sz_b.equalsIgnoreCase(sz_r)) {
            return 1.00000;
        }
        String[] ar_base = sz_b.split(" ");
        String[] ar_resp = sz_r.split(" ");
        int score = 0;
        for (int b = 0; b < ar_base.length; b++) {
            for (int r = 0; r < ar_resp.length; r++) {
                if (ar_base[b].toLowerCase().compareTo(ar_resp[r].toLowerCase()) == 0) {
                    score++;
                    break;
                }
            }
        }
        d = ((double) score * (double) 2) / (double) ((double) ar_base.length + (double) ar_resp.length);
        return d;
        //return new Double(f.format(d)).doubleValue();
    }

    public static String getContent(String s1, String s2, String s3) {
        try {
            String ret = "";
            if ((s1.length() == 0) || (s2.length() == 0)) {
                return "No content to extract";
            }
            s3 = s3.replaceAll("\r", "").replaceAll("\n", "");
            int i = 0;
            boolean sf = false;
            while (i < (s3.length() - s1.length())) {
                if (s3.substring(i, i + s1.length()).compareTo(s1) == 0) {
                    sf = true;
                    i += s1.length();
                    break;
                }
                i++;
            }
            int j = i;
            boolean ef = false;
            while (j < (s3.length() - s2.length())) {
                if (s3.substring(j, j + s2.length()).compareTo(s2) == 0) {
                    ef = true;
                    break;
                }
                j++;
            }
            if (sf) {
                if (ef) {
                    ret = s3.substring(i, j);
                } else {
                    ret = s3.substring(i);
                }
            } else {
                ret = "No content to extract";
            }
            return ret;
        } catch (Exception e) {
            return "No content to extract";
        }
    }
}
