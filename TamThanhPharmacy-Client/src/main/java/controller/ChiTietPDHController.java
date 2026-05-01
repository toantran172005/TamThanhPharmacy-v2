package controller;

import entity.PhieuDatHang;
import gui.ChiTietPhieuDatHang_GUI;
//import gui.LapHoaDon_GUI;
import gui.LapHoaDon_GUI;
import gui.TimKiemPhieuDatHang_GUI;
//import service.DonViTinhService;
import service.DonViTinhService;
import service.PhieuDatHangService;
//import service.ThuocService;
import service.ThuocService;
import utils.ToolCtrl;

import javax.swing.*;

public class ChiTietPDHController {

    public ChiTietPhieuDatHang_GUI ctpdhGUI;
    public ToolCtrl tool = new ToolCtrl();

    public PhieuDatHangService pdhService = new PhieuDatHangService();
    public DonViTinhService dvtService = new DonViTinhService();
    public ThuocService thService = new ThuocService();

    public ChiTietPDHController(ChiTietPhieuDatHang_GUI ctpdhGUI) {
        super();
        this.ctpdhGUI = ctpdhGUI;
    }

    public void taoHoaDon() {
        if (ctpdhGUI.btnCapNhat.getText().equals("Lưu")) {
            tool.hienThiThongBao("Tạo hóa đơn", "Vui lòng lưu phiếu trước khi tạo hóa đơn", false);
            return;
        } else if (ctpdhGUI.pdh.getTrangThai().equals("Đã hủy")) {
            tool.hienThiThongBao("Tạo hóa đơn", "Không thể tạo hóa đơn với phiếu đã hủy!", false);
            return;
        } else if (ctpdhGUI.daChuyenHD) {
            tool.hienThiThongBao("Tạo hóa đơn", "Phiếu này đã tạo hóa đơn rồi!", false);
            return;
        } else {
            String maPDH = ctpdhGUI.getLblMaPhieuDat().getText();

            // Chuyển trang Lập Hóa Đơn có thể cần Load dữ liệu, nên gói vào Thread
            new Thread(() -> {
                SwingUtilities.invokeLater(() -> {
                    LapHoaDon_GUI lapHD = new LapHoaDon_GUI(ctpdhGUI.getMainFrameQL());
                    LapHoaDonController ctrl = new LapHoaDonController(lapHD);
                    ctrl.loadTuPhieuDatHang(maPDH);
                    tool.doiPanel(ctpdhGUI, lapHD);
                });
            }).start();
        }
    }

    public void capNhatPDH() {
        if (ctpdhGUI.daChuyenHD) {
            tool.hienThiThongBao("Cập nhật", "Không được phép cập nhật vì phiếu này đã chuyển thành hóa đơn", false);
            return;
        } else {
            if (ctpdhGUI.btnCapNhat.getText().equals("Cập nhật")) {
                ctpdhGUI.btnCapNhat.setText("Lưu");
                ctpdhGUI.choPhepCapNhap();
            } else {
                if (tool.hienThiXacNhan("Cập nhật", "Xác nhận cập nhật trạng thái?", null)) {
                    capNhatTrangThai();
                }
            }
        }
    }

    public void capNhatTrangThai() {
        String trangThaiMoi = String.valueOf(ctpdhGUI.cmbTrangThai.getSelectedItem());
        String maPDH = ctpdhGUI.pdh.getMaPDH();

        // Khóa nút lưu để tránh spam nhiều request
        ctpdhGUI.btnCapNhat.setEnabled(false);

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return pdhService.capNhatTrangThaiPhieu(maPDH, trangThaiMoi);
            }

            @Override
            protected void done() {
                try {
                    int trangThaiCapNhat = get();

                    if (trangThaiCapNhat == 1) {
                        tool.hienThiThongBao("Cập nhật", "Không đủ số lượng tồn kho để khôi phục phiếu này!", false);
                        ctpdhGUI.cmbTrangThai.setSelectedItem(ctpdhGUI.pdh.getTrangThai());

                    } else if (trangThaiCapNhat == 0) {
                        tool.hienThiThongBao("Cập nhật", "Cập nhật phiếu đặt hàng thành công!", true);

                        // Load lại phiếu mới từ mạng để làm mới giao diện
                        // Vì đã ở trong hàm done(), bọc phần tìm kiếm này vào một luồng nhỏ khác
                        new Thread(() -> {
                            PhieuDatHang pdhNew = pdhService.timTheoMa(maPDH);
                            SwingUtilities.invokeLater(() -> {
                                if(pdhNew != null) {
                                    ctpdhGUI.pdh = pdhNew;
                                }
                                // Mở lại giao diện thành Cập Nhật
                                ctpdhGUI.btnCapNhat.setText("Cập nhật");
                                ctpdhGUI.choPhepCapNhap();
                                ctpdhGUI.btnCapNhat.setEnabled(true);
                            });
                        }).start();
                        return; // Kết thúc sớm để tránh setEnabled(true) bên dưới trước khi Thread kia chạy xong

                    } else if (trangThaiCapNhat == 3) {
                        tool.hienThiThongBao("Cập nhật", "Cập nhật phiếu đặt hàng thành công!", true);
                        ctpdhGUI.btnCapNhat.setText("Cập nhật");
                        ctpdhGUI.choPhepCapNhap();

                    } else {
                        tool.hienThiThongBao("Lỗi", "Lỗi cập nhật dữ liệu.", false);
                        ctpdhGUI.cmbTrangThai.setSelectedItem(ctpdhGUI.pdh.getTrangThai());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    tool.hienThiThongBao("Lỗi", "Lỗi kết nối máy chủ.", false);
                } finally {

                    ctpdhGUI.btnCapNhat.setEnabled(true);
                }
            }
        }.execute();
    }

    public void quayLaiTrangDanhSach() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (ctpdhGUI.mainFrameQL != null) {
                    ctpdhGUI.mainFrameQL.setUpNoiDung(new TimKiemPhieuDatHang_GUI(ctpdhGUI.mainFrameQL));
                } else {
                    ctpdhGUI.mainFrameNV.setUpNoiDung(new TimKiemPhieuDatHang_GUI(ctpdhGUI.mainFrameNV));
                }
            });
        }).start();
    }
}