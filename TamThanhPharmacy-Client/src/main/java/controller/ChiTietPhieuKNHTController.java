package controller;

import javax.swing.SwingUtilities;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuKhieuNaiHoTroKH;
import gui.ChiTietPhieuKNHT_GUI;
import gui.DanhSachKhieuNaiVaHoTroHK_GUI;
import service.PhieuKNHTService;
import utils.ToolCtrl;

public class ChiTietPhieuKNHTController {
    public ChiTietPhieuKNHT_GUI ctknhtGUI;
    public ToolCtrl tool;
    public PhieuKNHTService service;

    public ChiTietPhieuKNHTController(ChiTietPhieuKNHT_GUI ctknhtGUI) {
        super();
        this.ctknhtGUI = ctknhtGUI;
        this.tool = new ToolCtrl();
        this.service = new PhieuKNHTService();
    }

    public void capNhatPhieuKNHT() {
        if (ctknhtGUI.btnCapNhat.getText().equals("Cập nhật")) {
            ctknhtGUI.btnCapNhat.setText("Hoàn tất");
            choPhepEdit(true);
        } else {
            if (tool.hienThiXacNhan("Cập nhật", "Xác nhận cập nhật thông tin phiếu?", null)) {

                new Thread(() -> {
                    PhieuKhieuNaiHoTroKH knht = kiemTraThongTin();
                    if (knht == null) return;

                    boolean success = service.capNhatPhieu(knht);

                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            tool.hienThiThongBao("Cập nhập", "Cập nhật phiếu thành công!", true);
                            ctknhtGUI.btnCapNhat.setText("Cập nhật");
                            choPhepEdit(false);
                        } else {
                            tool.hienThiThongBao("Lỗi", "Không thể cập nhật phiếu qua hệ thống!", false);
                        }
                    });
                }).start();
            }
        }
    }

    public PhieuKhieuNaiHoTroKH kiemTraThongTin() {
        if (ktTenKhachHangHopLe() && ktTenNhanVienHopLe() && ktSoDienThoaiHopLe() && ktNoiDungHopLe()) {

            String tenKH = ctknhtGUI.txtTenKH.getText().trim();
            String tenNV = ctknhtGUI.txtTenNV.getText().trim();
            String sdt = tool.chuyenSoDienThoai(ctknhtGUI.txtSdt.getText().trim());
            String noiDung = ctknhtGUI.txaNoiDung.getText().trim();
            String loaiDon = ctknhtGUI.cmbLoaiDon.getSelectedItem().toString();
            String trangThai = ctknhtGUI.cmbTrangThai.getSelectedItem().toString();

            PhieuKhieuNaiHoTroKH phieuHienTai = ctknhtGUI.phieu;

            KhachHang kh = phieuHienTai.getKhachHang();
            kh.setTenKH(tenKH);
            kh.setSdt(sdt);

            NhanVien nv = phieuHienTai.getNhanVien();
            nv.setTenNV(tenNV);

            phieuHienTai.setNhanVien(nv);
            phieuHienTai.setKhachHang(kh);
            phieuHienTai.setNoiDung(noiDung);
            phieuHienTai.setLoaiDon(loaiDon);
            phieuHienTai.setTrangThai(trangThai);

            return phieuHienTai;
        }
        return null;
    }


    public boolean ktTenKhachHangHopLe() {
        String ten = ctknhtGUI.txtTenKH.getText().trim();
        String regex = "^[\\p{L}\\s]+$";
        if (ten.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Tên khách hàng không được để trống", false);
                ctknhtGUI.txtTenKH.requestFocus();
            });
            return false;
        } else if (!ten.matches(regex)) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Tên khách hàng không hợp lệ", false);
                ctknhtGUI.txtTenKH.requestFocus();
                ctknhtGUI.txtTenKH.selectAll();
            });
            return false;
        }
        return true;
    }

    public boolean ktTenNhanVienHopLe() {
        String ten = ctknhtGUI.txtTenNV.getText().trim();
        String regex = "^[\\p{L}\\s]+$";
        if (ten.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Tên nhân viên không được để trống", false);
                ctknhtGUI.txtTenNV.requestFocus();
            });
            return false;
        } else if (!ten.matches(regex)) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Tên nhân viên không hợp lệ", false);
                ctknhtGUI.txtTenNV.requestFocus();
                ctknhtGUI.txtTenNV.selectAll();
            });
            return false;
        }
        return true;
    }

    public boolean ktNoiDungHopLe() {
        String nd = ctknhtGUI.txaNoiDung.getText().trim();
        if (nd.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Nội dung không được để trống", false);
                ctknhtGUI.txaNoiDung.requestFocus();
            });
            return false;
        }
        return true;
    }

    public boolean ktSoDienThoaiHopLe() {
        String sdt = ctknhtGUI.txtSdt.getText().trim();
        String regex = "^0\\d{9}$";
        if (sdt.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Số điện thoại không được để trống", false);
                ctknhtGUI.txtSdt.requestFocus();
            });
            return false;
        } else if (!sdt.matches(regex)) {
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Lỗi", "Số điện thoại sai định dạng", false);
                ctknhtGUI.txtSdt.requestFocus();
                ctknhtGUI.txtSdt.selectAll();
            });
            return false;
        }
        return true;
    }

    public void choPhepEdit(boolean edit) {
        ctknhtGUI.txtTenKH.setEditable(edit);
        ctknhtGUI.txtTenNV.setEditable(edit);
        ctknhtGUI.txtSdt.setEditable(edit);
        ctknhtGUI.txaNoiDung.setEditable(edit);
        ctknhtGUI.cmbLoaiDon.setEnabled(edit);
        ctknhtGUI.cmbTrangThai.setEnabled(edit);
    }

    public void quayLaiDanhSachKNHT() {
        DanhSachKhieuNaiVaHoTroHK_GUI dsknhtGUI = new DanhSachKhieuNaiVaHoTroHK_GUI();
        tool.doiPanel(ctknhtGUI, dsknhtGUI);
    }
}