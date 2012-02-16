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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GetHttpResponse {

    public static String getRespone(String s, int p, boolean b, String r, int timeout) throws Exception {
        String sz_ret = "";
        if (!b) {
            try {
                Socket sock = new Socket(s, p);
                sock.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(sock.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out.print(r);
                out.flush();
                String ret = "";
                while ((ret = in.readLine()) != null) {
                    sz_ret += ret + "\r\n";
                }
                out.close();
                in.close();
                sock.close();
                return sz_ret;
            } catch (Exception e) {
                throw e;
            }
        } else {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                SocketFactory factory = sc.getSocketFactory();
                Socket sock = factory.createSocket(s, p);
                sock.setSoTimeout(timeout);
                PrintWriter out = new PrintWriter(sock.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out.print(r);
                out.flush();
                String ret = "";
                while ((ret = in.readLine()) != null) {
                    sz_ret += ret + "\r\n";
                }
                out.close();
                in.close();
                sock.close();
                return sz_ret;
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
