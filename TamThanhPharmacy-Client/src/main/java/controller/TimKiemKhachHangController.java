package controller;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import entity.KhachHang;
import gui.ChiTietKhachHang_GUI;
import gui.TimKiemKH_GUI;
import service.KhachHangService;
import utils.ToolCtrl;

public class TimKiemKhachHangController {

    public KhachHangService khService = new KhachHangService();
    public ToolCtrl tool = new ToolCtrl();
    public boolean hienThiHoatDong = true;
    public ArrayList<KhachHang> listKH = new ArrayList<>();
    public TimKiemKH_GUI tkkhGUI;

    public TimKiemKhachHangController(TimKiemKH_GUI tkkhGUI) {
        this.tkkhGUI = tkkhGUI;
    }

    public void xemChiTietKH() {
        int selectedRow = tkkhGUI.tblKhachHang.getSelectedRow();
        if (selectedRow == -1) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn một khách hàng!", false);
            return;
        }
        int modelRow = tkkhGUI.tblKhachHang.convertRowIndexToModel(selectedRow);
        String maKH = tkkhGUI.tblKhachHang.getModel().getValueAt(modelRow, 0).toString();

        KhachHang selected = null;
        for (KhachHang k : listKH) {
            if (k.getMaKH().equals(maKH)) {
                selected = k;
                break;
            }
        }

        if (selected != null) {
            tool.doiPanel(tkkhGUI, new ChiTietKhachHang_GUI(selected));
        }
    }

    public void xoaKhachHang() {
        int viewRow = tkkhGUI.tblKhachHang.getSelectedRow();
        if (viewRow == -1) {
            tool.hienThiThongBao("Lỗi!", "Vui lòng chọn khách hàng!", false);
            return;
        }

        String title = tkkhGUI.btnXoa.getText().equals("Xóa") ? "Xóa khách hàng" : "Khôi phục khách hàng";
        if (tool.hienThiXacNhan(title, "Xác nhận thực hiện thao tác?", null)) {
            int modelRow = tkkhGUI.tblKhachHang.convertRowIndexToModel(viewRow);
            String maKh = tkkhGUI.tblKhachHang.getModel().getValueAt(modelRow, 0).toString();

            new Thread(() -> {
                boolean result;
                if (tkkhGUI.btnXoa.getText().equals("Xóa")) {
                    result = khService.xoaKhachHang(maKh);
                } else {
                    result = khService.khoiPhucKhachHang(maKh);
                }

                SwingUtilities.invokeLater(() -> {
                    if (result) {
                        tool.hienThiThongBao(title, "Thành công!", true);
                        locTatCa(hienThiHoatDong);
                    } else {
                        tool.hienThiThongBao(title, "Thất bại!", false);
                    }
                });
            }).start();
        }
    }

    public void locTatCa(boolean hienThiHoatDong) {
        new SwingWorker<ArrayList<KhachHang>, Void>() {
            @Override
            protected ArrayList<KhachHang> doInBackground() throws Exception {
                List<KhachHang> fullList = khService.layListKhachHang();
                ArrayList<KhachHang> ketQua = new ArrayList<>();
                String tenKH = tkkhGUI.txtTenKH.getText().trim().toLowerCase();
                String sdt = tool.chuyenSoDienThoai(tkkhGUI.txtSdt.getText().trim());

                for (KhachHang kh : fullList) {
                    boolean trungTen = tenKH.isEmpty() || kh.getTenKH().toLowerCase().contains(tenKH);
                    boolean trungSdt = sdt.isEmpty() || kh.getSdt().contains(sdt);
                    if (trungTen && trungSdt && kh.getTrangThai() == hienThiHoatDong) {
                        ketQua.add(kh);
                    }
                }
                return ketQua;
            }

            @Override
            protected void done() {
                try {
                    listKH = get();
                    setDataChoTable(listKH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void lamMoi() {
        tkkhGUI.txtTenKH.setText("");
        tkkhGUI.txtSdt.setText("");
        hienThiHoatDong = true;
        tkkhGUI.btnLichSuXoa.setText("Lịch sử xóa");
        tkkhGUI.btnXoa.setText("Xóa");
        locTatCa(hienThiHoatDong);
    }

    public void xuLyBtnLichSuXoa() {
        hienThiHoatDong = !hienThiHoatDong;
        tkkhGUI.btnLichSuXoa.setText(!hienThiHoatDong ? "Danh sách hiện tại" : "Lịch sử xóa");
        tkkhGUI.btnXoa.setText(!hienThiHoatDong ? "Khôi phục" : "Xóa");
        locTatCa(hienThiHoatDong);
    }

    public void setDataChoTable(ArrayList<KhachHang> list) {
        tkkhGUI.model.setRowCount(0);
        for (KhachHang kh : list) {
            Object[] row = { kh.getMaKH(), kh.getTenKH(), tool.chuyenSoDienThoai(kh.getSdt()), kh.getTuoi(),
                    kh.getTrangThai() ? "Hoạt động" : "Đã xóa" };
            tkkhGUI.model.addRow(row);
        }
    }
}