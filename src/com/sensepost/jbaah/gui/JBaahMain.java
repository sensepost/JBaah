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

import com.sensepost.jbaah.types.JBaahResults;
import com.sensepost.jbaah.exceptions.FileMethodException;
import com.sensepost.jbaah.engines.JBaah;
import com.sensepost.jbaah.engines.GetHttpResponse;
import com.sensepost.jbaah.types.JBaahType;
import com.sensepost.jbaah.utility.FileAndIO;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

public class JBaahMain extends javax.swing.JFrame {

    // Private Class Vars
    private FileAndIO fio;
    private boolean IsRunning;
    private String sz_host;
    private int n_port;
    private boolean b_ssl;
    private boolean v1;
    private boolean v2;
    private Queue AttackQueue;
    private Queue ResultQueue;
    private ArrayList ResultList;
    private String BaseResponse;
    private JBaahResults listmodel;
    private String sz_stat;
    private int n_total;
    private boolean IsReporterRunning;

    // Public GETTERS and SETTERS
    public synchronized boolean getIsRunning() {
        return this.IsRunning;
    }

    public synchronized boolean getQueueEmpty() {
        return this.AttackQueue.isEmpty();
    }

    public synchronized Object getQueueItem() {
        return this.AttackQueue.poll();
    }

    public synchronized int getRetry() {
        return Integer.parseInt(this.spi_retries.getValue().toString());
    }

    public synchronized int getTimeOut() {
        return Integer.parseInt(this.spi_timeout.getValue().toString());
    }

    public synchronized void addResult(JBaahType c) {
        this.ResultQueue.add(c);
    }

    public String getContentStart() {
        return this.txt_contentstart.getText();
    }

    public String getContentEnd() {
        return this.txt_contentend.getText();
    }

    private void FilterValues() {
        this.listmodel.clear();
        this.listmodel.addElement("SCORE  :  PARAMETER 1  :  PARAMETER 2  :  CONTENT");
        for (int i = 0; i < ResultList.size(); i++) {
            JBaahType c = (JBaahType) ResultList.get(i);
            AddResultItem(c);
        }
        this.listmodel.setUpdate();
    }

    private synchronized void AddResultItem(JBaahType c) {
        DecimalFormat f = new DecimalFormat("#.#####");
        Double l = Double.valueOf(f.format(Double.valueOf(spi_low.getValue().toString())));
        Double h = Double.valueOf(f.format(Double.valueOf(spi_high.getValue().toString())));
        Double r = Double.valueOf(f.format(c.getScore()));
        String s = f.format(r) + "  :  " + c.getOuterLoop() + "  :  " + c.getInnerLoop() + "  :  " + c.getContent();
        if (rdo_eq.getSelectedObjects() != null) {
            if (r.compareTo(l) == 0) {
                listmodel.addElement(s);
            }
        } else if (rdo_ne.getSelectedObjects() != null) {
            if (r.compareTo(l) != 0) {
                listmodel.addElement(s);
            }
        } else if (rdo_all.getSelectedObjects() != null) {
            listmodel.addElement(s);
        } else if (rdo_inside.getSelectedObjects() != null) {
            if (r >= l && r <= h) {
                listmodel.addElement(s);
            }
        } else {
            if (r <= l || r >= h) {
                listmodel.addElement(s);
            }
        }
    }

    /** Creates new form CrowBarMain */
    public JBaahMain() {
        this.listmodel = new JBaahResults();
        this.listmodel.addElement("SCORE  :  PARAMETER 1  :  PARAMETER 2  :  CONTENT");
        initComponents();
        spi_low.setModel(new SpinnerNumberModel(0.00000, 0.00000, 9.99999, 0.00001));
        spi_low.setEditor(new JSpinner.NumberEditor(spi_low, "#0.00000"));
        JSpinner.NumberEditor nel = (JSpinner.NumberEditor) spi_low.getEditor();
        nel.getFormat().setMaximumFractionDigits(5);
        nel.getModel().setStepSize(0.00001);
        spi_high.setModel(new SpinnerNumberModel(1.30000, 0.00000, 9.99999, 0.00001));
        spi_high.setEditor(new JSpinner.NumberEditor(spi_high, "#0.00000"));
        JSpinner.NumberEditor neh = (JSpinner.NumberEditor) spi_high.getEditor();
        neh.getFormat().setMaximumFractionDigits(5);
        neh.getModel().setStepSize(0.00001);
        this.fio = new FileAndIO();
        this.IsRunning = false;
        this.v1 = false;
        this.v2 = false;
        this.AttackQueue = new LinkedList<JBaahType>();
        this.ResultList = new ArrayList();
        this.BaseResponse = "";
        this.sz_stat = "";
        this.ResultQueue = new LinkedList<JBaahType>();
        this.n_total = 0;
        this.IsReporterRunning = true;
        SwingWorker updater = new SwingWorker() {

            protected String doInBackground() throws InterruptedException {
                DecimalFormat f = new DecimalFormat("#.#####");
                while (IsReporterRunning) {
                    try {
                        if (AttackQueue.size() == 0 && IsRunning) {
                            btn_baseresponse.setEnabled(true);
                            btn_start.setEnabled(true);
                            btn_stop.setEnabled(false);
                            btn_pause.setEnabled(false);
                            btn_pause.setText("Pause");
                            sz_stat = "DONE";
                            lbl_status.setText(sz_stat + "... Tested #" + ResultList.size() + " of " + n_total);
                            IsRunning = false;
                        } else if (AttackQueue.size() > 0 && !IsRunning) {
                            btn_baseresponse.setEnabled(false);
                            btn_start.setEnabled(false);
                            btn_stop.setEnabled(true);
                            btn_pause.setEnabled(true);
                            btn_pause.setText("Resume");
                            sz_stat = "PAUSED";
                            lbl_status.setText(sz_stat + "... Tested #" + ResultList.size() + " of " + n_total);
                        } else if (AttackQueue.size() == 0 && !IsRunning) {
                            btn_baseresponse.setEnabled(true);
                            btn_start.setEnabled(true);
                            btn_stop.setEnabled(false);
                            btn_pause.setEnabled(false);
                            btn_pause.setText("Pause");
                            sz_stat = "STOPPED";
                            lbl_status.setText(sz_stat + "... Tested #" + ResultList.size() + " of " + n_total);
                        }
                        if (ResultQueue.size() == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                        } else {
                            while (ResultQueue.size() > 0) {
                                JBaahType c = (JBaahType) ResultQueue.poll();
                                String s = f.format(c.getScore()) + "  :  " + c.getOuterLoop() + "  :  " + c.getInnerLoop() + "  :  " + c.getContent();
                                ResultList.add(c);
                                if (chk_ignore.getSelectedObjects() != null) {
                                    listmodel.addElement(s);
                                } else {
                                    AddResultItem(c);
                                }
                                txt_httpres.setText(c.getResponse());
                                lbl_status.setText(sz_stat + "... Tested #" + ResultList.size() + " of " + n_total);
                            }
                            listmodel.setUpdate();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                return "Done";
            }

            @Override
            protected void done() {
            }
        };
        updater.execute();
    }

    // TODO: This will be used when I call it from cmdline... :/
    public JBaahMain(String r, String h, String p, String s, String v1, String v2) {
        this.fio = new FileAndIO();
        this.IsRunning = false;
        this.v1 = false;
        this.v2 = false;
        this.AttackQueue = new LinkedList<JBaahType>();
        this.ResultList = new ArrayList();
        this.BaseResponse = "";
        this.sz_stat = "";
        this.ResultQueue = new LinkedList<JBaahType>();
        this.n_total = 0;
        this.IsReporterRunning = true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mnu_context = new javax.swing.JPopupMenu();
        mni_onlythese = new javax.swing.JMenuItem();
        mni_allbutthese = new javax.swing.JMenuItem();
        mns_component = new javax.swing.JPopupMenu.Separator();
        mni_view = new javax.swing.JMenuItem();
        spn_main = new javax.swing.JSplitPane();
        spn_left = new javax.swing.JSplitPane();
        tpn_httpreqres = new javax.swing.JTabbedPane();
        pnl_httpreq = new javax.swing.JPanel();
        scp_httpreq = new javax.swing.JScrollPane();
        txt_httpreq = new javax.swing.JTextArea();
        pnl_httpres = new javax.swing.JPanel();
        scp_httpres = new javax.swing.JScrollPane();
        txt_httpres = new javax.swing.JTextArea();
        scp_results = new javax.swing.JScrollPane();
        lst_results = new javax.swing.JList();
        spn_right = new javax.swing.JSplitPane();
        tpn_vars = new javax.swing.JTabbedPane();
        pnl_var1 = new javax.swing.JPanel();
        rdo_v1file = new javax.swing.JRadioButton();
        rdo_v1number = new javax.swing.JRadioButton();
        lbl_v1from = new javax.swing.JLabel();
        lbl_v1to = new javax.swing.JLabel();
        txt_v1from = new javax.swing.JTextField();
        txt_v1to = new javax.swing.JTextField();
        lbl_v1file = new javax.swing.JLabel();
        txt_v1file = new javax.swing.JTextField();
        btn_v1browse = new javax.swing.JButton();
        pnl_var2 = new javax.swing.JPanel();
        rdo_v2file = new javax.swing.JRadioButton();
        rdo_v2number = new javax.swing.JRadioButton();
        lbl_v2from = new javax.swing.JLabel();
        lbl_v2to = new javax.swing.JLabel();
        txt_v2from = new javax.swing.JTextField();
        txt_v2to = new javax.swing.JTextField();
        lbl_v2file = new javax.swing.JLabel();
        txt_v2file = new javax.swing.JTextField();
        btn_v2browse = new javax.swing.JButton();
        tpn_control = new javax.swing.JTabbedPane();
        pnl_actions = new javax.swing.JPanel();
        btn_baseresponse = new javax.swing.JButton();
        btn_start = new javax.swing.JButton();
        btn_stop = new javax.swing.JButton();
        btn_pause = new javax.swing.JButton();
        lbl_tc = new javax.swing.JLabel();
        spi_tc = new javax.swing.JSpinner();
        pnl_target = new javax.swing.JPanel();
        lbl_host = new javax.swing.JLabel();
        lbl_port = new javax.swing.JLabel();
        txt_host = new javax.swing.JTextField();
        txt_port = new javax.swing.JTextField();
        lbl_timeout = new javax.swing.JLabel();
        spi_timeout = new javax.swing.JSpinner();
        lbl_retries = new javax.swing.JLabel();
        spi_retries = new javax.swing.JSpinner();
        chk_ssl = new javax.swing.JCheckBox();
        pnl_fuzz = new javax.swing.JPanel();
        chk_ignore = new javax.swing.JCheckBox();
        spi_low = new javax.swing.JSpinner();
        lbl_low = new javax.swing.JLabel();
        lbl_high = new javax.swing.JLabel();
        spi_high = new javax.swing.JSpinner();
        rdo_eq = new javax.swing.JRadioButton();
        rdo_ne = new javax.swing.JRadioButton();
        rdo_all = new javax.swing.JRadioButton();
        rdo_outside = new javax.swing.JRadioButton();
        rdo_inside = new javax.swing.JRadioButton();
        btn_recalc = new javax.swing.JButton();
        pnl_content = new javax.swing.JPanel();
        lbl_contentstart = new javax.swing.JLabel();
        txt_contentstart = new javax.swing.JTextField();
        lbl_contentend = new javax.swing.JLabel();
        txt_contentend = new javax.swing.JTextField();
        pnl_status = new javax.swing.JPanel();
        lbl_status = new javax.swing.JLabel();
        mnu_main = new javax.swing.JMenuBar();
        mnu_file = new javax.swing.JMenu();
        mni_import = new javax.swing.JMenuItem();
        mni_exportlist = new javax.swing.JMenuItem();
        mni_exportall = new javax.swing.JMenuItem();
        mns_file = new javax.swing.JPopupMenu.Separator();
        mni_exit = new javax.swing.JMenuItem();
        mnu_help = new javax.swing.JMenu();
        mni_help = new javax.swing.JMenuItem();

        mni_onlythese.setText("Only These");
        mni_onlythese.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_onlytheseActionPerformed(evt);
            }
        });
        mnu_context.add(mni_onlythese);

        mni_allbutthese.setText("All But These");
        mni_allbutthese.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_allbuttheseActionPerformed(evt);
            }
        });
        mnu_context.add(mni_allbutthese);
        mnu_context.add(mns_component);

        mni_view.setText("View Response");
        mni_view.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_viewActionPerformed(evt);
            }
        });
        mnu_context.add(mni_view);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SensePost J-Baah");
        setName("frm_crowbarmain"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        spn_main.setDividerLocation(400);
        spn_main.setOneTouchExpandable(true);

        spn_left.setDividerLocation(260);
        spn_left.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spn_left.setOneTouchExpandable(true);

        txt_httpreq.setColumns(20);
        txt_httpreq.setRows(5);
        txt_httpreq.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txt_httpreqFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txt_httpreqFocusLost(evt);
            }
        });
        txt_httpreq.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                txt_httpreqInputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        txt_httpreq.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                txt_httpreqPropertyChange(evt);
            }
        });
        txt_httpreq.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_httpreqKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txt_httpreqKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txt_httpreqKeyReleased(evt);
            }
        });
        scp_httpreq.setViewportView(txt_httpreq);

        org.jdesktop.layout.GroupLayout pnl_httpreqLayout = new org.jdesktop.layout.GroupLayout(pnl_httpreq);
        pnl_httpreq.setLayout(pnl_httpreqLayout);
        pnl_httpreqLayout.setHorizontalGroup(
            pnl_httpreqLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 373, Short.MAX_VALUE)
            .add(pnl_httpreqLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(scp_httpreq, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
        );
        pnl_httpreqLayout.setVerticalGroup(
            pnl_httpreqLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 212, Short.MAX_VALUE)
            .add(pnl_httpreqLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(scp_httpreq, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
        );

        tpn_httpreqres.addTab("Request", pnl_httpreq);

        txt_httpres.setColumns(20);
        txt_httpres.setEditable(false);
        txt_httpres.setRows(5);
        txt_httpres.setEnabled(false);
        scp_httpres.setViewportView(txt_httpres);

        org.jdesktop.layout.GroupLayout pnl_httpresLayout = new org.jdesktop.layout.GroupLayout(pnl_httpres);
        pnl_httpres.setLayout(pnl_httpresLayout);
        pnl_httpresLayout.setHorizontalGroup(
            pnl_httpresLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 373, Short.MAX_VALUE)
            .add(pnl_httpresLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(scp_httpres, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE))
        );
        pnl_httpresLayout.setVerticalGroup(
            pnl_httpresLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 212, Short.MAX_VALUE)
            .add(pnl_httpresLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(scp_httpres, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
        );

        tpn_httpreqres.addTab("Response", pnl_httpres);

        spn_left.setLeftComponent(tpn_httpreqres);

        lst_results.setModel(this.listmodel);
        lst_results.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lst_results.setAutoscrolls(false);
        lst_results.setComponentPopupMenu(mnu_context);
        lst_results.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        lst_results.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                lst_resultsFocusGained(evt);
            }
        });
        scp_results.setViewportView(lst_results);

        spn_left.setRightComponent(scp_results);

        spn_main.setLeftComponent(spn_left);

        spn_right.setDividerLocation(260);
        spn_right.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spn_right.setOneTouchExpandable(true);

        pnl_var1.setBorder(javax.swing.BorderFactory.createTitledBorder("Inner Loop"));

        rdo_v1file.setText("File");
        rdo_v1file.setEnabled(false);
        rdo_v1file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_v1fileActionPerformed(evt);
            }
        });

        rdo_v1number.setSelected(true);
        rdo_v1number.setText("Numeric");
        rdo_v1number.setEnabled(false);
        rdo_v1number.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_v1numberActionPerformed(evt);
            }
        });

        lbl_v1from.setText("From:");

        lbl_v1to.setText("To:");

        txt_v1from.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v1from.setText("0000");
        txt_v1from.setEnabled(false);

        txt_v1to.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v1to.setText("9999");
        txt_v1to.setEnabled(false);

        lbl_v1file.setText("File:");

        txt_v1file.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v1file.setEnabled(false);

        btn_v1browse.setText("Browse");
        btn_v1browse.setEnabled(false);
        btn_v1browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_v1browseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pnl_var1Layout = new org.jdesktop.layout.GroupLayout(pnl_var1);
        pnl_var1.setLayout(pnl_var1Layout);
        pnl_var1Layout.setHorizontalGroup(
            pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnl_var1Layout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(btn_v1browse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(pnl_var1Layout.createSequentialGroup()
                        .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rdo_v1number)
                            .add(pnl_var1Layout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(lbl_v1from)
                                    .add(lbl_v1to)))
                            .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(lbl_v1file)
                                .add(rdo_v1file)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txt_v1to, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                            .add(txt_v1from, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                            .add(txt_v1file, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnl_var1Layout.setVerticalGroup(
            pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_var1Layout.createSequentialGroup()
                .add(rdo_v1number)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v1from)
                    .add(txt_v1from, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v1to)
                    .add(txt_v1to, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_v1file)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v1file)
                    .add(txt_v1file, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(btn_v1browse))
        );

        tpn_vars.addTab("Parameter 1", pnl_var1);

        pnl_var2.setBorder(javax.swing.BorderFactory.createTitledBorder("Outer Loop"));

        rdo_v2file.setText("File");
        rdo_v2file.setEnabled(false);
        rdo_v2file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_v2fileActionPerformed(evt);
            }
        });

        rdo_v2number.setSelected(true);
        rdo_v2number.setText("Numeric");
        rdo_v2number.setEnabled(false);
        rdo_v2number.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_v2numberActionPerformed(evt);
            }
        });

        lbl_v2from.setText("From:");

        lbl_v2to.setText("To:");

        txt_v2from.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v2from.setText("0000");
        txt_v2from.setEnabled(false);

        txt_v2to.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v2to.setText("9999");
        txt_v2to.setEnabled(false);

        lbl_v2file.setText("File:");

        txt_v2file.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_v2file.setEnabled(false);

        btn_v2browse.setText("Browse");
        btn_v2browse.setEnabled(false);
        btn_v2browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_v2browseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pnl_var2Layout = new org.jdesktop.layout.GroupLayout(pnl_var2);
        pnl_var2.setLayout(pnl_var2Layout);
        pnl_var2Layout.setHorizontalGroup(
            pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnl_var2Layout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(btn_v2browse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .add(pnl_var2Layout.createSequentialGroup()
                        .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rdo_v2number)
                            .add(pnl_var2Layout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(lbl_v2from)
                                    .add(lbl_v2to)))
                            .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(lbl_v2file)
                                .add(rdo_v2file)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(txt_v2to, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                            .add(txt_v2from, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                            .add(txt_v2file, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnl_var2Layout.setVerticalGroup(
            pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_var2Layout.createSequentialGroup()
                .add(rdo_v2number)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v2from)
                    .add(txt_v2from, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v2to)
                    .add(txt_v2to, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_v2file)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_var2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_v2file)
                    .add(txt_v2file, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(btn_v2browse))
        );

        tpn_vars.addTab("Parameter 2", pnl_var2);

        spn_right.setLeftComponent(tpn_vars);
        tpn_vars.getAccessibleContext().setAccessibleName("Parameter 1");

        btn_baseresponse.setText("Base Response");
        btn_baseresponse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_baseresponseActionPerformed(evt);
            }
        });

        btn_start.setText("Start");
        btn_start.setEnabled(false);
        btn_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_startActionPerformed(evt);
            }
        });

        btn_stop.setText("Stop");
        btn_stop.setEnabled(false);
        btn_stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_stopActionPerformed(evt);
            }
        });

        btn_pause.setText("Pause");
        btn_pause.setEnabled(false);
        btn_pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_pauseActionPerformed(evt);
            }
        });

        lbl_tc.setText("Threads:");

        spi_tc.setValue(3);

        org.jdesktop.layout.GroupLayout pnl_actionsLayout = new org.jdesktop.layout.GroupLayout(pnl_actions);
        pnl_actions.setLayout(pnl_actionsLayout);
        pnl_actionsLayout.setHorizontalGroup(
            pnl_actionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_actionsLayout.createSequentialGroup()
                .add(pnl_actionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btn_pause, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btn_stop, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btn_start, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, btn_baseresponse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 97, Short.MAX_VALUE)
                .add(lbl_tc)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spi_tc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnl_actionsLayout.setVerticalGroup(
            pnl_actionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_actionsLayout.createSequentialGroup()
                .add(pnl_actionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnl_actionsLayout.createSequentialGroup()
                        .add(pnl_actionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(btn_baseresponse)
                            .add(lbl_tc))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btn_start)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btn_stop))
                    .add(spi_tc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btn_pause)
                .addContainerGap(168, Short.MAX_VALUE))
        );

        tpn_control.addTab("Actions", pnl_actions);

        lbl_host.setText("Host:");

        lbl_port.setText("Port:");

        txt_host.setText("127.0.0.1");

        txt_port.setText("80");

        lbl_timeout.setText("Timeout:");

        spi_timeout.setValue(6000);

        lbl_retries.setText("Retries:");

        spi_retries.setValue(3);

        chk_ssl.setLabel("SSL");
        chk_ssl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chk_sslActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pnl_targetLayout = new org.jdesktop.layout.GroupLayout(pnl_target);
        pnl_target.setLayout(pnl_targetLayout);
        pnl_targetLayout.setHorizontalGroup(
            pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_targetLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnl_targetLayout.createSequentialGroup()
                        .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbl_host)
                            .add(lbl_port))
                        .add(32, 32, 32)
                        .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(txt_host, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                            .add(txt_port, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)))
                    .add(chk_ssl)
                    .add(pnl_targetLayout.createSequentialGroup()
                        .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbl_retries)
                            .add(lbl_timeout))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, spi_retries, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                            .add(spi_timeout, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnl_targetLayout.setVerticalGroup(
            pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_targetLayout.createSequentialGroup()
                .add(41, 41, 41)
                .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_host)
                    .add(txt_host, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_port)
                    .add(txt_port, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(chk_ssl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_timeout)
                    .add(spi_timeout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_targetLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_retries)
                    .add(spi_retries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(89, Short.MAX_VALUE))
        );

        tpn_control.addTab("Target", pnl_target);

        chk_ignore.setText("Ignore FLT");
        chk_ignore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chk_ignoreActionPerformed(evt);
            }
        });

        lbl_low.setText("Low:");

        lbl_high.setText("High:");

        rdo_eq.setText("==");
        rdo_eq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_eqActionPerformed(evt);
            }
        });

        rdo_ne.setText("!=");
        rdo_ne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_neActionPerformed(evt);
            }
        });

        rdo_all.setSelected(true);
        rdo_all.setText("All");
        rdo_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_allActionPerformed(evt);
            }
        });

        rdo_outside.setText("<>");
        rdo_outside.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_outsideActionPerformed(evt);
            }
        });

        rdo_inside.setText("><");
        rdo_inside.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdo_insideActionPerformed(evt);
            }
        });

        btn_recalc.setText("Recalculate");
        btn_recalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_recalcActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pnl_fuzzLayout = new org.jdesktop.layout.GroupLayout(pnl_fuzz);
        pnl_fuzz.setLayout(pnl_fuzzLayout);
        pnl_fuzzLayout.setHorizontalGroup(
            pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_fuzzLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rdo_inside)
                    .add(rdo_outside)
                    .add(rdo_ne)
                    .add(chk_ignore)
                    .add(pnl_fuzzLayout.createSequentialGroup()
                        .add(pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbl_low)
                            .add(lbl_high))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 124, Short.MAX_VALUE)
                        .add(pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(spi_low)
                            .add(spi_high, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)))
                    .add(rdo_eq)
                    .add(rdo_all)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, btn_recalc))
                .addContainerGap())
        );
        pnl_fuzzLayout.setVerticalGroup(
            pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_fuzzLayout.createSequentialGroup()
                .add(pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(pnl_fuzzLayout.createSequentialGroup()
                        .add(chk_ignore)
                        .add(17, 17, 17)
                        .add(lbl_low))
                    .add(spi_low, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_fuzzLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lbl_high)
                    .add(spi_high, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(rdo_eq)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_ne)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_all)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_outside)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rdo_inside)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 50, Short.MAX_VALUE)
                .add(btn_recalc))
        );

        tpn_control.addTab("Fuzzy Logic", pnl_fuzz);

        lbl_contentstart.setText("Start Token:");

        txt_contentstart.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_contentstart.setText("le>");

        lbl_contentend.setText("End Token:");

        txt_contentend.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txt_contentend.setText("</tit");

        org.jdesktop.layout.GroupLayout pnl_contentLayout = new org.jdesktop.layout.GroupLayout(pnl_content);
        pnl_content.setLayout(pnl_contentLayout);
        pnl_contentLayout.setHorizontalGroup(
            pnl_contentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_contentLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_contentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnl_contentLayout.createSequentialGroup()
                        .add(lbl_contentstart)
                        .add(14, 14, 14)
                        .add(txt_contentstart, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                    .add(pnl_contentLayout.createSequentialGroup()
                        .add(lbl_contentend)
                        .add(18, 18, 18)
                        .add(txt_contentend, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnl_contentLayout.setVerticalGroup(
            pnl_contentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnl_contentLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnl_contentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txt_contentstart, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lbl_contentstart))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnl_contentLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lbl_contentend)
                    .add(txt_contentend, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(212, Short.MAX_VALUE))
        );

        tpn_control.addTab("Content Extraction", pnl_content);

        spn_right.setRightComponent(tpn_control);

        spn_main.setRightComponent(spn_right);

        pnl_status.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lbl_status.setText("SensePost CrowBar-J");

        org.jdesktop.layout.GroupLayout pnl_statusLayout = new org.jdesktop.layout.GroupLayout(pnl_status);
        pnl_status.setLayout(pnl_statusLayout);
        pnl_statusLayout.setHorizontalGroup(
            pnl_statusLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, lbl_status, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE)
        );
        pnl_statusLayout.setVerticalGroup(
            pnl_statusLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(lbl_status)
        );

        mnu_file.setText("File");

        mni_import.setText("Import Request");
        mni_import.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_importActionPerformed(evt);
            }
        });
        mnu_file.add(mni_import);

        mni_exportlist.setText("Export Results From List");
        mni_exportlist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_exportlistActionPerformed(evt);
            }
        });
        mnu_file.add(mni_exportlist);
        mni_exportlist.getAccessibleContext().setAccessibleName("Export Result List");

        mni_exportall.setText("Export All Results");
        mni_exportall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_exportallActionPerformed(evt);
            }
        });
        mnu_file.add(mni_exportall);
        mnu_file.add(mns_file);

        mni_exit.setText("Exit");
        mni_exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_exitActionPerformed(evt);
            }
        });
        mnu_file.add(mni_exit);

        mnu_main.add(mnu_file);

        mnu_help.setText("Help");

        mni_help.setText("Index");
        mni_help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_helpActionPerformed(evt);
            }
        });
        mnu_help.add(mni_help);

        mnu_main.add(mnu_help);

        setJMenuBar(mnu_main);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 819, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnl_status, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(spn_main, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 819, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 637, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(617, Short.MAX_VALUE)
                    .add(pnl_status, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(spn_main, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // <editor-fold defaultstate="collapsed" desc="Custom Field Helpers">

    private void EnableV1(boolean b) {
        this.rdo_v1file.setEnabled(b);


        this.rdo_v1number.setEnabled(b);


        if (b) {
            if (this.rdo_v1file.getSelectedObjects() != null) {
                this.txt_v1file.setEnabled(true);


                this.btn_v1browse.setEnabled(true);


                this.txt_v1from.setEnabled(false);


                this.txt_v1to.setEnabled(false);


            } else {
                this.txt_v1file.setEnabled(false);


                this.btn_v1browse.setEnabled(false);


                this.txt_v1from.setEnabled(true);


                this.txt_v1to.setEnabled(true);


            }
        } else {
            this.txt_v1file.setEnabled(false);


            this.btn_v1browse.setEnabled(false);


            this.txt_v1from.setEnabled(false);


            this.txt_v1to.setEnabled(false);


        }
    }

    private void EnableV2(boolean b) {
        this.rdo_v2file.setEnabled(b);


        this.rdo_v2number.setEnabled(b);


        this.txt_v2file.setEnabled(b);


        this.btn_v2browse.setEnabled(b);


        this.txt_v2from.setEnabled(b);


        this.txt_v2to.setEnabled(b);


        if (b) {
            if (this.rdo_v2file.getSelectedObjects() != null) {
                this.txt_v2file.setEnabled(true);


                this.btn_v2browse.setEnabled(true);


                this.txt_v2from.setEnabled(false);


                this.txt_v2to.setEnabled(false);


            } else {
                this.txt_v2file.setEnabled(false);


                this.btn_v2browse.setEnabled(false);


                this.txt_v2from.setEnabled(true);


                this.txt_v2to.setEnabled(true);


            }
        } else {
            this.txt_v2file.setEnabled(false);


            this.btn_v2browse.setEnabled(false);


            this.txt_v2from.setEnabled(false);


            this.txt_v2to.setEnabled(false);


        }
    }

    private void SetV1Fields() {
        if (this.rdo_v1file.getSelectedObjects() != null) {
            this.txt_v1file.setEnabled(true);


            this.btn_v1browse.setEnabled(true);


            this.txt_v1from.setEnabled(false);


            this.txt_v1to.setEnabled(false);


        } else {
            this.txt_v1file.setEnabled(false);


            this.btn_v1browse.setEnabled(false);


            this.txt_v1from.setEnabled(true);


            this.txt_v1to.setEnabled(true);


        }
    }

    private void SetV2Fields() {
        if (this.rdo_v2file.getSelectedObjects() != null) {
            this.txt_v2file.setEnabled(true);


            this.btn_v2browse.setEnabled(true);


            this.txt_v2from.setEnabled(false);


            this.txt_v2to.setEnabled(false);


        } else {
            this.txt_v2file.setEnabled(false);


            this.btn_v2browse.setEnabled(false);


            this.txt_v2from.setEnabled(true);


            this.txt_v2to.setEnabled(true);


        }
    }

    private void SetFuzzFields() {
        if (this.chk_ignore.getSelectedObjects() == null) {
            this.spi_low.setEnabled(true);


            this.spi_high.setEnabled(true);


            this.rdo_eq.setEnabled(true);


            this.rdo_ne.setEnabled(true);


            this.rdo_all.setEnabled(true);


            this.rdo_inside.setEnabled(true);


            this.rdo_outside.setEnabled(true);


            this.btn_recalc.setEnabled(true);


        } else {
            this.rdo_all.setSelected(true);
            this.spi_low.setEnabled(false);


            this.spi_high.setEnabled(false);


            this.rdo_eq.setEnabled(false);


            this.rdo_ne.setEnabled(false);


            this.rdo_all.setEnabled(false);


            this.rdo_inside.setEnabled(false);


            this.rdo_outside.setEnabled(false);


            this.btn_recalc.setEnabled(false);


        }
    }

    private void CheckParameters() {
        if (this.txt_httpreq.getText().contains("##1##")) {
            this.v1 = true;


        } else {
            this.v1 = false;


        }
        EnableV1(this.v1);


        if (this.txt_httpreq.getText().contains("##2##")) {
            this.v2 = true;


        } else {
            this.v2 = false;


        }
        EnableV2(this.v2);


    }

    private ArrayList ParseTheInts(String s1, String s2) throws Exception {
        ArrayList ret_list = new ArrayList();


        int n1 = 0;


        int n2 = 0;


        try {
            n1 = Integer.parseInt(s1);
            n2 = Integer.parseInt(s2);


        } catch (Exception e) {
            throw e;


        }
        if (n1 > n2) {
            throw new Exception("Start int less than end int...");


        }
        for (int n = n1; n
                <= n2; n++) {
            String s = Integer.toString(n);


            while (s.length() < s1.length()) {
                s = "0" + s;


            }
            ret_list.add(s);


        }
        return ret_list;


    }

    private void SetBaseResponse() {
        this.lbl_status.setText("Getting base response...");
        String HttpReq = this.txt_httpreq.getText();
        HttpReq = HttpReq.replaceAll("\r", "");
        HttpReq = HttpReq.replaceAll("\n", "\r\n");


        if (!HttpReq.endsWith("\r\n\r\n")) {
            HttpReq += "\r\n\r\n";


        }
        try {
            this.BaseResponse = GetHttpResponse.getRespone(this.sz_host, this.n_port, this.b_ssl, HttpReq, this.getTimeOut());
            this.lbl_status.setText("Base response loaded. Click Start to start BruteForce.");
        } catch (Exception e) {
            this.BaseResponse = "";
            this.lbl_status.setText("Could not get base response...");
        }
    }

    public String getBaseResponse() {
        return this.BaseResponse;
    }

    private void BuildQueue(ArrayList a1, String s) {
        String HttpReq = txt_httpreq.getText();


        for (int i = 0; i < a1.size(); i++) {
            String myReq = HttpReq.replaceAll(s, a1.get(i).toString());
            myReq = myReq.replaceAll("\r", "");
            String[] req = myReq.split("\n\n", 2);
            String hdr = req[0];
            String bod = "";
            if (req.length > 1) {
                bod = req[1];
            } else {
                bod = "";
            }
            int clen = bod.length();
            String[] hda = hdr.split("\n");
            String newReq = "";
            for (int z = 0; z < hda.length; z++) {
                if (!hda[z].startsWith("Content-Length:")) {
                    newReq += hda[z] + "\n";
                }
            }
            newReq += "Content-Length: " + clen + "\n";
            newReq += "\n" + bod;
            newReq = newReq.replaceAll("\n", "\r\n");
            myReq = newReq;
            JBaahType cbt = new JBaahType();
            if (s.startsWith("##1##")) {
                cbt = new JBaahType(sz_host, n_port, b_ssl, a1.get(i).toString(), "");

            } else {
                cbt = new JBaahType(sz_host, n_port, b_ssl, "", a1.get(i).toString());

            }
            cbt.setRequest(myReq);
            AttackQueue.add(cbt);
        }
    }

    private void BuildQueue(ArrayList a1, ArrayList a2) {
        this.IsReporterRunning = true;
        String HttpReq = txt_httpreq.getText();
        for (int i = 0; i < a1.size(); i++) {
            for (int j = 0; j < a2.size(); j++) {
                String myReq = HttpReq.replaceAll("##1##", a1.get(i).toString());
                myReq = myReq.replaceAll("##2##", a2.get(j).toString());

                myReq = myReq.replaceAll("\r", "");
                String[] req = myReq.split("\n\n", 2);
                String hdr = req[0];
                String bod = "";
                if (req.length > 1) {
                    bod = req[1];
                } else {
                    bod = "";
                }
                int clen = bod.length();
                String[] hda = hdr.split("\n");
                String newReq = "";
                for (int z = 0; z < hda.length; z++) {
                    if (!hda[z].startsWith("Content-Length:")) {
                        newReq += hda[z] + "\n";
                    }
                }
                newReq += "Content-Length: " + clen + "\n";
                newReq += "\n" + bod;
                newReq = newReq.replaceAll("\n", "\r\n");
                myReq = newReq;
                JBaahType cbt = new JBaahType(sz_host, n_port, b_ssl, a1.get(i).toString(), a2.get(j).toString());
                cbt.setRequest(myReq);
                AttackQueue.add(cbt);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generated Field Actions">
    private void rdo_v1fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_v1fileActionPerformed
        if (this.rdo_v1file.getSelectedObjects() != null) {
            this.rdo_v1number.setSelected(false);


        } else {
            this.rdo_v1number.setSelected(true);


        }
        SetV1Fields();
    }//GEN-LAST:event_rdo_v1fileActionPerformed
    private void rdo_v1numberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_v1numberActionPerformed
        if (this.rdo_v1number.getSelectedObjects() != null) {
            this.rdo_v1file.setSelected(false);


        } else {
            this.rdo_v1file.setSelected(true);


        }
        SetV1Fields();
    }//GEN-LAST:event_rdo_v1numberActionPerformed
    private void rdo_v2fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_v2fileActionPerformed
        if (this.rdo_v2file.getSelectedObjects() != null) {
            this.rdo_v2number.setSelected(false);


        } else {
            this.rdo_v2number.setSelected(true);


        }
        SetV2Fields();
    }//GEN-LAST:event_rdo_v2fileActionPerformed
    private void rdo_v2numberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_v2numberActionPerformed
        if (this.rdo_v2number.getSelectedObjects() != null) {
            this.rdo_v2file.setSelected(false);


        } else {
            this.rdo_v2file.setSelected(true);


        }
        SetV2Fields();
    }//GEN-LAST:event_rdo_v2numberActionPerformed
    private void chk_sslActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chk_sslActionPerformed
        if (this.chk_ssl.getSelectedObjects() != null) {
            this.txt_port.setText("443");


        } else {
            this.txt_port.setText("80");


        }
    }//GEN-LAST:event_chk_sslActionPerformed

    private void chk_ignoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chk_ignoreActionPerformed
        SetFuzzFields();
    }//GEN-LAST:event_chk_ignoreActionPerformed

    private void mni_importActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_importActionPerformed
        final JFileChooser fc = new JFileChooser();


        int returnVal = fc.showOpenDialog(this);
        String sz_req = "";


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                sz_req = this.fio.FileToString(fc.getSelectedFile());


            } catch (FileMethodException e) {
                //TODO: Add error dialog here...
            }
        }
        this.txt_httpreq.setText(sz_req);
        CheckParameters();
    }//GEN-LAST:event_mni_importActionPerformed

    private void btn_v1browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_v1browseActionPerformed
        final JFileChooser fc = new JFileChooser();


        int returnVal = fc.showOpenDialog(this);
        String sz_fil = "";


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                sz_fil = fc.getSelectedFile().getCanonicalPath();


            } catch (IOException ex) {
                //TODO: Add error dialog here...
            }
        }
        this.txt_v1file.setText(sz_fil);
    }//GEN-LAST:event_btn_v1browseActionPerformed

    private void btn_v2browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_v2browseActionPerformed
        final JFileChooser fc = new JFileChooser();


        int returnVal = fc.showOpenDialog(this);
        String sz_fil = "";


        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                sz_fil = fc.getSelectedFile().getCanonicalPath();


            } catch (IOException ex) {
                //TODO: Add error dialog here...
            }
        }
        this.txt_v2file.setText(sz_fil);
    }//GEN-LAST:event_btn_v2browseActionPerformed
    private void btn_baseresponseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_baseresponseActionPerformed
        this.sz_host = this.txt_host.getText();


        try {
            this.n_port = Integer.parseInt(this.txt_port.getText());


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid port specified", "Error", JOptionPane.ERROR_MESSAGE);


            return;


        }
        if (this.chk_ssl.getSelectedObjects() != null) {
            this.b_ssl = true;


        } else {
            this.b_ssl = false;


        }
        this.SetBaseResponse();


        if (this.BaseResponse.compareTo("") != 0) {
            this.btn_start.setEnabled(true);


        }
        this.txt_httpres.setText(this.BaseResponse);
    }//GEN-LAST:event_btn_baseresponseActionPerformed

    private void btn_startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_startActionPerformed
        this.btn_baseresponse.setEnabled(false);
        this.btn_stop.setEnabled(true);
        this.btn_pause.setEnabled(true);
        this.btn_start.setEnabled(false);
        this.listmodel.clear();
        this.listmodel.addElement("SCORE  :  PARAMETER 1  :  PARAMETER 2  :  CONTENT");
        this.lbl_status.setText("STARTING...");
        this.sz_stat = "RUNNING";
        listmodel.setUpdate();
        if (this.BaseResponse.equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(this, "Base response has not been obtained yet", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ArrayList ar_outer = new ArrayList();
        ArrayList ar_inner = new ArrayList();
        this.sz_host = this.txt_host.getText();
        try {
            this.n_port = Integer.parseInt(this.txt_port.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid port specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (this.chk_ssl.getSelectedObjects() != null) {
            this.b_ssl = true;
        } else {
            this.b_ssl = false;
        } // First, we check to see whether at least 1 parameter has been specified...
        if (!this.v1 && !this.v2) {
            //JOptionPane.showOptionDialog(pnl_content, mns_file, null, n_total, WIDTH, null, options, n_total)
            JOptionPane.showMessageDialog(this, "No fuzz parameters specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Firstly, we check to see whether parameter1 is specified
        if (this.v1) {
            // We're going through the list numerically...
            if (this.rdo_v1number.getSelectedObjects() != null) {
                String s1 = this.txt_v1from.getText();
                String s2 = this.txt_v1to.getText();
                try {
                    ar_outer = ParseTheInts(s1, s2);
                } catch (Exception e) {
                    if (e.getMessage().startsWith("Start int less than end int...")) {
                        JOptionPane.showMessageDialog(this, "Start number for parameter 1 is less than End Integer", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid number specified for parameter 1", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            } // We're going through some other list...
            else {
                try {
                    File tmp_file = new File(this.txt_v1file.getText());
                    ar_outer = this.fio.FileToList(tmp_file);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Could not read from file for parameter 1", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (this.v2) {
            // We're going through the list numerically...
            if (this.rdo_v2number.getSelectedObjects() != null) {
                String s1 = this.txt_v2from.getText();
                String s2 = this.txt_v2to.getText();
                try {
                    ar_inner = ParseTheInts(s1, s2);

                } catch (Exception e) {
                    if (e.getMessage().startsWith("Start int less than end int...")) {
                        JOptionPane.showMessageDialog(this, "Start number for parameter 2 is less than End Integer", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid number specified for parameter 2", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            } // We're going through some other list...
            else {
                try {
                    File tmp_file = new File(this.txt_v2file.getText());
                    ar_inner = this.fio.FileToList(tmp_file);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Could not read from file for parameter 2", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        if (v1 && !v2) {
            BuildQueue(ar_outer, "##1##");
        } else if (v2 && !v1) {
            BuildQueue(ar_inner, "##2##");
        } else {
            BuildQueue(ar_outer, ar_inner);
        }
        n_total = AttackQueue.size();
        listmodel.ensureCapacity(n_total + 10);
        ResultList = new ArrayList();
        ResultList.ensureCapacity(n_total + 10);
        ResultQueue = new LinkedList<JBaahType>();
        this.IsRunning = true;
        int threads = Integer.parseInt(this.spi_tc.getValue().toString());
        for (int i = 0;
                i < threads;
                i++) {
            JBaah cb = new JBaah(this, this.sz_host, this.n_port, this.b_ssl);
            cb.start();
        }


        this.lbl_status.setText(
                "STARTING...");

        this.sz_stat = "RUNNING";
    }//GEN-LAST:event_btn_startActionPerformed
    private void btn_stopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_stopActionPerformed
        this.IsRunning = false;
        this.AttackQueue.clear();
        this.btn_baseresponse.setEnabled(true);
        this.btn_start.setEnabled(true);
        this.btn_stop.setEnabled(false);
        this.btn_pause.setEnabled(false);
        this.btn_pause.setText("Pause");
        this.lbl_status.setText("STOPPING...");
        this.sz_stat = "STOPPED";
    }//GEN-LAST:event_btn_stopActionPerformed
    private void btn_pauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_pauseActionPerformed
        if (this.btn_pause.getText().compareTo("Pause") == 0) {
            this.IsRunning = false;
            this.btn_baseresponse.setEnabled(false);
            this.btn_start.setEnabled(false);
            this.btn_stop.setEnabled(true);
            this.btn_pause.setEnabled(true);
            this.btn_pause.setText("Resume");
            this.lbl_status.setText("PAUSING...");
            sz_stat = "PAUSED";
        } else {
            this.btn_baseresponse.setEnabled(false);
            this.btn_start.setEnabled(false);
            this.btn_stop.setEnabled(true);
            this.btn_pause.setEnabled(true);
            this.btn_pause.setText("Pause");
            this.IsRunning = true;
            int threads = Integer.parseInt(this.spi_tc.getValue().toString());
            for (int i = 0; i < threads; i++) {
                JBaah cb = new JBaah(this, this.sz_host, this.n_port, this.b_ssl);
                cb.start();
            }
            this.lbl_status.setText("RESUMING...");
            sz_stat = "RUNNING";
        }
    }//GEN-LAST:event_btn_pauseActionPerformed

    private void txt_httpreqInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_txt_httpreqInputMethodTextChanged
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqInputMethodTextChanged
    private void txt_httpreqFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_httpreqFocusGained
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqFocusGained
    private void txt_httpreqFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txt_httpreqFocusLost
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqFocusLost
    private void txt_httpreqPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_txt_httpreqPropertyChange
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqPropertyChange
    private void txt_httpreqKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_httpreqKeyTyped
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqKeyTyped
    private void txt_httpreqKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_httpreqKeyReleased
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqKeyReleased
    private void txt_httpreqKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_httpreqKeyPressed
        CheckParameters();
    }//GEN-LAST:event_txt_httpreqKeyPressed
    private void lst_resultsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_lst_resultsFocusGained
        this.lst_results.repaint();
    }//GEN-LAST:event_lst_resultsFocusGained

    private void rdo_eqActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_eqActionPerformed
        rdo_eq.setSelected(true);
        rdo_ne.setSelected(false);
        rdo_all.setSelected(false);
        rdo_inside.setSelected(false);
        rdo_outside.setSelected(false);
        spi_high.setValue(spi_low.getValue());
    }//GEN-LAST:event_rdo_eqActionPerformed

    private void rdo_neActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_neActionPerformed
        rdo_eq.setSelected(false);
        rdo_ne.setSelected(true);
        rdo_all.setSelected(false);
        rdo_inside.setSelected(false);
        rdo_outside.setSelected(false);
        spi_high.setValue(spi_low.getValue());
    }//GEN-LAST:event_rdo_neActionPerformed

    private void rdo_allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_allActionPerformed
        rdo_eq.setSelected(false);
        rdo_ne.setSelected(false);
        rdo_all.setSelected(true);
        rdo_inside.setSelected(false);
        rdo_outside.setSelected(false);
    }//GEN-LAST:event_rdo_allActionPerformed

    private void rdo_outsideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_outsideActionPerformed
        rdo_eq.setSelected(false);
        rdo_ne.setSelected(false);
        rdo_all.setSelected(false);
        rdo_inside.setSelected(false);
        rdo_outside.setSelected(true);
    }//GEN-LAST:event_rdo_outsideActionPerformed

    private void rdo_insideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdo_insideActionPerformed
        rdo_eq.setSelected(false);
        rdo_ne.setSelected(false);
        rdo_all.setSelected(false);
        rdo_inside.setSelected(true);
        rdo_outside.setSelected(false);
    }//GEN-LAST:event_rdo_insideActionPerformed

    private void btn_recalcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_recalcActionPerformed
        FilterValues();
    }//GEN-LAST:event_btn_recalcActionPerformed

    private void mni_exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_exitActionPerformed
        this.lbl_status.setText("SHUTTING DOWN - WAITING FOR THREADS...");
        this.IsReporterRunning = false;
        this.IsRunning = false;
        this.dispose();
        System.exit(0);
    }//GEN-LAST:event_mni_exitActionPerformed

    private void mni_onlytheseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_onlytheseActionPerformed
        try {
            if (lst_results.getSelectedValue() != null) {
                String s = lst_results.getSelectedValue().toString();
                s = s.replaceAll("  :  ", ":SPLITHERE:");
                String[] a = s.split(":SPLITHERE:");
                Double d = Double.valueOf(a[0]);
                this.spi_low.setValue(d);
                this.spi_high.setValue(d);
                this.rdo_eq.setSelected(true);
                FilterValues();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_mni_onlytheseActionPerformed

    private void mni_allbuttheseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_allbuttheseActionPerformed
        try {
            if (lst_results.getSelectedValue() != null) {
                String s = lst_results.getSelectedValue().toString();
                s = s.replaceAll("  :  ", ":SPLITHERE:");
                String[] a = s.split(":SPLITHERE:");
                Double d = Double.valueOf(a[0]);
                this.spi_low.setValue(d);
                this.spi_high.setValue(d);
                this.rdo_ne.setSelected(true);
                FilterValues();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_mni_allbuttheseActionPerformed

    private void mni_viewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_viewActionPerformed
        try {
            if (lst_results.getSelectedValue() != null) {
                String s = lst_results.getSelectedValue().toString();
                s = s.replaceAll("  :  ", ":SPLITHERE:");
                String[] a = s.split(":SPLITHERE:");
                Double d = Double.valueOf(a[0]);
                String o = a[1];
                String i = a[2];
                for (int j = 0; j < ResultList.size(); j++) {
                    JBaahType c = (JBaahType) ResultList.get(j);
                    if ((c.getOuterLoop().compareTo(o) == 0) && (c.getInnerLoop().compareTo(i) == 0)) {
                        JBaahResultForm cbr = new JBaahResultForm(c.getResponse());
                        cbr.setVisible(true);
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_mni_viewActionPerformed

    private void mni_exportlistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_exportlistActionPerformed
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        //int returnVal = fc.showOpenDialog(this);
        ArrayList filedata = new ArrayList();
        for (int i = 0; i < listmodel.getSize(); i++) {
            String s = listmodel.getElementAt(i).toString();
            filedata.add(s);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                boolean b = this.fio.ArrayToFile(fc.getSelectedFile().getCanonicalPath(), filedata);
                if (b) {
                    JOptionPane.showMessageDialog(this, "Result List exported Successfully", "Results Exported", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not export the result list to file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Could not export the result list to file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_mni_exportlistActionPerformed

    private void mni_exportallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_exportallActionPerformed
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(this);
        //int returnVal = fc.showOpenDialog(this);
        ArrayList filedata = new ArrayList();
        DecimalFormat f = new DecimalFormat("#.#####");
        for (int i = 0; i < ResultList.size(); i++) {
            JBaahType c = (JBaahType) ResultList.get(i);
            String s = f.format(c.getScore()) + "  :  " + c.getOuterLoop() + "  :  " + c.getInnerLoop() + "  :  " + c.getContent();
            filedata.add(s);
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                boolean b = this.fio.ArrayToFile(fc.getSelectedFile().getCanonicalPath(), filedata);
                if (b) {
                    JOptionPane.showMessageDialog(this, "All results exported Successfully", "Results Exported", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not export all results to file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Could not export all results to file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_mni_exportallActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.IsReporterRunning = false;
        this.IsRunning = false;
    }//GEN-LAST:event_formWindowClosing

    private void mni_helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_helpActionPerformed
        JBaahHelp cbh = new JBaahHelp();
        cbh.setVisible(true);
    }//GEN-LAST:event_mni_helpActionPerformed

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="MAIN Entry">
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SplashScreen moo = new SplashScreen(6000);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new JBaahMain().setVisible(true);


            }
        });


    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Generated Variables">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_baseresponse;
    private javax.swing.JButton btn_pause;
    private javax.swing.JButton btn_recalc;
    private javax.swing.JButton btn_start;
    private javax.swing.JButton btn_stop;
    private javax.swing.JButton btn_v1browse;
    private javax.swing.JButton btn_v2browse;
    private javax.swing.JCheckBox chk_ignore;
    private javax.swing.JCheckBox chk_ssl;
    private javax.swing.JLabel lbl_contentend;
    private javax.swing.JLabel lbl_contentstart;
    private javax.swing.JLabel lbl_high;
    private javax.swing.JLabel lbl_host;
    private javax.swing.JLabel lbl_low;
    private javax.swing.JLabel lbl_port;
    private javax.swing.JLabel lbl_retries;
    private javax.swing.JLabel lbl_status;
    private javax.swing.JLabel lbl_tc;
    private javax.swing.JLabel lbl_timeout;
    private javax.swing.JLabel lbl_v1file;
    private javax.swing.JLabel lbl_v1from;
    private javax.swing.JLabel lbl_v1to;
    private javax.swing.JLabel lbl_v2file;
    private javax.swing.JLabel lbl_v2from;
    private javax.swing.JLabel lbl_v2to;
    private javax.swing.JList lst_results;
    private javax.swing.JMenuItem mni_allbutthese;
    private javax.swing.JMenuItem mni_exit;
    private javax.swing.JMenuItem mni_exportall;
    private javax.swing.JMenuItem mni_exportlist;
    private javax.swing.JMenuItem mni_help;
    private javax.swing.JMenuItem mni_import;
    private javax.swing.JMenuItem mni_onlythese;
    private javax.swing.JMenuItem mni_view;
    private javax.swing.JPopupMenu.Separator mns_component;
    private javax.swing.JPopupMenu.Separator mns_file;
    private javax.swing.JPopupMenu mnu_context;
    private javax.swing.JMenu mnu_file;
    private javax.swing.JMenu mnu_help;
    private javax.swing.JMenuBar mnu_main;
    private javax.swing.JPanel pnl_actions;
    private javax.swing.JPanel pnl_content;
    private javax.swing.JPanel pnl_fuzz;
    private javax.swing.JPanel pnl_httpreq;
    private javax.swing.JPanel pnl_httpres;
    private javax.swing.JPanel pnl_status;
    private javax.swing.JPanel pnl_target;
    private javax.swing.JPanel pnl_var1;
    private javax.swing.JPanel pnl_var2;
    private javax.swing.JRadioButton rdo_all;
    private javax.swing.JRadioButton rdo_eq;
    private javax.swing.JRadioButton rdo_inside;
    private javax.swing.JRadioButton rdo_ne;
    private javax.swing.JRadioButton rdo_outside;
    private javax.swing.JRadioButton rdo_v1file;
    private javax.swing.JRadioButton rdo_v1number;
    private javax.swing.JRadioButton rdo_v2file;
    private javax.swing.JRadioButton rdo_v2number;
    private javax.swing.JScrollPane scp_httpreq;
    private javax.swing.JScrollPane scp_httpres;
    private javax.swing.JScrollPane scp_results;
    private javax.swing.JSpinner spi_high;
    private javax.swing.JSpinner spi_low;
    private javax.swing.JSpinner spi_retries;
    private javax.swing.JSpinner spi_tc;
    private javax.swing.JSpinner spi_timeout;
    private javax.swing.JSplitPane spn_left;
    private javax.swing.JSplitPane spn_main;
    private javax.swing.JSplitPane spn_right;
    private javax.swing.JTabbedPane tpn_control;
    private javax.swing.JTabbedPane tpn_httpreqres;
    private javax.swing.JTabbedPane tpn_vars;
    private javax.swing.JTextField txt_contentend;
    private javax.swing.JTextField txt_contentstart;
    private javax.swing.JTextField txt_host;
    private javax.swing.JTextArea txt_httpreq;
    private javax.swing.JTextArea txt_httpres;
    private javax.swing.JTextField txt_port;
    private javax.swing.JTextField txt_v1file;
    private javax.swing.JTextField txt_v1from;
    private javax.swing.JTextField txt_v1to;
    private javax.swing.JTextField txt_v2file;
    private javax.swing.JTextField txt_v2from;
    private javax.swing.JTextField txt_v2to;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
