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
package com.sensepost.jbaah.utility;

import com.sensepost.jbaah.exceptions.FileMethodException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class FileAndIO {

    // <editor-fold defaultstate="collapsed" desc="Private Class Variables">
    // Directory stuff
    private String sz_homedir;
    // Delimiter stuff
    private String sz_delimiter;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Class Instantiation">
    public FileAndIO() {

        // Instantiate variables...
        this.sz_homedir = this.GetHomeDirectory();
        this.sz_delimiter = this.GetFileDelimiter();
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Private Class Methods">
    // Returns the user's home directory
    private String GetHomeDirectory() {
        String sz_ret = System.getProperty("user.home");
        return sz_ret;
    }

    // Returns the file-delimiter
    private String GetFileDelimiter() {
        String sz_ret = "" + File.separatorChar;
        return sz_ret;
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Public Class Variables">
    public String UserHome() {
        return this.sz_homedir;
    }

    public boolean ArrayToFile(String s, ArrayList a) throws FileMethodException {
        try {
            FileWriter w = new FileWriter(s);
            BufferedWriter b = new BufferedWriter(w);
            for (int i = 0; i < a.size(); i++) {
                b.write(a.get(i).toString() + "\r\n");
            }
            b.close();
            w.close();
            return true;
        } catch (Exception ex) {
            throw new FileMethodException("Could not write output to file: " + ex.getMessage(), 1);
        }
    }

    public String FileToString(File f_file) throws FileMethodException {
        String sz_file_contents = "";
        try {
            FileReader the_reader = new FileReader(f_file);
            BufferedReader bufRead = new BufferedReader(the_reader);
            String line;
            line = bufRead.readLine();
            while (line != null) {
                sz_file_contents += line + "\r\n";
                line = bufRead.readLine();
            }
            bufRead.close();
            the_reader.close();
        } catch (Exception ex) {
            throw new FileMethodException("Unable to read file: " + ex.getMessage(), 1);
        }
        return sz_file_contents;
    }

    public ArrayList FileToList(File f_file) throws FileMethodException {
        ArrayList file_contents = new ArrayList();
        try {
            FileReader the_reader = new FileReader(f_file);
            BufferedReader bufRead = new BufferedReader(the_reader);
            String line;
            line = bufRead.readLine();
            while (line != null) {
                file_contents.add(line);
                line = bufRead.readLine();
            }
            bufRead.close();
            the_reader.close();
        } catch (Exception ex) {
            throw new FileMethodException("Unable to read file: " + ex.getMessage(), 1);
        }
        return file_contents;
    }
    // </editor-fold>
}
