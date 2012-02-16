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

import com.sensepost.jbaah.gui.JBaahMain;
import com.sensepost.jbaah.types.JBaahType;

public class JBaah extends Thread {

    private JBaahMain the_main;
    private String sz_host;
    private int n_port;
    private String sz_base;
    private boolean b_ssl;
    private String sz_cs;
    private String sz_ce;

    public JBaah(JBaahMain m, String h, int p, boolean s) {
        this.the_main = m;
        this.sz_host = h;
        this.n_port = p;
        this.b_ssl = s;
        this.sz_base = this.the_main.getBaseResponse();
        this.sz_cs = this.the_main.getContentStart();
        this.sz_ce = this.the_main.getContentEnd();
    }

    @Override
    public synchronized void run() {
        while (this.the_main.getIsRunning() && !this.the_main.getQueueEmpty()) {
            try {
                JBaahType cbt = (JBaahType) this.the_main.getQueueItem();
                int n = 0;
                boolean b = true;
                while (b) {
                    String r = "";
                    try {
                        r = GetHttpResponse.getRespone(this.sz_host, this.n_port, this.b_ssl, cbt.getRequest(), this.the_main.getTimeOut());
                        cbt.setResponse(r);
                        // Now, we have to get the diffs...
                        double dif = GetHttpDiffs.getDiffs(this.sz_base, r);
                        cbt.setScore(dif);
                        // Now we have to extract the content...
                        r = GetHttpDiffs.getContent(this.sz_cs, this.sz_ce, r);
                        cbt.setContent(r);
                        this.the_main.addResult(cbt);
                        b = false;
                    } catch (Exception e) {
                        n++;
                        if (n < this.the_main.getRetry()) {
                            cbt.setResponse("");
                            cbt.setContent("Timeout and Retry count exceeded");
                            cbt.setScore(9.00000);
                            this.the_main.addResult(cbt);
                            b = false;
                        } else {
                            b = true;
                        }
                        r = "";
                    }
                }
            } catch (Exception e) {
            }
        }
        try {
            this.join(5);
        } catch (Exception e) {
        }
    }
}
