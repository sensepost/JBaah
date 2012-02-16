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
package com.sensepost.jbaah.gui;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class SplashScreen extends JWindow {

    public SplashScreen(int time) {

        try {

            URL imageURL = this.getClass().getResource("splash/splash.png");
            ImageIcon ii = new ImageIcon(imageURL);
            JScrollPane jsp = new JScrollPane(new JLabel(ii));
            getContentPane().add(jsp);
            setSize(550, 315);
            centerScreen();
            setVisible(true);
            if (time != 0) {
                try {
                    Thread.sleep(time);
                    dispose();
                } catch (InterruptedException ie) {
                }
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    private void centerScreen() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((d.getWidth() - getWidth()) / 2);
        int y = (int) ((d.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }
}
