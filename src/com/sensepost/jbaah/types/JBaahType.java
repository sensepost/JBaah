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
package com.sensepost.jbaah.types;

public class JBaahType {

    private String sz_host;
    private int n_port;
    private boolean b_ssl;
    private String sz_outer;
    private String sz_inner;
    private String sz_req;
    private String sz_res;
    private double n_score;
    private String sz_content;

    public JBaahType() {
        this.sz_host = "";
        this.n_port = -1;
        this.b_ssl = false;
        this.sz_outer = "";
        this.sz_inner = "";
        this.sz_req = "";
        this.sz_res = "";
        this.n_score = 0;
        this.sz_content = "";
    }

    public JBaahType(String h, int p, boolean b, String o, String i) {
        this.sz_host = h;
        this.n_port = p;
        this.b_ssl = b;
        this.sz_outer = o;
        this.sz_inner = i;
        this.sz_req = "";
        this.sz_res = "";
        this.n_score = 0;
        this.sz_content = "";
    }

    public String getHost() {
        return this.sz_host;
    }

    public int getPort() {
        return this.n_port;
    }

    public boolean getSsl() {
        return this.b_ssl;
    }

    public String getOuterLoop() {
        return this.sz_outer;
    }

    public String getInnerLoop() {
        return this.sz_inner;
    }

    public String getRequest() {
        return this.sz_req;
    }

    public String getResponse() {
        return this.sz_res;
    }

    public double getScore() {
        return this.n_score;
    }

    public String getContent() {
        return this.sz_content;
    }

    public void setRequest(String s) {
        this.sz_req = s;
    }

    public void setResponse(String s) {
        this.sz_res = s;
    }

    public void setScore(double f) {
        this.n_score = f;
    }

    public void setContent(String s) {
        this.sz_content = s;
    }
}
