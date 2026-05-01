package controller;

import entity.*;
//import service.DonViTinhService;
//import service.HoaDonService;
import gui.*;
import service.DonViTinhService;
import service.HoaDonService;
import service.PhieuDoiTraService;
//import service.ThuocService;
//import gui.TimKiemHD_GUI;
import service.ThuocService;
import utils.ToolCtrl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LapPhieuDoiTraController {
    public LapPhieuDoiTra_GUI gui;
    public HoaDonService hdService = new HoaDonService();
    public PhieuDoiTraService pdtService = new PhieuDoiTraService();
    public ToolCtrl tool = new ToolCtrl();
    public DonViTinhService dvtService = new DonViTinhService();
    public ThuocService thuocService = new ThuocService();

    public LapPhieuDoiTra_GUI getGui() {
        return gui;
    }

    public String maHD;
    public double tongTienHoan = 0;

    public LapPhieuDoiTraController(LapPhieuDoiTra_GUI gui) {
        this.gui = gui;
        suKien();
    }

    // ========== GẮN SỰ KIỆN ==========
    public void suKien() {
        gui.getBtnThem().addActionListener(e -> themThuocVaoPhieu());
        gui.getBtnXoa().addActionListener(e -> xuLyXoaDong());
        gui.getBtnLamMoi().addActionListener(e -> lamMoi());
        gui.getBtnTaoPhieuDT().addActionListener(e -> taoPhieuDoiTra());
        gui.getBtnQuayLai().addActionListener(e -> quayLai());

        gui.getTblHDThuoc().getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = gui.getTblHDThuoc().getSelectedRow();
                if (selectedRow != -1) {
                    String tenThuoc = gui.getTblHDThuoc().getValueAt(selectedRow, 1).toString();
                    gui.getTxtTenThuoc().setText(tenThuoc);
                }
            }
        });
    }

    // ========== GÁN MÃ HÓA ĐƠN VÀ TẢI DỮ LIỆU ==========
    public void setMaHD(String maHD) {
        this.maHD = maHD;
        gui.getLblMaHD().setText(maHD);
        taiDuLieuHoaDon(maHD);
        tinhTongTienHoan();
    }

    // ========== LẤY DỮ LIỆU TỪ HOÁ ĐƠN ==========
    public void taiDuLieuHoaDon(String maHD) {
        new Thread(() -> {
            HoaDon hd = hdService.timHoaDonTheoMa(maHD); // Gọi Server
            String tenKH = (hd != null && hd.getKhachHang() != null) ? hd.getKhachHang().getTenKH() : "Khách lẻ";

            SwingUtilities.invokeLater(() -> {
                gui.getLblKhachHang().setText(tenKH);
                capNhatBangThuocDaMua(maHD);
            });
        }).start();
    }

    // ========== ĐƯA NHỮNG THUỐC ĐÃ MUA LÊN BẢNG ==========
    public void capNhatBangThuocDaMua(String maHD) {
        new Thread(() -> {
            List<Object[]> chiTietList = hdService.layChiTietHoaDon(maHD);
            List<Object[]> tableData = new ArrayList<>();

            for (Object[] ct : chiTietList) {
                String noiSanXuat = thuocService.timTenQGTheoMaThuoc(ct[1].toString());
                tableData.add(new Object[] {
                        ct[1], // maThuoc
                        ct[2], // tenThuoc
                        noiSanXuat,
                        ct[3], // soLuong
                        ct[5], // donVi
                        tool.dinhDangVND(ct[6] instanceof Number ? ((Number) ct[6]).doubleValue() : 0),
                        tool.dinhDangVND(ct[7] instanceof Number ? ((Number) ct[7]).doubleValue() : 0)
                });
            }

            SwingUtilities.invokeLater(() -> {
                DefaultTableModel model = (DefaultTableModel) gui.getTblHDThuoc().getModel();
                model.setRowCount(0);
                for (Object[] row : tableData) {
                    model.addRow(row);
                }
                gui.getTblHDThuoc().getColumnModel().getColumn(0).setMinWidth(0);
                gui.getTblHDThuoc().getColumnModel().getColumn(0).setMaxWidth(0);
            });
        }).start();
    }

    // ========== THÊM THUỐC VÀO PHIẾU ==========
    public void themThuocVaoPhieu() {
        String tenThuoc = gui.getTxtTenThuoc().getText().trim();
        String soLuongStr = gui.getTxtSoLuong().getText().trim();
        String mucHoanStr = (String) gui.getCmbMucHoan().getSelectedItem();
        String ghiChu = gui.getTxaGhiChu().getText().trim();

        if (tenThuoc.isEmpty() || soLuongStr.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng nhập tên thuốc và số lượng!", false);
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(soLuongStr);
            if (soLuong <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            tool.hienThiThongBao("Lỗi", "Số lượng phải là số nguyên dương!", false);
            return;
        }

        JTable tblHD = gui.getTblHDThuoc();
        int row = -1;
        for (int i = 0; i < tblHD.getRowCount(); i++) {
            if (tblHD.getValueAt(i, 1).toString().equalsIgnoreCase(tenThuoc)) {
                row = i;
                break;
            }
        }

        if (row == -1) {
            tool.hienThiThongBao("Lỗi", "Thuốc không tồn tại trong hóa đơn!", false);
            return;
        }

        int slMua = Integer.parseInt(tblHD.getValueAt(row, 3).toString().replaceAll("[^0-9]", ""));
        String maThuoc = tblHD.getValueAt(row, 0).toString();
        String tenDVT = tblHD.getValueAt(row, 4).toString();
        String thanhTienStr = tblHD.getValueAt(row, 6).toString();
        String tenThuocTrongBang = tblHD.getValueAt(row, 2).toString();

        new Thread(() -> {
            String maDVT = dvtService.timMaDVTTheoTen(tenDVT);
            int daDoiTra = pdtService.tongSoLuongDaDoiTra(maHD, maThuoc, maDVT);
            int soLuongConLai = slMua - daDoiTra;

            if (soLuong > soLuongConLai) {
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Số lượng đổi trả vượt quá số lượng còn lại!\n"
                        + "Đã mua: " + slMua + " | Đã đổi trả: " + daDoiTra + " | Còn lại: " + soLuongConLai, false));
                return;
            }

            double thanhTien = tool.chuyenTienSangSo(thanhTienStr);
            double donGia = thanhTien / slMua;
            double tyLeHoan = Double.parseDouble(mucHoanStr.replace("%", "")) / 100.0;
            double tienHoan = (donGia * soLuong) * tyLeHoan;

            SwingUtilities.invokeLater(() -> {
                DefaultTableModel modelDT = (DefaultTableModel) gui.getTblPhieuDTThuoc().getModel();
                modelDT.addRow(new Object[] {
                        tenThuoc, tenThuocTrongBang, soLuong, tenDVT,
                        mucHoanStr, mucHoanStr, tool.dinhDangVND(tienHoan), ghiChu.isEmpty() ? "Không" : ghiChu, "Xóa"
                });
                tinhTongTienHoan();
                lamMoiInput();
            });
        }).start();
    }

    // ========== XOÁ 1 DÒNG TRONG TABLE PHIẾU ĐỔI TRẢ ==========
    public void xuLyXoaDong() {
        JTable table = gui.getTblPhieuDTThuoc();
        int row = table.getSelectedRow();
        if (row == -1) {
            tool.hienThiThongBao("Thông báo", "Vui lòng chọn dòng cần xóa!", false);
            return;
        }

        boolean confirm = tool.hienThiXacNhan("Xác nhận", "Bạn có chắc chắn muốn xoá dòng này?", null);
        if (confirm) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.removeRow(row);
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }
            tinhTongTienHoan();
        }
    }

    // ========== TÍNH TỔNG TIỀN HOÀN ==========
    public void tinhTongTienHoan() {
        JTable tblDT = gui.getTblPhieuDTThuoc();
        DefaultTableModel model = (DefaultTableModel) tblDT.getModel();
        double tong = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object tienHoanObj = model.getValueAt(i, 6); // cột [6] = tiền hoàn
            if (tienHoanObj != null) {
                try {
                    double tienHoan = tool.chuyenTienSangSo(tienHoanObj.toString());
                    tong += tienHoan;
                } catch (Exception e) {
                    System.err.println("Lỗi khi cộng tiền hoàn dòng " + i + ": " + e.getMessage());
                }
            }
        }
        tongTienHoan = tong;
        gui.getLblTongTienHoan().setText(tool.dinhDangVND(tongTienHoan));
    }

    public void lamMoiInput() {
        gui.getTxtTenThuoc().setText("");
        gui.getTxtSoLuong().setText("");
        gui.getCmbMucHoan().setSelectedIndex(0);
        gui.getTxaGhiChu().setText("");
    }

    public void lamMoi() {
        lamMoiInput();
        DefaultTableModel modelDT = (DefaultTableModel) gui.getTblPhieuDTThuoc().getModel();
        modelDT.setRowCount(0);
        tongTienHoan = 0;
        tinhTongTienHoan();
    }

    // ========== TẠO PHIẾU ĐỔI TRẢ ==========
    public void taoPhieuDoiTra() {
        if (gui.getTblPhieuDTThuoc().getRowCount() == 0) {
            tool.hienThiThongBao("Lỗi", "Chưa có thuốc nào để đổi trả!", false);
            return;
        }

        String lyDo = gui.getTxaLyDo().getText().trim();
        if (lyDo.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng nhập lý do đổi trả!", false);
            return;
        }

        NhanVien nvDangNhap = null;
        if (gui.getTrangChuQL() != null) nvDangNhap = gui.getTrangChuQL().layNhanVien();
        else if (gui.getTrangChuNV() != null) nvDangNhap = gui.getTrangChuNV().layNhanVien();

        if (nvDangNhap == null || nvDangNhap.getMaNV() == null) {
            tool.hienThiThongBao("Lỗi", "Không xác định được nhân viên lập phiếu!", false);
            return;
        }

        //Trích xuất dữ liệu 2 bảng trên UI trước khi ném vào Thread
        final NhanVien finalNvDangNhap = nvDangNhap;
        List<Object[]> listRowDT = new ArrayList<>();
        for (int i = 0; i < gui.getTblPhieuDTThuoc().getRowCount(); i++) {
            listRowDT.add(new Object[]{
                    gui.getTblPhieuDTThuoc().getValueAt(i, 0), // Tên Thuốc ĐT
                    gui.getTblPhieuDTThuoc().getValueAt(i, 3), // Tên DVT
                    gui.getTblPhieuDTThuoc().getValueAt(i, 2), // Số Lượng
                    gui.getTblPhieuDTThuoc().getValueAt(i, 7), // Ghi Chú
                    gui.getTblPhieuDTThuoc().getValueAt(i, 6), // Tiền Hoàn
                    gui.getTblPhieuDTThuoc().getValueAt(i, 5)  // Mức Hoàn
            });
        }

        List<Object[]> listRowHD = new ArrayList<>();
        for (int j = 0; j < gui.getTblHDThuoc().getRowCount(); j++) {
            listRowHD.add(new Object[]{
                    gui.getTblHDThuoc().getValueAt(j, 1), // Tên Thuốc HD
                    gui.getTblHDThuoc().getValueAt(j, 0)  // Mã Thuốc HD
            });
        }

        // Mở Thread xử lý DB, cập nhật kho
        new Thread(() -> {
            HoaDon hd = hdService.timHoaDonTheoMa(maHD);
            if (hd == null) {
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không tìm thấy hóa đơn!", false));
                return;
            }

            PhieuDoiTra pdt = new PhieuDoiTra();
            pdt.setMaPhieuDT(tool.taoKhoaChinh("PDT"));
            pdt.setHoaDon(hd);
            pdt.setNhanVien(finalNvDangNhap);
            pdt.setNgayDoiTra(LocalDate.now());
            pdt.setLyDo(lyDo);

            List<CTPhieuDoiTra> listChiTiet = new ArrayList<>();

            for (Object[] rowDT : listRowDT) {
                String tenThuocDT = rowDT[0].toString();
                String maThuoc = null;

                for (Object[] rowHD : listRowHD) {
                    if (rowHD[0].toString().equalsIgnoreCase(tenThuocDT)) {
                        maThuoc = rowHD[1].toString();
                        break;
                    }
                }
                if (maThuoc == null) continue;

                String tenDVT = rowDT[1].toString();
                String maDVT = dvtService.timMaDVTTheoTen(tenDVT);

                CTPhieuDoiTra chiTiet = new CTPhieuDoiTra();
                chiTiet.setPhieuDoiTra(pdt);
                chiTiet.setThuoc(thuocService.timThuocTheoMa(maThuoc));

                DonViTinh donViTinh = new DonViTinh();
                donViTinh.setMaDVT(maDVT);
                chiTiet.setDonViTinh(donViTinh);

                chiTiet.setSoLuong(Integer.parseInt(rowDT[2].toString()));
                chiTiet.setGhiChu(rowDT[3].toString());
                chiTiet.setTienHoan(tool.chuyenTienSangSo((String) rowDT[4]));
                chiTiet.setMucHoan(Double.parseDouble(rowDT[5].toString().replace("%", "")) / 100.0);

                listChiTiet.add(chiTiet);
            }

            pdt.setDanhSachChiTiet(listChiTiet);

            // Lưu Phiếu Đổi Trả vào DB
            if (!pdtService.themPDT(pdt)) {
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Tạo phiếu đổi trả thất bại. Vui lòng kiểm tra lại cấu hình DB!", false));
                return;
            }

            // Cập nhật tồn kho thuốc
            for (Object[] rowDT : listRowDT) {
                String tenThuocDT = rowDT[0].toString();
                String maThuoc = null;
                for (Object[] rowHD : listRowHD) {
                    if (rowHD[0].toString().equalsIgnoreCase(tenThuocDT)) {
                        maThuoc = rowHD[1].toString();
                        break;
                    }
                }
                if (maThuoc != null) {
                    String maDVT = dvtService.timMaDVTTheoTen(rowDT[1].toString());
                    int soLuongTra = Integer.parseInt(rowDT[2].toString());
                    thuocService.capNhatSoLuongTon(maThuoc, maDVT, soLuongTra, true);
                }
            }

            PhieuDoiTra pdtHoanChinh = pdtService.timPhieuDoiTraTheoMa(pdt.getMaPhieuDT());

            // Trả về luồng UI để thông báo và chuyển giao diện
            SwingUtilities.invokeLater(() -> {
                tool.hienThiThongBao("Thành công", "Tạo phiếu đổi trả thành công!", true);

                ChiTietPhieuDoiTra_GUI chiTietPanel;
                if (gui.getTrangChuQL() != null) {
                    chiTietPanel = new ChiTietPhieuDoiTra_GUI(gui.getTrangChuQL());
                    gui.getTrangChuQL().setUpNoiDung(chiTietPanel);
                } else if (gui.getTrangChuNV() != null) {
                    chiTietPanel = new ChiTietPhieuDoiTra_GUI(gui.getTrangChuNV());
                    gui.getTrangChuNV().setUpNoiDung(chiTietPanel);
                } else {
                    return;
                }
                chiTietPanel.getCtrl().hienThiThongTinPhieuDT(pdtHoanChinh);
            });
        }).start();
    }

    // ========== QUAY LẠI ==========
    public void quayLai() {
        // Việc khởi tạo TimKiemHD_GUI có thể kích hoạt Controller gọi Data nên cần bọc Thread
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                if (gui.getTrangChuQL() != null) {
                    tool.doiPanel(gui, new TimKiemHD_GUI(gui.getTrangChuQL()));
                } else if (gui.getTrangChuNV() != null) {
                    tool.doiPanel(gui, new TimKiemHD_GUI(gui.getTrangChuNV()));
                }
            });
        }).start();
    }
}