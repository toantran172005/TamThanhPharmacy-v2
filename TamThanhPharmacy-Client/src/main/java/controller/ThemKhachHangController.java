package controller;

import javax.swing.SwingUtilities;
import entity.KhachHang;
import gui.ThemKhachHang_GUI;
import service.KhachHangService;
import utils.ToolCtrl;

public class ThemKhachHangController {
    public ThemKhachHang_GUI tkhGUI;
    public ToolCtrl tool;
    public KhachHangService khService;

    public ThemKhachHangController(ThemKhachHang_GUI tkhGUI) {
        this.tkhGUI = tkhGUI;
        this.tool = new ToolCtrl();
        this.khService = new KhachHangService();
    }

    public void ktTatCaTruocKhiThem() {
        if (ktTenKhachHangHopLe() && ktSoDienThoaiHopLe() && ktTuoiHopLe()) {
            themKhachHang();
        }
    }

    public void themKhachHang() {
        String sdtInput = tool.chuyenSoDienThoai(tkhGUI.txtSdt.getText().trim());
        String tenKH = tkhGUI.txtTenKH.getText().trim();
        String tuoiKH = tkhGUI.txtTuoi.getText().trim();

        if (tool.hienThiXacNhan("Thêm khách hàng", "Xác nhận thêm khách hàng?", null)) {
            new Thread(() -> {
                KhachHang checkKH = khService.timKhachHangTheoSDT(sdtInput);

                SwingUtilities.invokeLater(() -> {
                    if (checkKH != null) {
                        tool.hienThiThongBao("Lỗi", "Số điện thoại này đã tồn tại trong hệ thống!", false);
                    } else {
                        thucThiThem(tenKH, sdtInput, tuoiKH);
                    }
                });
            }).start();
        }
    }

    private void thucThiThem(String ten, String sdt, String tuoi) {
        new Thread(() -> {
            KhachHang kh = new KhachHang();
            kh.setMaKH(tool.taoKhoaChinh("KH"));
            kh.setTenKH(ten);
            kh.setSdt(sdt);
            kh.setTuoi(Integer.parseInt(tuoi));
            kh.setTrangThai(true);

            boolean result = khService.themKhachHang(kh);

            SwingUtilities.invokeLater(() -> {
                if (result) {
                    tool.hienThiThongBao("Thành công", "Đã thêm khách hàng mới!", true);
                    lamMoi();
                } else {
                    tool.hienThiThongBao("Lỗi", "Thêm thất bại (Có thể trùng số điện thoại)!", false);
                }
            });
        }).start();
    }

    public boolean ktTenKhachHangHopLe() {
        String ten = tkhGUI.txtTenKH.getText().trim();
        String regex = "^[\\p{L}\\s]+$";

        if (ten.isEmpty()) {
            tool.hienThiThongBao("Tên khách hàng không hợp lệ!", "Tên không được để trống", false);
            tkhGUI.txtTenKH.requestFocus();
            return false;
        } else if (!ten.matches(regex)) {
            tool.hienThiThongBao("Tên khách hàng không hợp lệ!", "Tên không được chứa số hoặc ký tự đặc biệt", false);
            tkhGUI.txtTenKH.requestFocus();
            tkhGUI.txtTenKH.selectAll();
            return false;
        }
        tkhGUI.txtSdt.requestFocus();
        return true;
    }

    public boolean ktSoDienThoaiHopLe() {
        String sdt = tkhGUI.txtSdt.getText().trim();
        String regex = "^0\\d{9}$";

        if (sdt.isEmpty()) {
            tool.hienThiThongBao("Số điện thoại không hợp lệ!", "Không được để trống", false);
            tkhGUI.txtSdt.requestFocus();
            return false;
        } else if (!sdt.matches(regex)) {
            tool.hienThiThongBao("Số điện thoại không hợp lệ!", "Phải gồm 10 chữ số và bắt đầu bằng 0", false);
            tkhGUI.txtSdt.requestFocus();
            tkhGUI.txtSdt.selectAll();
            return false;
        }
        tkhGUI.txtTuoi.requestFocus();
        return true;
    }

    public boolean ktTuoiHopLe() {
        String tuoiStr = tkhGUI.txtTuoi.getText().trim();

        try {
            int tuoi = Integer.parseInt(tuoiStr);

            if (tuoi < 0) {
                tool.hienThiThongBao("Tuổi không hợp lệ!", "Tuổi không được là số âm.", false);
                tkhGUI.txtTuoi.requestFocus();
                tkhGUI.txtTuoi.selectAll();
                return false;
            }
        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Tuổi không hợp lệ!", "Tuổi phải là số nguyên.", false);
            tkhGUI.txtTuoi.requestFocus();
            tkhGUI.txtTuoi.selectAll();
            return false;
        }
        return true;
    }

    public void lamMoi() {
        tkhGUI.txtTenKH.setText("");
        tkhGUI.txtSdt.setText("");
        tkhGUI.txtTuoi.setText("");
        tkhGUI.txtTenKH.requestFocus();
    }

}