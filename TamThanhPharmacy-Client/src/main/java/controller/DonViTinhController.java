package controller;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import service.DonViTinhService;
import entity.DonViTinh;
import gui.DonVi_GUI;
import utils.ToolCtrl;

public class DonViTinhController {

    public DonVi_GUI dvGUI;
    public ToolCtrl tool = new ToolCtrl();
    public DonViTinhService dvService = new DonViTinhService();

    public boolean isHienThi = true;

    public DonViTinhController(DonVi_GUI donVi_GUI) {
        this.dvGUI = donVi_GUI;
    }

    public void setDataChoTable(ArrayList<DonViTinh> list) {
        DefaultTableModel model = (DefaultTableModel) dvGUI.tblDonVi.getModel();
        model.setRowCount(0);
        int stt = 1;

        for(DonViTinh dvt : list) {
            model.addRow(new Object[] {
                    stt++,
                    dvt.getMaDVT(),
                    dvt.getTenDVT()
            });
        }
    }

    // ĐÃ BỌC SWING WORKER ĐỂ TẢI NGẦM
    public void locTatCa(boolean isHienThi) {
        new SwingWorker<ArrayList<DonViTinh>, Void>() {
            @Override
            protected ArrayList<DonViTinh> doInBackground() {
                // Lấy dữ liệu qua mạng (Block luồng phụ, không block UI)
                return dvService.layDanhSachTheoTrangThai(isHienThi);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<DonViTinh> fullList = get();
                    ArrayList<DonViTinh> ketQua = new ArrayList<>();
                    String tuKhoa = dvGUI.txtTimDV.getText().trim().toLowerCase();

                    if (fullList != null) {
                        for (DonViTinh dvt : fullList) {
                            boolean matchTen = tuKhoa.isEmpty() || dvt.getTenDVT().toLowerCase().contains(tuKhoa);
                            boolean matchTrangThai = (dvt.getTrangThai() == isHienThi);

                            if (matchTen && matchTrangThai) {
                                ketQua.add(dvt);
                            }
                        }
                    }
                    setDataChoTable(ketQua);
                } catch (Exception e) {
                    e.printStackTrace();
                    tool.hienThiThongBao("Lỗi", "Tải danh sách đơn vị thất bại!", false);
                }
            }
        }.execute();
    }

    // ĐÃ BỌC THREAD ĐỂ KIỂM TRA TRÙNG LẶP & THÊM DỮ LIỆU
    public void themDonVi() {
        String tenDV = dvGUI.txtTenDV.getText().trim();

        if (tenDV.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng nhập tên đơn vị!", false);
            dvGUI.txtTenDV.requestFocus();
            return;
        }

        new Thread(() -> {
            try {
                // Gọi mạng kiểm tra trùng
                if (dvService.timTheoTen(tenDV) != null) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Tên đơn vị đã tồn tại!", false));
                    return;
                }

                String maDV = tool.taoKhoaChinh("DVT");
                DonViTinh dvt = new DonViTinh(maDV, tenDV, true);

                // Gọi mạng thêm
                boolean kq = dvService.themDVT(dvt);

                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thành công", "Thêm đơn vị thành công!", true);
                        dvGUI.txtTenDV.setText("");
                        locTatCa(isHienThi); // Load lại dữ liệu
                    } else {
                        tool.hienThiThongBao("Thất bại", "Thêm đơn vị thất bại!", false);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi mạng", "Có lỗi khi kết nối Server!", false));
            }
        }).start();
    }

    // ĐÃ BỌC THREAD XÓA/KHÔI PHỤC
    public void xoaDonVi() {
        int selectedRow = dvGUI.tblDonVi.getSelectedRow();
        if (selectedRow == -1) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn đơn vị cần thao tác!", false);
            return;
        }

        String maDVT = dvGUI.tblDonVi.getValueAt(selectedRow, 1).toString();
        boolean isXoa = dvGUI.btnXoa.getText().equalsIgnoreCase("Xoá");

        String title = isXoa ? "Xóa đơn vị" : "Khôi phục đơn vị";
        String msgXacNhan = isXoa ? "Xác nhận xóa đơn vị này?" : "Xác nhận khôi phục đơn vị này?";

        if (tool.hienThiXacNhan(title, msgXacNhan, null)) {
            new Thread(() -> {
                boolean kq = isXoa ? dvService.xoaDVT(maDVT) : dvService.khoiPhucDVT(maDVT);

                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thông báo", "Thao tác thành công!", true);
                        locTatCa(isHienThi);
                    } else {
                        tool.hienThiThongBao("Lỗi", "Thao tác thất bại!", false);
                    }
                });
            }).start();
        }
    }

    public void xuLyBtnLichSuXoa() {
        isHienThi = !isHienThi;
        dvGUI.btnLichSuXoa.setText(!isHienThi ? "Danh sách hiện tại" : "Lịch sử xoá");
        dvGUI.btnXoa.setText(!isHienThi ? "Khôi phục" : "Xoá");
        locTatCa(isHienThi);
    }

    public void lamMoi() {
        dvGUI.txtTimDV.setText("");
        if (!isHienThi) {
            xuLyBtnLichSuXoa();
        } else {
            locTatCa(true);
        }
    }
}