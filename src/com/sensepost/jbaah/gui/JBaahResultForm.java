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

public class JBaahResultForm extends javax.swing.JFrame {

    public JBaahResultForm(String Response) {
        initComponents();
        this.txt_response.setText(Response);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_response = new javax.swing.JPanel();
        spn_response = new javax.swing.JScrollPane();
        txt_response = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("View Results");

        txt_response.setColumns(20);
        txt_response.setRows(5);
        spn_response.setViewportView(txt_response);

        org.jdesktop.layout.GroupLayout pnl_responseLayout = new org.jdesktop.layout.GroupLayout(pnl_response);
        pnl_response.setLayout(pnl_responseLayout);
        pnl_responseLayout.setHorizontalGroup(
            pnl_responseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spn_response, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        pnl_responseLayout.setVerticalGroup(
            pnl_responseLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(spn_response, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnl_response, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnl_response, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnl_response;
    private javax.swing.JScrollPane spn_response;
    private javax.swing.JTextArea txt_response;
    // End of variables declaration//GEN-END:variables
}
