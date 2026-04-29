package controller;

//import service.HoaDonService;
import service.PhieuDoiTraService;
//import service.ThuocService;
import entity.HoaDon;
import entity.PhieuDoiTra;
import gui.ChiTietPhieuDoiTra_GUI;
import gui.TimKiemPhieuDoiTra_GUI;
import gui.TrangChuNV_GUI;
import gui.TrangChuQL_GUI;
import utils.ToolCtrl;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuDoiTraController {
    public ChiTietPhieuDoiTra_GUI gui;

    public PhieuDoiTraService phieuDTService = new PhieuDoiTraService();
//    public HoaDonService hdService = new HoaDonService();
//    public ThuocService thuocService = new ThuocService();

    public ToolCtrl tool = new ToolCtrl();
    public TrangChuNV_GUI trangChuNV;
    public TrangChuQL_GUI trangChuQL;

    public ChiTietPhieuDoiTraController(ChiTietPhieuDoiTra_GUI gui) {
        this.gui = gui;
        suKien();
    }

    // ========== GẮN SỰ KIỆN ==========
    public void suKien() {
        gui.getBtnQuayLai().addActionListener(e -> quayLai());
        gui.getBtnInPhieu().addActionListener(e -> xuatPhieuDoiTraRaTXT());
    }

    // ========== HIỂN THỊ THÔNG TIN PHIẾU ĐỔI TRẢ ==========
    public void hienThiThongTinPhieuDT(PhieuDoiTra phieuDT) {
        if (phieuDT == null) return;

        // Trích xuất dữ liệu có sẵn ngay trên luồng UI
        String maPhieuDT = phieuDT.getMaPhieuDT();
        String maHD = phieuDT.getHoaDon().getMaHD();
        String tenKH = phieuDT.getHoaDon().getKhachHang().getTenKH();
        String tenNV = phieuDT.getNhanVien().getTenNV();
        String lyDo = phieuDT.getLyDo();
        LocalDate ngayDoiTraTemp = null;
        if (phieuDT.getNgayDoiTra() instanceof LocalDate) {
            ngayDoiTraTemp = (LocalDate) phieuDT.getNgayDoiTra();
        }
        final LocalDate ngayDoiTra = ngayDoiTraTemp;

        // Mở Thread để gọi mạng lấy các thông tin chưa có (HoaDon, Tính Tiền)
        new Thread(() -> {
//            HoaDon hd = hdService.timHoaDonTheoMa(maHD);
            double tongTienHoan = phieuDTService.tinhTongTienHoanTheoPhieuDT(maPhieuDT);

            // Trả kết quả về giao diện UI
            SwingUtilities.invokeLater(() -> {
//                String diaChi = (hd != null && hd.getDiaChiHT() != null) ? hd.getDiaChiHT() : "";
//                String hotline = (hd != null && hd.getHotline() != null) ? hd.getHotline() : "";

                gui.getLblMaPhieuDT().setText(maPhieuDT);
                gui.getLblMaHD().setText(maHD);
                gui.getLblKhachHang().setText(tenKH);
                gui.getLblNhanVien().setText(tenNV);
//                gui.getLblDiaChi().setText(diaChi);
//                gui.getLblHotline().setText(tool.chuyenSoDienThoai(hotline));
                gui.getLblLyDo().setText(lyDo);

                if (ngayDoiTra != null) {
                    gui.getLblNgayLap().setText(tool.dinhDangLocalDate(ngayDoiTra));
                } else {
                    gui.getLblNgayLap().setText("");
                }

                gui.getLblTongTienHoan().setText(tool.dinhDangVND(tongTienHoan));

                setDataChoTable(maPhieuDT);
            });
        }).start();
    }

    // ========== ĐƯA DỮ LIỆU VÀO BẢNG THUỐC ==========
    public void setDataChoTable(String maPhieuDT) {
        new Thread(() -> {
            // Lấy danh sách thuốc từ Server
            List<Object[]> listCT = phieuDTService.layDanhSachThuocTheoPhieuDT(maPhieuDT);
            List<Object[]> tableRows = new ArrayList<>();

            for (Object[] row : listCT) {
                double tienHoan = row[7] instanceof Number ? ((Number) row[7]).doubleValue() : 0;
//                String noiSanXuat = thuocService.timTenQGTheoMaThuoc(row[1].toString());

                tableRows.add(new Object[] {
                        row[2], // tên thuốc
//                        noiSanXuat,
                        row[3], // số lượng
                        row[5], // đơn vị
                        row[6], // mức hoàn
                        tool.dinhDangVND(tienHoan),
                        row[8]  // ghi chú
                });
            }

            // Đẩy tất cả dữ liệu lên bảng cùng 1 lúc trên luồng UI
            SwingUtilities.invokeLater(() -> {
                DefaultTableModel model = (DefaultTableModel) gui.getTblThuoc().getModel();
                model.setRowCount(0);
                for (Object[] r : tableRows) {
                    model.addRow(r);
                }
            });
        }).start();
    }

    // ========== IN PHIẾU ĐỔI TRẢ ==========
    public void xuatPhieuDoiTraRaTXT() {
        String maPhieuDT = gui.getLblMaPhieuDT().getText();
        if (maPhieuDT == null || maPhieuDT.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Không có phiếu đổi trả để xuất!", false);
            return;
        }

        // Form chọn File bắt buộc phải nằm trên luồng UI
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu phiếu đổi trả (.txt)");
        chooser.setSelectedFile(new File("PhieuDoiTra_" + maPhieuDT + ".txt"));

        if (chooser.showSaveDialog(gui) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();

        // 1. Trích xuất toàn bộ dữ liệu Text và Bảng vào biến tạm trên luồng UI
        String txtMaHD = gui.getLblMaHD().getText();
        String txtNgayLap = gui.getLblNgayLap().getText();
        String txtNhanVien = gui.getLblNhanVien().getText();
        String txtKhachHang = gui.getLblKhachHang().getText();
        String txtDiaChi = gui.getLblDiaChi().getText();
        String txtHotline = gui.getLblHotline().getText();
        String txtLyDo = gui.getLblLyDo().getText();
        String txtTongTien = gui.getLblTongTienHoan().getText();

        DefaultTableModel model = (DefaultTableModel) gui.getTblThuoc().getModel();
        List<Object[]> tableData = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            tableData.add(new Object[] {
                    model.getValueAt(i, 0), model.getValueAt(i, 1), model.getValueAt(i, 2),
                    model.getValueAt(i, 3), model.getValueAt(i, 4), model.getValueAt(i, 5), model.getValueAt(i, 6)
            });
        }

        // 2. Chạy Thread riêng để Ghi File (I/O) tránh giật lag lúc đang lưu
        new Thread(() -> {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write("=========================================\n");
                bw.write("        PHIẾU ĐỔI TRẢ - TAM THANH         \n");
                bw.write("=========================================\n\n");

                bw.write("Mã phiếu đổi trả : " + maPhieuDT + "\n");
                bw.write("Mã hóa đơn       : " + txtMaHD + "\n");
                bw.write("Ngày lập         : " + txtNgayLap + "\n");
                bw.write("Nhân viên        : " + txtNhanVien + "\n");
                bw.write("Khách hàng       : " + txtKhachHang + "\n");
                bw.write("Địa chỉ          : " + txtDiaChi + "\n");
                bw.write("Hotline          : " + txtHotline + "\n");
                bw.write("Lý do đổi trả    : " + txtLyDo + "\n\n");

                bw.write(String.format("%-25s %-15s %-8s %-10s %-10s %-15s %-20s%n",
                        "Tên thuốc", "Nơi SX", "SL", "Đơn vị", "Mức hoàn", "Tiền hoàn", "Ghi chú"));
                bw.write("----------------------------------------------------------------------------------------------\n");

                for (Object[] r : tableData) {
                    bw.write(String.format("%-25s %-15s %-8s %-10s %-10s %-15s %-20s%n",
                            r[0], r[1], r[2], r[3], r[4], r[5], r[6]));
                }

                bw.write("\n-----------------------------------------\n");
                bw.write("Tổng tiền hoàn : " + txtTongTien + "\n");
                bw.write("\n=========================================\n");
                bw.write("   CẢM ƠN QUÝ KHÁCH - HẸN GẶP LẠI!   \n");
                bw.write("=========================================\n");

                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Xuất phiếu đổi trả", "Xuất file TXT thành công!", true));

            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không thể ghi file TXT!", false));
            }
        }).start();
    }

    // ========== QUAY LẠI TRANG TÌM KIẾM PHIẾU ĐỔI TRẢ ==========
    public void quayLai() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (gui.getMainFrameQL() != null) {
                    tool.doiPanel(gui, new TimKiemPhieuDoiTra_GUI(gui.getMainFrameQL()));
                } else if (gui.getMainFrameNV() != null) {
                    tool.doiPanel(gui, new TimKiemPhieuDoiTra_GUI(gui.getMainFrameNV()));
                }
            });
        }).start();
    }
}