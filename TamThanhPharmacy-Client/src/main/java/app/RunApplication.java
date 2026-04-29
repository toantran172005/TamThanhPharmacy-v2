package app;

import client.ClientSocketManager;
import controller.DangNhapController;
import gui.DangNhap_GUI;

import javax.swing.*;

public class RunApplication {
    public static void main(String[] args) {

        try {
            ClientSocketManager.getInstance().connect("localhost", 9999);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến Máy Chủ!");
        }

        DangNhap_GUI dangNhapGUI = new DangNhap_GUI();

        new DangNhapController(dangNhapGUI);

        dangNhapGUI.setVisible(true);
    }
}
