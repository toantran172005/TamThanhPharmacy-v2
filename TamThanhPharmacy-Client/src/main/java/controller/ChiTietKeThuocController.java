package controller;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;

import entity.KeThuoc;
import entity.Thuoc;
import gui.ChiTietKeThuoc_GUI;
import gui.DanhSachKeThuoc_GUI;
import service.KeThuocService;
import utils.ToolCtrl;

public class ChiTietKeThuocController {

    public ChiTietKeThuoc_GUI ctktGUI;
    public ToolCtrl tool;

    private KeThuocService keService;
    public ArrayList<Thuoc> listThuoc;

    public ChiTietKeThuocController(ChiTietKeThuoc_GUI ctktGUI) {
        super();
        this.ctktGUI = ctktGUI;
        this.tool = new ToolCtrl();
        this.keService = new KeThuocService();
        this.listThuoc = new ArrayList<>();

        loadDanhSachThuocAsync(ctktGUI.ke.getMaKe());
    }

    private void loadDanhSachThuocAsync(String maKe) {
        SwingWorker<List<Thuoc>, Void> worker = new SwingWorker<List<Thuoc>, Void>() {
            @Override
            protected List<Thuoc> doInBackground() throws Exception {
                // Gọi mạng ở luồng nền
                return keService.layListThuocTrongKe(maKe);
            }

            @Override
            protected void done() {
                try {
                    List<Thuoc> dsThuoc = get();
                    if (dsThuoc != null) {
                        listThuoc = new ArrayList<>(dsThuoc);
                        setDataChoTable(listThuoc); // Đổ dữ liệu an toàn lên giao diện
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    public void loadTatCaTenKeAsync() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                // Chạy ngầm: Gửi Request qua Socket để lấy danh sách tên kệ
                return keService.layTatCaTenKe();
            }

            @Override
            protected void done() {
                try {
                    // Khi Server trả về kết quả, luồng này sẽ tự động chạy để cập nhật UI
                    List<String> listTenKe = get();
                    if (listTenKe != null) {
                        ctktGUI.cmbLoaiKe.removeAllItems(); // Xóa dữ liệu cũ (nếu có)
                        for (String tenKe : listTenKe) {
                            ctktGUI.cmbLoaiKe.addItem(tenKe); // Thêm dữ liệu mới
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute(); // Bắt đầu thực thi luồng
    }

    public void xuLyCapNhat() {
        if (ctktGUI.btnCapNhat.getText().equals("Cập nhật")) {
            choPhepEdit(true);
            ctktGUI.btnCapNhat.setText("Lưu");
        } else {
            if (tool.hienThiXacNhan("Cập nhật kệ thuốc", "Xác nhận cập nhật kệ thuốc?", null)) {
                KeThuoc kt = kiemTraThongTin();
                if (kt == null) {
                    return;
                }

                // Dùng Thread để thực thi cập nhật Database qua Socket
                new Thread(() -> {
                    boolean isSuccess = keService.capNhatKeThuoc(kt);

                    // Trả kết quả về luồng UI chính
                    SwingUtilities.invokeLater(() -> {
                        if (isSuccess) {
                            tool.hienThiThongBao("Cập nhật kệ thuốc", "Cập nhật kệ thuốc thành công!", true);
                            choPhepEdit(false);
                            ctktGUI.btnCapNhat.setText("Cập nhật");
                        } else {
                            tool.hienThiThongBao("Cập nhật kệ thuốc", "Không thể cập nhật kệ thuốc!", false);
                        }
                    });
                }).start();
            }
        }
    }

    public KeThuoc kiemTraThongTin() {
        if (kiemTraCmb() && kiemTraSucChua()) {
            String maKe = ctktGUI.txtMaKe.getText().trim();
            String loaiKe = ctktGUI.cmbLoaiKe.getSelectedItem().toString();
            int sucChua = Integer.valueOf(ctktGUI.txtSucChua.getText().trim());
            String moTa = ctktGUI.txaMoTa.getText().trim();
            String trangThai = ctktGUI.cmbTrangThai.getSelectedItem().toString();
            if (trangThai.equals("Hoạt động")) {
                return new KeThuoc(maKe, loaiKe, sucChua, moTa, true);
            } else {
                return new KeThuoc(maKe, loaiKe, sucChua, moTa, false);
            }
        }
        return null;
    }

    public boolean kiemTraCmb() {
        String text = ctktGUI.cmbLoaiKe.getSelectedItem().toString();

        if (text == null || text.trim().isEmpty()) {
            tool.hienThiThongBao("Lỗi ComboBox", "Vui lòng chọn hoặc nhập loại kệ!", false);
            return false;
        }
        ctktGUI.cmbLoaiKe.setSelectedItem(text.trim());
        return true;
    }

    public boolean kiemTraSucChua() {
        try {
            int sucChua = Integer.parseInt(ctktGUI.txtSucChua.getText().trim());

            if (sucChua > 900) {
                tool.hienThiThongBao("Lỗi sức chứa", "Sức chứa phải dưới 900!", false);
                ctktGUI.txtSucChua.selectAll();
                return false;
            }
            return true;

        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Lỗi sức chứa", "Vui lòng nhập số cho sức chứa!", false);
            ctktGUI.txtSucChua.selectAll();
            return false;
        }
    }

    public void quayLaiDS() {
        tool.doiPanel(ctktGUI, new DanhSachKeThuoc_GUI());
    }

    public void choPhepEdit(boolean editable) {
        ctktGUI.txtSucChua.setEditable(editable);
        ctktGUI.txaMoTa.setEditable(editable);

        ctktGUI.cmbLoaiKe.setEnabled(editable);
        ctktGUI.cmbTrangThai.setEnabled(editable);
    }

    public void setDataChoTable(ArrayList<Thuoc> list) {
        ctktGUI.model.setRowCount(0);

        for (Thuoc thuoc : list) {
            Object[] row = { thuoc.getMaThuoc(), thuoc.getTenThuoc(), thuoc.getTrangThai() ? "Hoạt động" : "Đã xóa" };
            ctktGUI.model.addRow(row);
        }
    }
}