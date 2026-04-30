package controller;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import entity.Thue;
import gui.Thue_GUI;
import service.ThueService;
import utils.ToolCtrl;

public class ThueController {

    public Thue_GUI thueGUI;
    public ThueService thueService = new ThueService();
    public ToolCtrl tool = new ToolCtrl();

    public ThueController(Thue_GUI thue_GUI) {
        this.thueGUI = thue_GUI;
    }

    // ĐÃ BỌC SWINGWORKER
    public void loadData() {
        new SwingWorker<ArrayList<Thue>, Void>() {
            @Override
            protected ArrayList<Thue> doInBackground() {
                // Lấy data ngầm qua mạng
                return thueService.layListThue();
            }

            @Override
            protected void done() {
                try {
                    hienThiLenBang(get());
                } catch (Exception e) {
                    e.printStackTrace();
                    tool.hienThiThongBao("Lỗi", "Không thể tải danh sách Thuế", false);
                }
            }
        }.execute();
    }

    private void hienThiLenBang(ArrayList<Thue> list) {
        DefaultTableModel model = (DefaultTableModel) thueGUI.tblThue.getModel();
        model.setRowCount(0);
        for (Thue t : list) {
            double tyLeHienThi = t.getTyLeThue() * 100;
            String tyLeStr;
            if (tyLeHienThi == (long) tyLeHienThi) {
                tyLeStr = String.format("%d%%", (long) tyLeHienThi);
            } else {
                tyLeStr = String.format("%s%%", tyLeHienThi);
            }

            model.addRow(new Object[] {
                    t.getMaThue(),
                    t.getLoaiThue(),
                    tyLeStr,
                    t.getMoTa()
            });
        }
    }

    // ĐÃ BỌC THREAD
    public void themThue() {
        String loaiThue = thueGUI.txtLoaiThue.getText().trim();
        String tyLeStr = thueGUI.txtTyLeThue.getText().trim();
        String moTa = thueGUI.txtMoTa.getText().trim();

        if (loaiThue.isEmpty() || tyLeStr.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng nhập tên loại thuế và tỷ lệ!", false);
            return;
        }

        try {
            double tyLeInput = Double.parseDouble(tyLeStr);
            if (tyLeInput < 0) {
                tool.hienThiThongBao("Lỗi", "Tỷ lệ thuế không được âm!", false);
                return;
            }

            double tyLeLuu = tyLeInput / 100.0;
            String maThue = tool.taoKhoaChinh("T");
            Boolean trangThai = true;

            Thue t = new Thue(maThue, loaiThue, tyLeLuu, moTa, trangThai);

            new Thread(() -> {
                boolean kq = thueService.themThue(t);

                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thành công", "Thêm loại thuế mới thành công!", true);
                        lamMoi(); // Đã chứa logic gọi ngầm loadData()
                    } else {
                        tool.hienThiThongBao("Thất bại", "Thêm thất bại!", false);
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Lỗi", "Tỷ lệ thuế phải là số!", false);
        }
    }

    // ĐÃ BỌC THREAD
    public void suaThue() {
        int row = thueGUI.tblThue.getSelectedRow();
        if (row < 0) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn dòng cần sửa!", false);
            return;
        }

        String maThue = thueGUI.txtMaThue.getText().trim();
        String loaiThue = thueGUI.txtLoaiThue.getText().trim();
        String tyLeStr = thueGUI.txtTyLeThue.getText().trim();
        String moTa = thueGUI.txtMoTa.getText().trim();
        Boolean trangThai = true;

        try {
            double tyLeInput = Double.parseDouble(tyLeStr);
            double tyLeLuu = tyLeInput / 100.0;

            Thue t = new Thue(maThue, loaiThue, tyLeLuu, moTa, trangThai);
            t.setTrangThai(true);

            new Thread(() -> {
                boolean kq = thueService.capNhatThue(t);

                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thành công", "Cập nhật thành công!", true);
                        lamMoi();
                    } else {
                        tool.hienThiThongBao("Thất bại", "Cập nhật thất bại!", false);
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Lỗi", "Tỷ lệ thuế không hợp lệ!", false);
        }
    }

    // ĐÃ BỌC THREAD
    public void xoaThue() {
        int row = thueGUI.tblThue.getSelectedRow();
        if (row < 0) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn dòng cần xóa!", false);
            return;
        }
        String maThue = thueGUI.tblThue.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(thueGUI,
                "Bạn có chắc chắn muốn NGƯNG SỬ DỤNG loại thuế này?\n",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                boolean kq = thueService.xoaThue(maThue);

                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thành công", "Đã xóa (ngưng sử dụng) loại thuế!", true);
                        lamMoi();
                    } else {
                        tool.hienThiThongBao("Thất bại", "Không tìm thấy dữ liệu hoặc có lỗi xảy ra!", false);
                    }
                });
            }).start();
        }
    }

    public void lamMoi() {
        thueGUI.txtMaThue.setText("Tự động tạo...");
        thueGUI.txtLoaiThue.setText("");
        thueGUI.txtTyLeThue.setText("");
        thueGUI.txtMoTa.setText("");
        thueGUI.txtTimKiem.setText("");
        thueGUI.tblThue.clearSelection();
        loadData(); // Gọi SwingWorker tải lại bảng
    }

    // ĐÃ BỌC SWINGWORKER
    public void timKiem() {
        String tuKhoa = thueGUI.txtTimKiem.getText().trim();

        new SwingWorker<ArrayList<Thue>, Void>() {
            @Override
            protected ArrayList<Thue> doInBackground() {
                return thueService.timKiem(tuKhoa);
            }

            @Override
            protected void done() {
                try {
                    hienThiLenBang(get());
                } catch (Exception e) {
                    tool.hienThiThongBao("Lỗi", "Lỗi trong quá trình tìm kiếm", false);
                }
            }
        }.execute();
    }
}