package controller;

import service.*;
import entity.*;
import gui.ChiTietHoaDon_GUI;
import gui.LapHoaDon_GUI;
import gui.TrangChuNV_GUI;
import gui.TrangChuQL_GUI;
import utils.ToolCtrl;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LapHoaDonController {
    public LapHoaDon_GUI gui;
    public ToolCtrl tool = new ToolCtrl();
    public TrangChuQL_GUI trangChuQL;
    public TrangChuNV_GUI trangChuNV;

    public ThuocService thuocService = new ThuocService();
    public KhachHangService khService = new KhachHangService();
    public NhanVienService nvService = new NhanVienService();
    public DonViTinhService dvtService = new DonViTinhService();
    public KhuyenMaiService kmService = new KhuyenMaiService();
    public HoaDonService hdService = new HoaDonService();
    public PhieuDatHangService pdhService = new PhieuDatHangService();

    public List<KhachHang> dsKhachHang;
    public List<Thuoc> dsThuoc;
    public DefaultTableModel tableModel;
    public boolean dangSetTenKH = false;
    public boolean dangSetSdtKH = false;
    public boolean lapTuPhieuDatHang = false;
    public boolean daLapHoaDon = false;

    public LapHoaDonController() {
        this(null);
    }

    public LapHoaDonController(LapHoaDon_GUI gui) {
        this.gui = gui;
        if (gui == null) return;
        this.trangChuQL = gui.getMainFrame();
        this.trangChuNV = gui.getMainFrameNV();
        this.tableModel = (DefaultTableModel) gui.getTblThuoc().getModel();
        loadData();
    }

    public void loadData() {
        taiDuLieu();
        suKien();
        goiYKhachHang();
        goiYThuoc();
        // Không gọi setComboxQuocGia() ở đây nữa vì dsThuoc chưa tải xong ngầm
    }

    public void suKien() {
        resetActionListener(gui.getBtnTaoHD());
        resetActionListener(gui.getBtnThem());
        resetActionListener(gui.getBtnXoa());
        resetActionListener(gui.getBtnLamMoi());

        gui.getBtnThem().addActionListener(e -> xuLyThemThuocVaoBang());
        gui.getBtnXoa().addActionListener(e -> xuLyXoaDong());
        gui.getBtnLamMoi().addActionListener(e -> lamMoi());
        gui.getBtnTaoHD().addActionListener(e -> xuLyXuatHoaDon());
        gui.getCmbHTThanhToan().addActionListener(e -> {
            String hinhThuc = gui.getCmbHTThanhToan().getSelectedItem().toString();

            if (hinhThuc.equalsIgnoreCase("Tiền mặt")) {
                gui.getTxtTienNhan().setEditable(true);
                gui.getTxtTienNhan().setText("");
                gui.getTxtTienNhan().requestFocus();
                tinhTienThua();
            } else {
                gui.getTxtTienNhan().setEditable(false);
                String tongTien = gui.getLblTongTien().getText().trim();
                gui.getTxtTienNhan().setText(tongTien);
            }
        });
        gui.getTxtTienNhan().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { tinhTienThua(); }
        });

        gui.getCmbSanPham().addActionListener(e -> setComboxQuocGia());
    }

    public void resetActionListener(AbstractButton btn) {
        for (ActionListener al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }
    }

    // ĐÃ BỌC SWINGWORKER (Do cần truy vấn QG và DVT theo thuốc)
    public void setComboxQuocGia() {
        Object selected = gui.getCmbSanPham().getSelectedItem();
        if (selected == null || selected.toString().isEmpty()) return;

        String tenThuoc = selected.toString();

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                List<QuocGia> listQG = thuocService.layListQuocGiaTheoThuoc(tenThuoc);
                String maThuoc = thuocService.layMaThuocTheoTen(tenThuoc);
                String donVi = thuocService.layTenDonViTinhTheoMaThuoc(maThuoc);
                return new Object[]{listQG, donVi};
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    List<QuocGia> listQG = (List<QuocGia>) result[0];
                    String donVi = (String) result[1];

                    gui.getCmbQuocGia().removeAllItems();
                    if (listQG != null) {
                        for (QuocGia qg : listQG) gui.getCmbQuocGia().addItem(qg.getTenQuocGia());
                    }

                    if (donVi == null) {
                        gui.cmbDonVi.setSelectedIndex(-1);
                    } else {
                        gui.cmbDonVi.setSelectedItem(donVi);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // ĐÃ BỌC SWINGWORKER
    public void taiDuLieu() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                List<KhachHang> khs = khService.layListKhachHang();
                List<Thuoc> ts = thuocService.layListThuoc(true);
                List<DonViTinh> dvts = dvtService.layListDVT();
                return new Object[]{khs, ts, dvts};
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    dsKhachHang = result[0] != null ? new ArrayList<>((List<KhachHang>) result[0]) : new ArrayList<>();
                    dsThuoc = result[1] != null ? new ArrayList<>((List<Thuoc>) result[1]) : new ArrayList<>();
                    List<DonViTinh> dsDVT = result[2] != null ? new ArrayList<>((List<DonViTinh>) result[2]) : new ArrayList<>();

                    gui.getCmbSanPham().removeAllItems();
                    gui.getCmbSanPham().addItem("");
                    for (Thuoc t : dsThuoc) gui.getCmbSanPham().addItem(t.getTenThuoc());

                    gui.getCmbDonVi().removeAllItems();
                    for (DonViTinh dvt : dsDVT) gui.getCmbDonVi().addItem(dvt.getTenDVT());
                    if (!dsDVT.isEmpty()) gui.getCmbDonVi().setSelectedIndex(0);

                    gui.getCmbHTThanhToan().setSelectedItem("Tiền mặt");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    public void goiYKhachHang() {
        gui.getTxtTenKH().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (dangSetTenKH || gui.getTxtTenKH().getText().trim().isEmpty() || dsKhachHang == null) return;
                String input = gui.getTxtTenKH().getText().trim().toLowerCase();
                List<KhachHang> ketQua = dsKhachHang.stream().filter(kh -> kh.getTenKH().toLowerCase().contains(input)).limit(5).toList();
                if (!ketQua.isEmpty()) hienThiListKhachHang(gui.getTxtTenKH(), ketQua, true);
            }
        });

        gui.getTxtSdt().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (dangSetSdtKH || gui.getTxtSdt().getText().trim().isEmpty() || dsKhachHang == null) return;
                String input = gui.getTxtSdt().getText().trim();
                List<KhachHang> ketQua = dsKhachHang.stream().filter(kh -> tool.chuyenSoDienThoai(kh.getSdt()).contains(input)).limit(5).toList();
                if (!ketQua.isEmpty()) hienThiListKhachHang(gui.getTxtSdt(), ketQua, false);
            }
        });
    }

    public void hienThiListKhachHang(JTextField tf, List<KhachHang> list, boolean isTen) {
        JPopupMenu pop = new JPopupMenu();
        for (KhachHang kh : list) {
            String text = isTen ? kh.getTenKH() + " - " + tool.chuyenSoDienThoai(kh.getSdt())
                    : tool.chuyenSoDienThoai(kh.getSdt()) + " - " + kh.getTenKH();
            JMenuItem item = new JMenuItem(text);
            item.addActionListener(e -> {
                if (isTen) {
                    dangSetSdtKH = true;
                    gui.getTxtTenKH().setText(kh.getTenKH());
                    gui.getTxtSdt().setText(tool.chuyenSoDienThoai(kh.getSdt()));
                    gui.getTxtTuoi().setText(String.valueOf(kh.getTuoi()));
                    gui.getTxtTuoi().setEditable(false);
                    dangSetSdtKH = false;
                } else {
                    dangSetTenKH = true;
                    gui.getTxtSdt().setText(tool.chuyenSoDienThoai(kh.getSdt()));
                    gui.getTxtTenKH().setText(kh.getTenKH());
                    gui.getTxtTuoi().setText(String.valueOf(kh.getTuoi()));
                    gui.getTxtTuoi().setEditable(false);
                    dangSetTenKH = false;
                }
                pop.setVisible(false);
            });
            pop.add(item);
        }
        SwingUtilities.invokeLater(() -> pop.show(tf, 0, tf.getHeight()));
    }

    // ĐÃ BỌC THREAD (Tách lấy UI -> Gọi DB ngầm -> Cập nhật UI)
    public void xuLyThemThuocVaoBang() {
        Object itemThuoc = gui.getCmbSanPham().getEditor().getItem();
        if (itemThuoc == null || itemThuoc.toString().trim().isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn thuốc!", false);
            return;
        }
        String tenThuoc = itemThuoc.toString().trim();
        String tenQG = (String) gui.getCmbQuocGia().getSelectedItem();
        if (tenQG == null || tenQG.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn quốc gia!", false);
            return;
        }
        int slNhapTemp;
        try {
            slNhapTemp = Integer.parseInt(gui.getTxtSoLuong().getText().trim());
            if (slNhapTemp <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Lỗi", "Số lượng phải lớn hơn 0!", false);
            return;
        }
        final int slNhap = slNhapTemp;
        String tenDVT = (String) gui.getCmbDonVi().getSelectedItem();

        // 1. Tìm xem thuốc đã có trên bảng chưa (Làm trên EDT)
        DefaultTableModel model = (DefaultTableModel) gui.getTblThuoc().getModel();
        int slDangCoTrenBang = 0;
        int rowIndex = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1).toString().equalsIgnoreCase(tenThuoc)) {
                slDangCoTrenBang = Integer.parseInt(model.getValueAt(i, 3).toString());
                rowIndex = i;
                break;
            }
        }

        final int finalSlDangCo = slDangCoTrenBang;
        final int finalRowIndex = rowIndex;

        // 2. Chạy ngầm gọi CSDL
        new Thread(() -> {
            try {
                String maThuoc = thuocService.layMaThuocTheoTenVaQG(tenThuoc, tenQG);
                Thuoc thuoc = thuocService.timThuocTheoMa(maThuoc);
                if (thuoc == null) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không tìm thấy thuốc!", false));
                    return;
                }

                int tonKho = thuocService.laySoLuongTon(thuoc.getMaThuoc());
                if (slNhap + finalSlDangCo > tonKho) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Tồn kho không đủ! (Tồn: " + tonKho + ", Đã nhập: " + finalSlDangCo + ")", false));
                    return;
                }

                DonViTinh dvt = dvtService.timTheoTen(tenDVT);
                double donGiaGoc = thuoc.getGiaBan();
                double donGiaSauKMTemp = donGiaGoc;
                double thanhTienDotNayTemp = 0;
                int slThucTeVaoBangTemp = slNhap;
                String moTaKMTemp = "Không có KM";

                String maKM = thuocService.layMaKMTheoMaThuoc(thuoc.getMaThuoc());
                LocalDate homNay = LocalDate.now();
                boolean coKM = false;

                if (maKM != null && !maKM.isEmpty()) {
                    KhuyenMai km = kmService.layKhuyenMaiTheoMa(maKM);
                    if (km != null && !homNay.isBefore(km.getNgayBD()) && !homNay.isAfter(km.getNgayKT())) {
                        coKM = true;
                        if (km.getLoaiKM().equalsIgnoreCase("giảm giá")) {
                            double mucGiam = km.getMucKM();
                            donGiaSauKMTemp = donGiaGoc * (1 - mucGiam / 100.0);
                            thanhTienDotNayTemp = donGiaSauKMTemp * slNhap;
                            moTaKMTemp = "Giảm " + mucGiam + "%";
                        } else if (km.getLoaiKM().equalsIgnoreCase("mua tặng")) {
                            int soLuongTang = (slNhap / km.getSoLuongMua()) * km.getSoLuongTang();
                            if (soLuongTang > 0) {
                                slThucTeVaoBangTemp = slNhap + soLuongTang;
                                moTaKMTemp = String.format("Mua %d tặng %d (Tặng thêm: %d)", km.getSoLuongMua(), km.getSoLuongTang(), soLuongTang);
                            }
                            thanhTienDotNayTemp = donGiaGoc * slNhap;
                        }
                    }
                }
                if (!coKM) thanhTienDotNayTemp = donGiaGoc * slNhap;

                final double donGiaSauKM = donGiaSauKMTemp;
                final double thanhTienDotNay = thanhTienDotNayTemp;
                final int slThucTeVaoBang = slThucTeVaoBangTemp;
                final String moTaKM = moTaKMTemp;

                // 3. Đổ dữ liệu lên bảng (EDT)
                SwingUtilities.invokeLater(() -> {
                    if (finalRowIndex != -1) {
                        int tongSoLuongMoi = finalSlDangCo + slThucTeVaoBang;
                        double tienCuTrongBang = tool.chuyenTienSangSo(model.getValueAt(finalRowIndex, 7).toString());
                        double tongTienMoi = tienCuTrongBang + thanhTienDotNay;

                        model.setValueAt(tongSoLuongMoi, finalRowIndex, 3);
                        model.setValueAt(tool.dinhDangVND(tongTienMoi), finalRowIndex, 7);
                        if (moTaKM.contains("Tặng thêm")) model.setValueAt(moTaKM, finalRowIndex, 8);
                    } else {
                        model.addRow(new Object[] { model.getRowCount() + 1, thuoc.getTenThuoc(), tenQG, slThucTeVaoBang,
                                dvt != null ? dvt.getTenDVT() : "", tool.dinhDangVND(donGiaGoc), tool.dinhDangVND(donGiaSauKM),
                                tool.dinhDangVND(thanhTienDotNay), moTaKM, "Xóa" });
                    }
                    tinhTongTien();
                    resetFormNhap();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Có lỗi xảy ra khi truy vấn dữ liệu!", false));
            }
        }).start();
    }

    public void resetFormNhap() {
        gui.getTxtSoLuong().setText("");
        gui.getCmbSanPham().setSelectedIndex(0);
        if(gui.getCmbDonVi().getItemCount() > 0) gui.getCmbDonVi().setSelectedIndex(0);
    }

    public void xuLyXoaDong() {
        JTable table = gui.getTblThuoc();
        int row = table.getSelectedRow();
        if (row == -1) {
            tool.hienThiThongBao("Thông báo", "Vui lòng chọn dòng cần xóa!", false);
            return;
        }

        boolean confirm = tool.hienThiXacNhan("Xác nhận", "Bạn có chắc chắn muốn xoá dòng này?", null);
        if (confirm) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.removeRow(row);
            for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(i + 1, i, 0);
            tinhTongTien();
        }
    }

    public void capNhatSTT() {
        for (int i = 0; i < tableModel.getRowCount(); i++) tableModel.setValueAt(i + 1, i, 0);
    }

    public void tinhTongTien() {
        double tong = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object giaTri = tableModel.getValueAt(i, 7);
            if (giaTri != null) {
                String text = giaTri.toString().trim();
                tong += tool.chuyenTienSangSo(text);
            }
        }
        gui.getLblTongTien().setText(tool.dinhDangVND(tong));
        tinhTienThua();
    }

    public void tinhTienThua() {
        if (!"Tiền mặt".equals(gui.getCmbHTThanhToan().getSelectedItem())) {
            gui.getLblTienThua().setText("0 VNĐ");
            return;
        }
        try {
            double tong = tool.chuyenTienSangSo(gui.getLblTongTien().getText());
            double nhan = tool.chuyenTienSangSo(gui.getTxtTienNhan().getText());
            double tienThua = nhan - tong;
            if (tienThua < 0) tienThua = 0;
            gui.getLblTienThua().setText(tool.dinhDangVND(tienThua));
        } catch (Exception e) {
            gui.getLblTienThua().setText("0 VNĐ");
        }
    }

    public void lamMoi() {
        lapTuPhieuDatHang = false;
        gui.getTxtSdt().setText("");
        gui.getTxtTenKH().setText("");
        gui.getTxtTuoi().setText("");
        gui.getTxtTuoi().setEditable(true);
        gui.getTxtSoLuong().setText("");
        gui.getTxtTienNhan().setText("");
        gui.getCmbSanPham().setSelectedItem("");
        tableModel.setRowCount(0);
        tinhTongTien();
    }

    public boolean ktTenKhachHangHopLe() {
        String ten = gui.txtTenKH.getText().trim();
        String regex = "^[\\p{L}\\s]+$";
        if (ten.isEmpty()) {
            tool.hienThiThongBao("Tên khách hàng không hợp lệ!", "Tên không được để trống", false);
            gui.txtTenKH.requestFocus();
            return false;
        } else if (!ten.matches(regex)) {
            tool.hienThiThongBao("Tên khách hàng không hợp lệ!", "Tên không được chứa số hoặc ký tự đặc biệt", false);
            gui.txtTenKH.requestFocus();
            gui.txtTenKH.selectAll();
            return false;
        }
        gui.txtSdt.requestFocus();
        return true;
    }

    public boolean ktSoDienThoaiHopLe() {
        String sdt = gui.txtSdt.getText().trim();
        String regex = "^0\\d{9}$";
        if (sdt.isEmpty()) {
            tool.hienThiThongBao("Số điện thoại không hợp lệ!", "Không được để trống", false);
            gui.txtSdt.requestFocus();
            return false;
        } else if (!sdt.matches(regex)) {
            tool.hienThiThongBao("Số điện thoại không hợp lệ!", "Phải gồm 10 chữ số và bắt đầu bằng 0", false);
            gui.txtSdt.requestFocus();
            gui.txtSdt.selectAll();
            return false;
        }
        gui.txtTuoi.requestFocus();
        return true;
    }

    public boolean ktTuoiHopLe() {
        String tuoiStr = gui.txtTuoi.getText().trim();
        try {
            int tuoi = Integer.parseInt(tuoiStr);
            if (tuoi < 0) {
                tool.hienThiThongBao("Tuổi không hợp lệ!", "Tuổi không được là số âm.", false);
                gui.txtTuoi.requestFocus();
                gui.txtTuoi.selectAll();
                return false;
            }
        } catch (NumberFormatException e) {
            tool.hienThiThongBao("Tuổi không hợp lệ!", "Tuổi phải là số nguyên.", false);
            gui.txtTuoi.requestFocus();
            gui.txtTuoi.selectAll();
            return false;
        }
        return true;
    }

    // ĐÃ BỌC THREAD (Transaction khổng lồ cần chạy ngầm)
    public void xuLyXuatHoaDon() {
        if (daLapHoaDon) return;

        if (tableModel.getRowCount() == 0) {
            tool.hienThiThongBao("Thông báo", "Chưa có thuốc trong hóa đơn!", false);
            return;
        }

        if (!ktSoDienThoaiHopLe() || !ktTenKhachHangHopLe() || !ktTuoiHopLe()) return;

        // 1. Lấy toàn bộ dữ liệu UI
        String tenKH = gui.getTxtTenKH().getText().trim();
        String sdtChuan = tool.chuyenSoDienThoai(gui.getTxtSdt().getText().trim());
        String tuoiText = gui.getTxtTuoi().getText().trim();
        String hinhThucTT = (String) gui.getCmbHTThanhToan().getSelectedItem();
        double tienCanTra = tool.chuyenTienSangSo(gui.getLblTongTien().getText());
        double tienNhanTemp = 0;

        if (hinhThucTT.equalsIgnoreCase("Tiền mặt")) {
            String tienNhanStr = gui.getTxtTienNhan().getText().trim().replace(",", "");
            if (tienNhanStr.isEmpty()) {
                tool.hienThiThongBao("Lỗi", "Tiền nhận không được để trống", false);
                return;
            }
            try {
                tienNhanTemp = Double.parseDouble(tienNhanStr);
                if (tienNhanTemp < tienCanTra) {
                    tool.hienThiThongBao("Lỗi", "Tiền nhận không đủ để thanh toán!", false);
                    return;
                }
            } catch (NumberFormatException e) {
                tool.hienThiThongBao("Lỗi!", "Số tiền nhận không hợp lệ!", false);
                return;
            }
        } else {
            tienNhanTemp = tienCanTra;
        }

        final double tienNhan = tienNhanTemp;
        List<Object[]> tableData = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableData.add(new Object[]{
                    tableModel.getValueAt(i, 1), // Tên thuốc
                    tableModel.getValueAt(i, 3), // Số lượng
                    tableModel.getValueAt(i, 5), // Đơn giá
                    tableModel.getValueAt(i, 4), // Tên DVT
                    tableModel.getValueAt(i, 8)  // Ghi chú KM
            });
        }

        // 2. Gọi mạng lưu Database
        new Thread(() -> {
            try {
                KhachHang kh = khService.timKhachHangTheoSDT(sdtChuan);
                String maKH;
                if (kh == null) {
                    maKH = tool.taoKhoaChinh("KH");
                    boolean themKH = khService.themKhachHang(kh);
                    if (!themKH) {
                        SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không thể thêm khách hàng mới!", false));
                        return;
                    }
                } else {
                    maKH = kh.getMaKH();
                }

                String maHD = tool.taoKhoaChinh("HD");
                String maNV = (trangChuNV != null) ? trangChuNV.layNhanVien().getMaNV() : trangChuQL.layNhanVien().getMaNV();
                String diaChiHT = "456 Nguyễn Huệ, TP.HCM";
                String tenHT = "Hiệu Thuốc Tâm Thanh";
                String hotline = "+84-912345689";

                KhachHang khHD = khService.timKhachHangTheoMa(maKH);
                NhanVien nv = nvService.timNhanVienTheoMa(maNV);
                StringBuilder ghiChu = new StringBuilder();

                for (Object[] row : tableData) {
                    Object tenThuoc = row[0];
                    Object ghiChuKM = row[4];
                    if (ghiChuKM != null && !ghiChuKM.toString().isBlank()) {
                        ghiChu.append(tenThuoc).append(" : ").append(ghiChuKM).append("; ");
                    }
                }

                HoaDon hd = new HoaDon(maHD, khHD, nv, hinhThucTT, LocalDate.now(), diaChiHT, tenHT, ghiChu.toString(), hotline, tienNhan, true);

                if (!hdService.themHoaDon(hd)) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không thể tạo hóa đơn!", false));
                    return;
                }

                for (Object[] row : tableData) {
                    String tenThuoc = row[0].toString();
                    Thuoc t = null;
                    if(dsThuoc != null) {
                        t = dsThuoc.stream().filter(x -> x.getTenThuoc().equals(tenThuoc)).findFirst().orElse(null);
                    }
                    if (t == null) continue;

                    int soLuong = Integer.parseInt(row[1].toString());
                    double donGia = tool.chuyenTienSangSo(row[2].toString());
                    String tenDVT = row[3].toString();
                    String maDVT = dvtService.timMaDVTTheoTen(tenDVT);

                    CTHoaDon ct = new CTHoaDon();
                    HoaDon hdProxy = new HoaDon(); hdProxy.setMaHD(maHD);
                    Thuoc thuocProxy = new Thuoc(); thuocProxy.setMaThuoc(t.getMaThuoc());
                    DonViTinh dvtProxy = new DonViTinh(); dvtProxy.setMaDVT(maDVT);

                    ct.setHoaDon(hdProxy);
                    ct.setThuoc(thuocProxy);
                    ct.setDonViTinh(dvtProxy);
                    ct.setSoLuong(soLuong);
                    ct.setDonGia(donGia);

                    hdService.themChiTietHoaDon(ct);
                    thuocService.giamSoLuongTon(t.getMaThuoc(), maDVT, soLuong);
                }

                HoaDon hoaDonDaLuu = hdService.timHoaDonTheoMa(maHD);

                // 3. Xong xuôi, cập nhật giao diện
                SwingUtilities.invokeLater(() -> {
                    tool.hienThiThongBao("Thành công", "Xuất hóa đơn thành công!", true);
                    daLapHoaDon = true;
                    lapTuPhieuDatHang = false;

                    ChiTietHoaDon_GUI chiTietPanel;
                    if (trangChuQL != null) {
                        chiTietPanel = new ChiTietHoaDon_GUI(trangChuQL);
                        trangChuQL.setUpNoiDung(chiTietPanel);
                    } else if (trangChuNV != null) {
                        chiTietPanel = new ChiTietHoaDon_GUI(trangChuNV);
                        trangChuNV.setUpNoiDung(chiTietPanel);
                    } else return;

                    chiTietPanel.getCtrl().hienThiThongTinHoaDon(hoaDonDaLuu);
                    lamMoi();
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Đã xảy ra lỗi hệ thống khi xuất hóa đơn!", false));
            }
        }).start();
    }

    // ĐÃ BỌC SWINGWORKER
    public void loadTuPhieuDatHang(String maPDH) {
        lapTuPhieuDatHang = true;

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                PhieuDatHang pdh = pdhService.timTheoMa(maPDH);
                if (pdh == null) return null;

                List<Object[]> chiTiet = new ArrayList<>(pdhService.layDanhSachThuocTheoPDH(maPDH));
                List<Object[]> enrichedChiTiet = new ArrayList<>();
                LocalDate homNay = LocalDate.now();

                // Tính toán Khuyến Mãi và lấy tên Quốc Gia ngầm (Tránh lag)
                for (Object[] ct : chiTiet) {
                    String maThuoc = ct[1].toString();
                    int sl = Integer.parseInt(ct[3].toString());
                    String tenQG = thuocService.timTenQGTheoMaThuoc(maThuoc);
                    String tenDVT = ct[5].toString();
                    double donGia = Double.parseDouble(ct[6].toString());
                    double thanhTien = Double.parseDouble(ct[7].toString());

                    double donGiaSauKM = donGia;
                    String moTaKM = "Không có KM";
                    String maKM = thuocService.layMaKMTheoMaThuoc(maThuoc);

                    if (maKM != null && !maKM.isEmpty()) {
                        KhuyenMai km = kmService.layKhuyenMaiTheoMa(maKM);
                        if (km != null && !homNay.isBefore(km.getNgayBD()) && !homNay.isAfter(km.getNgayKT())) {
                            if (km.getLoaiKM().equalsIgnoreCase("giảm giá")) {
                                donGiaSauKM = donGia * (1 - km.getMucKM() / 100.0);
                                moTaKM = "Giảm " + km.getMucKM() + "%";
                            } else if (km.getLoaiKM().equalsIgnoreCase("mua tặng")) {
                                int soLuongTang = (sl / km.getSoLuongMua()) * km.getSoLuongTang();
                                if (soLuongTang > 0) moTaKM = String.format("Mua %d tặng %d (Tặng: %d)", km.getSoLuongMua(), km.getSoLuongTang(), soLuongTang);
                            }
                        }
                    }
                    enrichedChiTiet.add(new Object[]{ct[2], tenQG, sl, tenDVT, donGia, donGiaSauKM, thanhTien, moTaKM});
                }
                return new Object[]{pdh, enrichedChiTiet};
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    if (result == null) {
                        tool.hienThiThongBao("Lỗi", "Không tìm thấy phiếu đặt hàng!", false);
                        return;
                    }

                    PhieuDatHang pdh = (PhieuDatHang) result[0];
                    List<Object[]> chiTiet = (List<Object[]>) result[1];

                    KhachHang kh = pdh.getKhachHang();
                    gui.getTxtTenKH().setText(kh.getTenKH());
                    gui.getTxtSdt().setText(tool.chuyenSoDienThoai(kh.getSdt()));
                    gui.getTxtTuoi().setText(String.valueOf(kh.getTuoi()));
                    gui.getTxtTuoi().setEditable(false);

                    tableModel.setRowCount(0);
                    for (Object[] ct : chiTiet) {
                        tableModel.addRow(new Object[] { tableModel.getRowCount() + 1, ct[0], ct[1], ct[2], ct[3],
                                tool.dinhDangVND((double)ct[4]), tool.dinhDangVND((double)ct[5]),
                                tool.dinhDangVND((double)ct[6]), ct[7], "Xóa" });
                    }
                    tinhTongTien();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    public void goiYThuoc() {
        JTextField txtTimThuoc = (JTextField) gui.getCmbSanPham().getEditor().getEditorComponent();

        txtTimThuoc.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_LEFT
                        || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    return;
                }

                String input = txtTimThuoc.getText().trim().toLowerCase();
                if (input.isEmpty() || dsThuoc == null) return;

                List<Thuoc> ketQua = dsThuoc.stream().filter(t -> t.getTenThuoc().toLowerCase().contains(input)).limit(10).toList();

                if (!ketQua.isEmpty()) hienThiListThuoc(txtTimThuoc, ketQua);
            }
        });
    }

    public void hienThiListThuoc(JTextField tf, List<Thuoc> list) {
        JPopupMenu pop = new JPopupMenu();

        for (Thuoc t : list) {
            JMenuItem item = new JMenuItem(t.getTenThuoc());
            item.addActionListener(e -> {
                tf.setText(t.getTenThuoc());
                gui.getCmbSanPham().setSelectedItem(t.getTenThuoc());
                setComboxQuocGia();
                pop.setVisible(false);
            });
            pop.add(item);
        }

        SwingUtilities.invokeLater(() -> {
            pop.show(tf, 0, tf.getHeight());
            tf.requestFocus();
        });
    }
}