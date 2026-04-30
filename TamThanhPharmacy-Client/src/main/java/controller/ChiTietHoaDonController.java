package controller;

import service.HoaDonService;
import service.PhieuDoiTraService;
import service.ThuocService;
import entity.HoaDon;
import gui.*;
import utils.ToolCtrl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonController {
    public ChiTietHoaDon_GUI gui;
    public HoaDonService hdService = new HoaDonService();
    public ToolCtrl tool = new ToolCtrl();
    public ThuocService thuocService = new ThuocService();
    public PhieuDoiTraService pdtService = new PhieuDoiTraService();
    public HoaDon hd;

    public ChiTietHoaDonController(ChiTietHoaDon_GUI gui) {
        this.gui = gui;
        suKien();
    }

    public void suKien() {
        gui.getBtnQuayLai().addActionListener(e -> quayLai());
        gui.getBtnInHoaDon().addActionListener(e -> xuatHoaDonRaTXT());
        gui.getBtnTaoPhieuDoiTra().addActionListener(e -> taoPhieuDoiTra());
    }

    // ĐÃ BỌC SWINGWORKER (Tính tổng tiền ngầm)
    public void hienThiThongTinHoaDon(HoaDon hd) {
        if (hd == null) return;
        this.hd = hd;

        // Cập nhật các thông tin cơ bản có sẵn (Chạy ngay trên UI)
        gui.getLblMaHD().setText(hd.getMaHD());
        gui.getLblNgayLap().setText(tool.dinhDangLocalDate(hd.getNgayLap()));
        gui.getLblNhanVien().setText(hd.getNhanVien() != null ? hd.getNhanVien().getTenNV() : "Không xác định");
        gui.getLblKhachHang().setText(hd.getKhachHang() != null ? hd.getKhachHang().getTenKH() : "Khách lẻ");
        gui.getLblGhiChu().setText(hd.getGhiChu() != null ? hd.getGhiChu() : "Không có");
        gui.getLblDiaChi().setText(hd.getDiaChiHT() != null ? hd.getDiaChiHT() : "Chưa có");
        gui.getLblHotline().setText(hd.getHotline() != null ? tool.chuyenSoDienThoai(hd.getHotline()) : "Chưa có");

        // Gọi mạng ngầm để tính tiền
        new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() {
                return hdService.tinhTongTienTheoHoaDon(hd.getMaHD());
            }

            @Override
            protected void done() {
                try {
                    double tongTien = get();
                    double tienNhan = hd.getTienNhan();
                    double tienThua = Math.max(0, tienNhan - tongTien);

                    gui.getLblTongTien().setText(tool.dinhDangVND(tongTien));
                    gui.getLblTienNhan().setText(tool.dinhDangVND(tienNhan));
                    gui.getLblTienThua().setText(tool.dinhDangVND(tienThua));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();

        capNhatBangChiTiet(hd.getMaHD());
    }

    // ĐÃ BỌC SWINGWORKER (Tránh lỗi N+1 Query làm giật UI khi duyệt list)
    public void capNhatBangChiTiet(String maHD) {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> chiTietList = new ArrayList<>(hdService.layChiTietHoaDon(maHD));
                List<Object[]> enrichedList = new ArrayList<>();

                for (Object[] ct : chiTietList) {
                    // Truy vấn ngầm N lần (không làm kẹt UI)
                    String noiSanXuat = thuocService.timTenQGTheoMaThuoc(ct[1].toString());
                    enrichedList.add(new Object[] {
                            ct[2], // tenThuoc
                            noiSanXuat,
                            ct[3], // soLuong
                            ct[5], // donVi
                            ct[6], // donGia
                            ct[7]  // thanhTien
                    });
                }
                return enrichedList;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel model = (DefaultTableModel) gui.getTblThuoc().getModel();
                    model.setRowCount(0);

                    for (Object[] ct : get()) {
                        model.addRow(new Object[] {
                                ct[0],
                                ct[1],
                                ct[2],
                                ct[3],
                                tool.dinhDangVND(ct[4] instanceof Number ? ((Number) ct[4]).doubleValue() : 0),
                                tool.dinhDangVND(ct[5] instanceof Number ? ((Number) ct[5]).doubleValue() : 0)
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // ĐÃ BỌC THREAD ĐỂ LƯU FILE KHÔNG GÂY TREO UI
    public void xuatHoaDonRaTXT() {
        String maHD = gui.getLblMaHD().getText();
        if (maHD == null || maHD.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Không có hóa đơn để xuất!", false);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn (.txt)");
        chooser.setSelectedFile(new File("HoaDon_" + maHD + ".txt"));

        if (chooser.showSaveDialog(gui) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        // Đọc dữ liệu từ UI trước khi đưa vào Thread để tránh xung đột
        String ngayLap = gui.getLblNgayLap().getText();
        String nhanVien = gui.getLblNhanVien().getText();
        String khachHang = gui.getLblKhachHang().getText();
        String diaChi = gui.getLblDiaChi().getText();
        String hotline = gui.getLblHotline().getText();
        String ghiChu = gui.getLblGhiChu().getText();
        String tongTien = gui.getLblTongTien().getText();
        String tienNhan = gui.getLblTienNhan().getText();
        String tienThua = gui.getLblTienThua().getText();

        DefaultTableModel model = (DefaultTableModel) gui.getTblThuoc().getModel();
        List<Object[]> tableData = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            tableData.add(new Object[] {
                    model.getValueAt(i, 0), model.getValueAt(i, 1), model.getValueAt(i, 2),
                    model.getValueAt(i, 3), model.getValueAt(i, 4), model.getValueAt(i, 5)
            });
        }

        new Thread(() -> {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("=========================================\n");
                bw.write("         HÓA ĐƠN BÁN THUỐC TAM THANH       \n");
                bw.write("=========================================\n\n");

                bw.write("Mã hóa đơn   : " + maHD + "\n");
                bw.write("Ngày lập     : " + ngayLap + "\n");
                bw.write("Nhân viên    : " + nhanVien + "\n");
                bw.write("Khách hàng   : " + khachHang + "\n");
                bw.write("Địa chỉ      : " + diaChi + "\n");
                bw.write("Hotline      : " + hotline + "\n");
                bw.write("Ghi chú      : " + ghiChu + "\n\n");

                bw.write(String.format("%-25s %-15s %-8s %-10s %-15s %-15s%n", "Tên thuốc", "Nơi SX", "SL", "Đơn vị", "Đơn giá", "Thành tiền"));
                bw.write("--------------------------------------------------------------------------\n");

                for (Object[] row : tableData) {
                    bw.write(String.format("%-25s %-15s %-8s %-10s %-15s %-15s%n",
                            row[0], row[1], row[2], row[3], row[4], row[5]));
                }

                bw.write("\n-----------------------------------------\n");
                bw.write("Tổng tiền : " + tongTien + "\n");
                bw.write("Tiền nhận : " + tienNhan + "\n");
                bw.write("Tiền thừa : " + tienThua + "\n");

                bw.write("\n-----------------------------------------\n");
                bw.write("(Giá trên đã bao gồm thuế GTGT)\n");
                bw.write("\n=========================================\n");
                bw.write("   CẢM ƠN QUÝ KHÁCH - HẸN GẶP LẠI!   \n");
                bw.write("=========================================\n");

                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Xuất hóa đơn", "Xuất file TXT thành công!", true));
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không thể ghi file TXT!", false));
            }
        }).start();
    }

    public void quayLai() {
        if (gui.getMainFrameQL() != null) {
            tool.doiPanel(gui, new TimKiemHD_GUI(gui.getMainFrameQL()));
        } else if (gui.getMainFrameNV() != null) {
            tool.doiPanel(gui, new TimKiemHD_GUI(gui.getMainFrameNV()));
        }
    }

    // ĐÃ BỌC SWINGWORKER (Do có gọi Service kiểm tra hoá đơn)
    public void taoPhieuDoiTra() {
        String maHD = gui.getLblMaHD().getText();
        if (maHD.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Không có hóa đơn để đổi trả!", false);
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                // Kiểm tra database
                return pdtService.kiemTraHoaDonDaDoiTra(hd.getMaHD());
            }

            @Override
            protected void done() {
                try {
                    boolean daDoiTra = get();
                    if (daDoiTra) {
                        tool.hienThiThongBao("Tạo phiếu đổi trả", "Hóa đơn này đã tạo phiếu đổi trả", false);
                    } else {
                        LapPhieuDoiTra_GUI panel = new LapPhieuDoiTra_GUI();
                        panel.setTrangChuQL(gui.getMainFrameQL());
                        panel.setTrangChuNV(gui.getMainFrameNV());

                        LapPhieuDoiTraController ctrl = new LapPhieuDoiTraController(panel);
                        ctrl.setMaHD(maHD);

                        if (gui.getMainFrameQL() != null) {
                            gui.getMainFrameQL().setUpNoiDung(panel);
                        } else if (gui.getMainFrameNV() != null) {
                            gui.getMainFrameNV().setUpNoiDung(panel);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    tool.hienThiThongBao("Lỗi", "Có lỗi xảy ra khi kiểm tra phiếu đổi trả!", false);
                }
            }
        }.execute();
    }
}