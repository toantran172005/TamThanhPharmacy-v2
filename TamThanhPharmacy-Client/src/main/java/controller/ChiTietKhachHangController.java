package controller;

import javax.swing.SwingUtilities;
import entity.KhachHang;
import gui.ChiTietKhachHang_GUI;
import gui.TimKiemKH_GUI;
import service.KhachHangService;
import utils.ToolCtrl;

public class ChiTietKhachHangController {

    public ChiTietKhachHang_GUI ctkhGUI;
    public ToolCtrl tool;
    public KhachHangService khService;

    public ChiTietKhachHangController(ChiTietKhachHang_GUI ctkhGUI) {
        this.ctkhGUI = ctkhGUI;
        tool = new ToolCtrl();
        khService = new KhachHangService();
    }

    public void capNhatThongTin() {
        if (ctkhGUI.btnCapNhat.getText().trim().equals("Cập nhật")) {
            ctkhGUI.btnCapNhat.setText("Lưu");
            choPhepEdit(true);
        } else {
            if (tool.hienThiXacNhan("Cập nhật", "Xác nhận lưu thay đổi?", null)) {
                if (!ktTenKhachHangHopLe() || !ktSoDienThoaiHopLe() || !ktTuoiHopLe()) {
                    return;
                }

                String sdtMoi = tool.chuyenSoDienThoai(ctkhGUI.txtSdt.getText().trim());
                String maHT = ctkhGUI.txtMaKH.getText().trim();

                new Thread(() -> {
                    KhachHang khCheck = khService.timKhachHangTheoSDT(sdtMoi);

                    if (khCheck != null && !khCheck.getMaKH().equals(maHT)) {
                        SwingUtilities.invokeLater(() -> {
                            tool.hienThiThongBao("Lỗi", "Số điện thoại này đã thuộc về khách hàng khác!", false);
                        });
                        return;
                    }

                    KhachHang khUpdate = thuThapThongTin();
                    boolean success = khService.capNhatKhachHang(khUpdate);

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            tool.hienThiThongBao("Cập nhật", "Cập nhật thông tin thành công!", true);
                            ctkhGUI.btnCapNhat.setText("Cập nhật");
                            choPhepEdit(false);
                        } else {
                            tool.hienThiThongBao("Cập nhật", "Thất bại! Vui lòng kiểm tra lại kết nối.", false);
                        }
                    });
                }).start();
            }
        }
    }

    private KhachHang thuThapThongTin() {
        String maKH = ctkhGUI.txtMaKH.getText().trim();
        String tenKH = ctkhGUI.txtTenKH.getText().trim();
        String sdt = tool.chuyenSoDienThoai(ctkhGUI.txtSdt.getText().trim());
        int tuoi = Integer.parseInt(ctkhGUI.txtTuoi.getText().trim());
        boolean trangThai = ctkhGUI.cmbTrangThai.getSelectedItem().equals("Hoạt động");

        return new KhachHang(maKH, tenKH, sdt, tuoi, trangThai);
    }

    public void choPhepEdit(boolean editable) {
        ctkhGUI.txtTenKH.setEditable(editable);
        ctkhGUI.txtSdt.setEditable(editable);
        ctkhGUI.txtTuoi.setEditable(editable);
        ctkhGUI.cmbTrangThai.setEnabled(editable);
    }

    public void quayLaiTKKH() {
        tool.doiPanel(ctkhGUI, new TimKiemKH_GUI());
    }

    // --- Các hàm Validation (Giữ nguyên từ bản cũ nhưng tối ưu thông báo) ---

    public boolean ktTenKhachHangHopLe() {
        String ten = ctkhGUI.txtTenKH.getText().trim();
        if (ten.isEmpty() || !ten.matches("^[\\p{L}\\s]+$")) {
            tool.hienThiThongBao("Lỗi", "Tên không hợp lệ (không để trống, không chứa số/ký tự đặc biệt)", false);
            ctkhGUI.txtTenKH.requestFocus();
            return false;
        }
        return true;
    }

    public boolean ktSoDienThoaiHopLe() {
        String sdt = ctkhGUI.txtSdt.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("^0\\d{9}$")) {
            tool.hienThiThongBao("Lỗi", "Số điện thoại phải 10 số và bắt đầu bằng số 0", false);
            ctkhGUI.txtSdt.requestFocus();
            return false;
        }
        return true;
    }

    public boolean ktTuoiHopLe() {
        try {
            int tuoi = Integer.parseInt(ctkhGUI.txtTuoi.getText().trim());
            if (tuoi < 0 || tuoi > 150) throw new Exception();
        } catch (Exception e) {
            tool.hienThiThongBao("Lỗi", "Tuổi phải là số nguyên dương hợp lệ (0-150)", false);
            ctkhGUI.txtTuoi.requestFocus();
            return false;
        }
        return true;
    }
}