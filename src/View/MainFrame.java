/*
 * Created by JFormDesigner on Mon Apr 22 15:40:23 CST 2019
 */

package View;

import java.awt.event.*;

import ECC.*;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import javax.swing.*;

/**
 * @author leng
 */
public class MainFrame extends JFrame {
    public MainFrame() {
        initComponents();
    }

    //对给出的参数进行处理，产生密钥对keyPair
    private void button_processActionPerformed(ActionEvent e) {
        // TODO add your code here
        if(textField_a.getText().trim().equals("") || textField_b.getText().trim().equals("") || textField_p.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, "参数a, b, p为必填内容，请填写完整", "提示", JOptionPane.WARNING_MESSAGE);
        } else {
            BigInteger a = new BigInteger(textField_a.getText());
            BigInteger b = new BigInteger(textField_b.getText());
            BigInteger p = new BigInteger(textField_p.getText());
            BigInteger gx = new BigInteger(textField_gx.getText());
            BigInteger gy = new BigInteger(textField_gy.getText());

            try {
                keyPair = ECCryptoSystem.generateKeyPair(new EllipticCurve(a, b, p, new ECPoint(gx, gy)), new Random(System.currentTimeMillis()));
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, e1.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
                e1.printStackTrace();
            }
        }
    }

    private void comboBox_kindsActionPerformed(ActionEvent e) {
        // TODO add your code here
        int id = comboBox_kinds.getSelectedIndex();
        switch (id){
            case 0:{
                keyPair = null;
                setParametersTextField(null);
            } break;
            case 1:{
                keyPair = null;
                setParametersTextField(NIST.P_192);
            } break;
            case 2:{
                keyPair = null;
                setParametersTextField(NIST.P_256);
            } break;
            case 3:{
                keyPair = null;
                setParametersTextField(NIST.P_384);
            } break;
            case 4:{
                keyPair = null;
                setParametersTextField(NIST.P_521);
            }
        }
    }

    private void setParametersTextField(EllipticCurve curve) {
        if(curve == null){
            textField_a.setText("");
            textField_b.setText("");
            textField_p.setText("");
            textField_gx.setText("");
            textField_gy.setText("");

            textField_a.setEditable(true);
            textField_b.setEditable(true);
            textField_p.setEditable(true);
            textField_gx.setEditable(true);
            textField_gy.setEditable(true);
        }
        else {
            textField_a.setText(curve.getA().toString());
            textField_b.setText(curve.getB().toString());
            textField_p.setText(curve.getP().toString());
            textField_gx.setText(curve.getBasePoint().getX().toString());
            textField_gy.setText(curve.getBasePoint().getY().toString());

            textField_a.setEditable(false);
            textField_b.setEditable(false);
            textField_p.setEditable(false);
            textField_gx.setEditable(false);
            textField_gy.setEditable(false);
        }
    }

    //产生公钥的响应
    private void button_gpubKeyActionPerformed(ActionEvent e) {
        // TODO add your code here
        if(keyPair == null)
            JOptionPane.showMessageDialog(this, "请先点击Press按钮进行数据处理", "提示", JOptionPane.INFORMATION_MESSAGE);
        else {
            JFileChooser chooser = new JFileChooser();
            int returnValue = chooser.showSaveDialog(this);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                keyPair.getPublicKey().saveToFile(path);
            }
        }
    }

    //产生秘钥的响应
    private void button_gpriKeyActionPerformed(ActionEvent e) {
        // TODO add your code here
        if(keyPair == null)
        JOptionPane.showMessageDialog(this, "请先点击Press按钮进行数据处理", "提示", JOptionPane.INFORMATION_MESSAGE);
        else {
            JFileChooser chooser = new JFileChooser();
            int returnValue = chooser.showSaveDialog(this);
            if(returnValue == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                keyPair.getPrivateKey().saveToFile(path);
            }
        }
    }

    //加密选择明文的响应
    private void button_inFileBro1ActionPerformed(ActionEvent e) {
        // TODO add your code here
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(this);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            textField_inFilePath1.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    //加密选择公钥的响应
    private void button_pubKeyBroActionPerformed(ActionEvent e) {
        // TODO add your code here
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(this);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            textField_pubKeyPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        try {
            pubKey = new PublicKey(textField_pubKeyPath.getText());
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(this, e1.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
            e1.printStackTrace();
        }
    }

    //加密的响应
    private void button_encryptActionPerformed(ActionEvent e) {
        // TODO add your code here
        if(pubKey == null || textField_inFilePath1.getText().trim().equals("") || textField_pubKeyPath.getText().trim().equals("")){
            JOptionPane.showMessageDialog(this, "明文或公钥文件地址为空，请选择！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String inputFile = textField_inFilePath1.getText().trim();

            try {
                byte[] plainText = Files.readAllBytes(Paths.get(inputFile));
                byte[] byteFile = ECCryptoSystem.encrypt(plainText, pubKey);

                JFileChooser chooser = new JFileChooser();
                int returnValue = chooser.showSaveDialog(this);
                if(returnValue == JFileChooser.APPROVE_OPTION) {
                    String savePath = chooser.getSelectedFile().getAbsolutePath();
                    FileOutputStream fout = new FileOutputStream(savePath);
                    fout.write(byteFile);
                    fout.close();
                    File f = new File(savePath);
                    label_time1.setText("Time:" + ECCryptoSystem.getExecutionTime() + "ms");

                    textField_inFilePath1.setText("");
                    textField_pubKeyPath.setText("");
                    pubKey = null;
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    //选择密文的响应
    private void button_inFileBro2ActionPerformed(ActionEvent e) {
        // TODO add your code here
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(this);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            textField_inFilePath2.setText(chooser.getSelectedFile().getAbsolutePath());
        }

    }

    //选择秘钥的响应
    private void button_priKeyBroActionPerformed(ActionEvent e) {
        // TODO add your code here
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(this);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            textField_privKeyPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        try {
            priKey = new PrivateKey(textField_privKeyPath.getText());
        } catch (Exception e1) {
            JOptionPane.showMessageDialog(this, e1.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
            e1.printStackTrace();
        }

    }

    //解密的响应
    private void button_decryptActionPerformed(ActionEvent e) {
        // TODO add your code here
        if(priKey == null || textField_inFilePath2.getText().trim().equals("") || textField_privKeyPath.getText().trim().equals("")){
            JOptionPane.showMessageDialog(this, "密文或私钥文件地址为空，请选择！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String inputFile = textField_inFilePath2.getText().trim();
            try {
                byte[] cipherText = Files.readAllBytes(Paths.get(inputFile));
                byte[] byteFile = ECCryptoSystem.decrypt(cipherText, priKey);

                JFileChooser chooser = new JFileChooser();
                int returnValue = chooser.showSaveDialog(this);
                if(returnValue == JFileChooser.APPROVE_OPTION) {
                    String savePath = chooser.getSelectedFile().getAbsolutePath();
                    FileOutputStream fout = new FileOutputStream(savePath);
                    fout.write(byteFile);
                    fout.close();
                    File f = new File(savePath);
                    label_time2.setText("Time:" + ECCryptoSystem.getExecutionTime() + "ms");

                    textField_inFilePath2.setText("");
                    textField_privKeyPath.setText("");
                    priKey = null;
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        tabbedPane = new JTabbedPane();
        panel1 = new JPanel();
        label_a = new JLabel();
        label_b = new JLabel();
        label_p = new JLabel();
        textField_a = new JTextField();
        textField_b = new JTextField();
        textField_p = new JTextField();
        comboBox_kinds = new JComboBox<>();
        button_gpriKey = new JButton();
        button_gpubKey = new JButton();
        button_process = new JButton();
        label_basePoineX = new JLabel();
        textField_gx = new JTextField();
        textField_gy = new JTextField();
        label_basePointY = new JLabel();
        panel2 = new JPanel();
        label_inFile1 = new JLabel();
        textField_inFilePath1 = new JTextField();
        button_inFileBro1 = new JButton();
        label_pubKey = new JLabel();
        textField_pubKeyPath = new JTextField();
        button_pubKeyBro = new JButton();
        button_encrypt = new JButton();
        label_time1 = new JLabel();
        panel3 = new JPanel();
        label_inFile2 = new JLabel();
        textField_inFilePath2 = new JTextField();
        label_priKey = new JLabel();
        textField_privKeyPath = new JTextField();
        button_inFileBro2 = new JButton();
        button_priKeyBro = new JButton();
        button_decrypt = new JButton();
        label_time2 = new JLabel();

        //======== this ========
        setTitle("ECCryptoSystem");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== tabbedPane ========
        {

            //======== panel1 ========
            {
                panel1.setLayout(null);

                //---- label_a ----
                label_a.setText("Parameter A");
                panel1.add(label_a);
                label_a.setBounds(20, 20, 80, 30);

                //---- label_b ----
                label_b.setText("Parameter B");
                panel1.add(label_b);
                label_b.setBounds(20, 85, 80, 30);

                //---- label_p ----
                label_p.setText("Parameter P");
                panel1.add(label_p);
                label_p.setBounds(20, 150, 80, 30);
                panel1.add(textField_a);
                textField_a.setBounds(15, 50, 250, 35);
                panel1.add(textField_b);
                textField_b.setBounds(15, 115, 250, 35);
                panel1.add(textField_p);
                textField_p.setBounds(15, 180, 250, 35);

                //---- comboBox_kinds ----
                comboBox_kinds.setModel(new DefaultComboBoxModel<>(new String[] {
                    "Custom",
                    "NIST_P_192",
                    "NIST_P_256",
                    "NIST_P_384",
                    "NIST_P_521"
                }));
                comboBox_kinds.addActionListener(e -> comboBox_kindsActionPerformed(e));
                panel1.add(comboBox_kinds);
                comboBox_kinds.setBounds(285, 180, 110, 35);

                //---- button_gpriKey ----
                button_gpriKey.setText("Generate Private Key");
                button_gpriKey.addActionListener(e -> button_gpriKeyActionPerformed(e));
                panel1.add(button_gpriKey);
                button_gpriKey.setBounds(285, 240, button_gpriKey.getPreferredSize().width, 35);

                //---- button_gpubKey ----
                button_gpubKey.setText("Generate Public Key");
                button_gpubKey.addActionListener(e -> button_gpubKeyActionPerformed(e));
                panel1.add(button_gpubKey);
                button_gpubKey.setBounds(110, 240, button_gpubKey.getPreferredSize().width, 35);

                //---- button_process ----
                button_process.setText("Process");
                button_process.addActionListener(e -> button_processActionPerformed(e));
                panel1.add(button_process);
                button_process.setBounds(430, 180, 80, 35);

                //---- label_basePoineX ----
                label_basePoineX.setText("Base Point_X:");
                panel1.add(label_basePoineX);
                label_basePoineX.setBounds(290, 20, 85, 30);
                panel1.add(textField_gx);
                textField_gx.setBounds(285, 50, 250, 35);
                panel1.add(textField_gy);
                textField_gy.setBounds(285, 115, 250, 35);

                //---- label_basePointY ----
                label_basePointY.setText("Base Point_Y:");
                panel1.add(label_basePointY);
                label_basePointY.setBounds(290, 85, 85, 30);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel1.getComponentCount(); i++) {
                        Rectangle bounds = panel1.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel1.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel1.setMinimumSize(preferredSize);
                    panel1.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Key Generator", panel1);

            //======== panel2 ========
            {
                panel2.setLayout(null);

                //---- label_inFile1 ----
                label_inFile1.setText("Input File:");
                panel2.add(label_inFile1);
                label_inFile1.setBounds(80, 25, label_inFile1.getPreferredSize().width, 30);
                panel2.add(textField_inFilePath1);
                textField_inFilePath1.setBounds(75, 55, 250, 35);

                //---- button_inFileBro1 ----
                button_inFileBro1.setText("Browse");
                button_inFileBro1.addActionListener(e -> button_inFileBro1ActionPerformed(e));
                panel2.add(button_inFileBro1);
                button_inFileBro1.setBounds(340, 55, button_inFileBro1.getPreferredSize().width, 35);

                //---- label_pubKey ----
                label_pubKey.setText("Public Key:");
                panel2.add(label_pubKey);
                label_pubKey.setBounds(80, 110, label_pubKey.getPreferredSize().width, 30);
                panel2.add(textField_pubKeyPath);
                textField_pubKeyPath.setBounds(75, 140, 250, 35);

                //---- button_pubKeyBro ----
                button_pubKeyBro.setText("Browse");
                button_pubKeyBro.addActionListener(e -> button_pubKeyBroActionPerformed(e));
                panel2.add(button_pubKeyBro);
                button_pubKeyBro.setBounds(340, 140, button_pubKeyBro.getPreferredSize().width, 35);

                //---- button_encrypt ----
                button_encrypt.setText("Encrypt");
                button_encrypt.addActionListener(e -> button_encryptActionPerformed(e));
                panel2.add(button_encrypt);
                button_encrypt.setBounds(185, 205, 100, 35);

                //---- label_time1 ----
                label_time1.setText("Time:");
                panel2.add(label_time1);
                label_time1.setBounds(100, 260, 80, 35);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel2.getComponentCount(); i++) {
                        Rectangle bounds = panel2.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel2.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel2.setMinimumSize(preferredSize);
                    panel2.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Encryption", panel2);

            //======== panel3 ========
            {
                panel3.setLayout(null);

                //---- label_inFile2 ----
                label_inFile2.setText("Input File:");
                panel3.add(label_inFile2);
                label_inFile2.setBounds(80, 25, label_inFile2.getPreferredSize().width, 30);
                panel3.add(textField_inFilePath2);
                textField_inFilePath2.setBounds(75, 55, 250, 35);

                //---- label_priKey ----
                label_priKey.setText("Private Key:");
                panel3.add(label_priKey);
                label_priKey.setBounds(80, 110, label_priKey.getPreferredSize().width, 30);
                panel3.add(textField_privKeyPath);
                textField_privKeyPath.setBounds(75, 140, 250, 35);

                //---- button_inFileBro2 ----
                button_inFileBro2.setText("Browse");
                button_inFileBro2.addActionListener(e -> button_inFileBro2ActionPerformed(e));
                panel3.add(button_inFileBro2);
                button_inFileBro2.setBounds(340, 55, button_inFileBro2.getPreferredSize().width, 35);

                //---- button_priKeyBro ----
                button_priKeyBro.setText("Browse");
                button_priKeyBro.addActionListener(e -> button_priKeyBroActionPerformed(e));
                panel3.add(button_priKeyBro);
                button_priKeyBro.setBounds(340, 140, button_priKeyBro.getPreferredSize().width, 35);

                //---- button_decrypt ----
                button_decrypt.setText("Decrypt");
                button_decrypt.addActionListener(e -> button_decryptActionPerformed(e));
                panel3.add(button_decrypt);
                button_decrypt.setBounds(185, 205, 100, 35);

                //---- label_time2 ----
                label_time2.setText("Time:");
                panel3.add(label_time2);
                label_time2.setBounds(100, 260, 80, 35);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel3.getComponentCount(); i++) {
                        Rectangle bounds = panel3.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel3.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel3.setMinimumSize(preferredSize);
                    panel3.setPreferredSize(preferredSize);
                }
            }
            tabbedPane.addTab("Decryption", panel3);
        }
        contentPane.add(tabbedPane);
        tabbedPane.setBounds(0, 0, 550, 350);

        { // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private KeyPair keyPair;
    private PublicKey pubKey;
    private  PrivateKey priKey;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane;
    private JPanel panel1;
    private JLabel label_a;
    private JLabel label_b;
    private JLabel label_p;
    private JTextField textField_a;
    private JTextField textField_b;
    private JTextField textField_p;
    private JComboBox<String> comboBox_kinds;
    private JButton button_gpriKey;
    private JButton button_gpubKey;
    private JButton button_process;
    private JLabel label_basePoineX;
    private JTextField textField_gx;
    private JTextField textField_gy;
    private JLabel label_basePointY;
    private JPanel panel2;
    private JLabel label_inFile1;
    private JTextField textField_inFilePath1;
    private JButton button_inFileBro1;
    private JLabel label_pubKey;
    private JTextField textField_pubKeyPath;
    private JButton button_pubKeyBro;
    private JButton button_encrypt;
    private JLabel label_time1;
    private JPanel panel3;
    private JLabel label_inFile2;
    private JTextField textField_inFilePath2;
    private JLabel label_priKey;
    private JTextField textField_privKeyPath;
    private JButton button_inFileBro2;
    private JButton button_priKeyBro;
    private JButton button_decrypt;
    private JLabel label_time2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
