package controller;

import entity.NhanVien;
import entity.TaiKhoan;
import entity.Thue;
import gui.ThemNhanVien_GUI;
import service.NhanVienService;
//import service.ThueService;
import utils.ToolCtrl;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class ThemNhanVienController {
    public ThemNhanVien_GUI gui;
    public File fileAnhDaChon;
    public ToolCtrl tool = new ToolCtrl();

    // Khởi tạo Service
    private NhanVienService nvService = new NhanVienService();
//    private ThueService thueService = new ThueService();

    public ThemNhanVienController(ThemNhanVien_GUI gui) {
        this.gui = gui;
        suKien();
        setItemComboBox();
    }

    // ========== SỰ KIỆN ==========
    public void suKien() {
        gui.getBtnChonAnh().addActionListener(e -> chonAnh());
        gui.getBtnLamMoi().addActionListener(e -> lamMoi());
        gui.getBtnThem().addActionListener(e -> themNhanVien());
    }

    // ========== COMBOBOX ==========
    public void setItemComboBox() {
        gui.getCmbGioiTinh().setModel(new DefaultComboBoxModel<>(new String[] { "Nam", "Nữ" }));
        gui.getCmbChucVu().setModel(new DefaultComboBoxModel<>(new String[] { "Nhân viên bán hàng", "Nhân viên quản lý" }));

        gui.getCmbThue().setEnabled(false); // Khóa lại trong lúc đợi mạng

//        new SwingWorker<List<Thue>, Void>() {
//            @Override
//            protected List<Thue> doInBackground() {
//                return thueService.layListThue(); // Gọi Server
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    List<Thue> dsThue = get();
//                    gui.getCmbThue().removeAllItems();
//                    if (dsThue != null) {
//                        for (Thue t : dsThue) {
//                            gui.getCmbThue().addItem(t);
//                        }
//                    }
//                    gui.getCmbThue().setEnabled(true); // Mở khóa khi có data
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    gui.getCmbThue().setEnabled(true);
//                }
//            }
//        }.execute();
    }

    // ========== CHỌN ẢNH ==========
    public void chonAnh() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn ảnh nhân viên");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh", "png", "jpg", "jpeg"));

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileAnhDaChon = fc.getSelectedFile();
            java.awt.Image imgDaChon = new ImageIcon(fileAnhDaChon.getAbsolutePath()).getImage();
            java.awt.Image scaledImg = imgDaChon.getScaledInstance(180, 220, java.awt.Image.SCALE_SMOOTH);
            gui.getImgAnhNV().setIcon(new ImageIcon(scaledImg));
        }
    }

    // ========== LƯU ẢNH ==========
    public String luuAnh(String maNV) {
        try {
            if (fileAnhDaChon == null) return null;
            String projectPath = System.getProperty("user.dir");
            File destFolder = new File(projectPath + "/src/main/resources/picture/nhanVien");
            if (!destFolder.exists()) { destFolder.mkdirs(); }
            String extension = fileAnhDaChon.getName().substring(fileAnhDaChon.getName().lastIndexOf("."));
            String tenMoi = maNV + extension;
            File fileDich = new File(destFolder, tenMoi);
            Files.copy(fileAnhDaChon.toPath(), fileDich.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "/picture/nhanVien/" + tenMoi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ========== LÀM MỚI ==========
    public void lamMoi() {
        gui.getTxtTenNV().setText("");
        gui.getTxtSdt().setText("");
        gui.getTxtLuong().setText("");
        gui.getTxtEmail().setText("");
        gui.getCmbChucVu().setSelectedIndex(0);
        gui.getCmbGioiTinh().setSelectedIndex(0);
        gui.getDtpNgaySinh().setDate(null);
        gui.getDtpNgayVaoLam().setDate(null);
        gui.getImgAnhNV().setIcon(null);
        fileAnhDaChon = null;
    }

    // ========== THÊM NHÂN VIÊN ==========
    public void themNhanVien() {
        if (!kiemTraHopLe()) return;

        // Trích xuất toàn bộ dữ liệu UI trên Main Thread để tránh lỗi bất đồng bộ
        String maNV = tool.taoKhoaChinh("NV");
        String tenNV = gui.getTxtTenNV().getText();
        String chucVu = gui.getCmbChucVu().getSelectedItem().toString();
        String sdt = tool.chuyenSoDienThoai(gui.getTxtSdt().getText());
        String email = gui.getTxtEmail().getText();
        boolean gioiTinh = gui.getCmbGioiTinh().getSelectedItem().equals("Nam");
        LocalDate ngaySinh = tool.utilDateSangLocalDate(gui.getDtpNgaySinh().getDate());
        LocalDate ngayVaoLam = tool.utilDateSangLocalDate(gui.getDtpNgayVaoLam().getDate());

        // Kiểm tra xem Combobox Thuế đã tải xong chưa
        Object selectedThue = gui.getCmbThue().getSelectedItem();
        if (!(selectedThue instanceof Thue)) {
            showError("Dữ liệu thuế chưa được tải xong từ máy chủ, vui lòng đợi giây lát!");
            return;
        }
        Thue thue = (Thue) selectedThue;
        double luong = tool.chuyenTienSangSo(gui.getTxtLuong().getText());

        if (!tool.hienThiXacNhan("Thêm nhân viên?", "Bạn có chắc muốn thêm?", (JFrame) SwingUtilities.getWindowAncestor(gui))) {
            return;
        }

        // Tạo Thread riêng để gọi mạng và I/O File (chép ảnh)
        new Thread(() -> {
            try {
                // Xử lý sao chép file ảnh cục bộ
                String anh = luuAnh(maNV);

                NhanVien nv = new NhanVien(maNV, tenNV, chucVu, ngaySinh, gioiTinh, sdt, ngayVaoLam, luong, true, anh, thue);
                String maTK = tool.taoKhoaChinh("TK");
                String loaiTK = chucVu.equalsIgnoreCase("Nhân viên bán hàng") ? "Nhân viên" : "Quản lý";
                TaiKhoan tk = new TaiKhoan(maTK, maNV, maNV, true, email, loaiTK, nv);

                // Gọi tới Server qua Socket
                boolean kqNV = nvService.themNhanVien(nv);
                boolean kqTK = false;

                if (kqNV) {
                    kqTK = nvService.themTaiKhoan(tk);
                }

                // Chuyển kết quả về lại UI để hiển thị
                final boolean finalSuccess = (kqNV && kqTK);
                SwingUtilities.invokeLater(() -> {
                    if (finalSuccess) {
                        JOptionPane.showMessageDialog(null, "Thêm thành công!");
                        lamMoi();
                    } else {
                        JOptionPane.showMessageDialog(null, "Thêm thất bại từ máy chủ!");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> showError("Đã xảy ra lỗi hệ thống!"));
            }
        }).start();
    }

    // ========== KIỂM TRA HỢP LỆ ==========
    public boolean kiemTraHopLe() {
        String ten = gui.getTxtTenNV().getText().trim();
        if (ten.isEmpty()) { showError("Tên không được để trống"); return false; }
        if (!ten.matches("^[\\p{L}\\s]+$")) { showError("Tên chỉ gồm chữ cái và khoảng trắng"); return false; }

        String sdt = gui.getTxtSdt().getText().trim();
        if (!sdt.matches("^0\\d{9}$")) { showError("Số điện thoại phải 10 số và bắt đầu bằng 0"); return false; }

        String email = gui.getTxtEmail().getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
            showError("Email không hợp lệ"); return false;
        }

        String luongText = gui.getTxtLuong().getText().trim().replace(",", "");
        try {
            double luong = Double.parseDouble(luongText);
            if (luong <= 0) { showError("Lương phải > 0"); return false; }
        } catch (Exception e) {
            showError("Lương không hợp lệ!"); return false;
        }

        if (gui.getDtpNgaySinh().getDate() == null) { showError("Chưa chọn ngày sinh"); return false; }
        if (gui.getDtpNgayVaoLam().getDate() == null) { showError("Chưa chọn ngày vào làm"); return false; }

        return true;
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }
}