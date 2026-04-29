package controller;

import entity.NhanVien;
import gui.ChiTietNhanVien_GUI;
import service.NhanVienService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.toedter.calendar.JDateChooser;
import utils.ToolCtrl;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ChiTietNhanVienController {
    public ChiTietNhanVien_GUI gui;
    public JComboBox<String> cmbGioiTinh;
    public JComboBox<String> cmbChucVu;
    public JButton btnChonAnh, btnCapNhat, btnLamMoi, btnLuu;
    public JTextField txtMaNV, txtTenNV, txtSdt, txtLuong, txtEmail, txtThue;
    public JDateChooser dtpNgaySinh, dtpNgayVaoLam;
    public JLabel lblTrangThai;
    public JLabel imgAnhNV;
    public String tenNV_goc, luong_goc, thue_goc, sdt_goc, chucVu_goc, gioiTinh_goc, email_goc;
    public Image anh_goc;
    public File fileAnhDaChon;
    public String duongDanAnh;
    public ToolCtrl tool = new ToolCtrl();

    public NhanVienService nvService = new NhanVienService();

    public ChiTietNhanVienController(ChiTietNhanVien_GUI gui) {
        this.gui = gui;

        this.cmbGioiTinh = gui.getCmbGioiTinh();
        this.cmbChucVu = gui.getCmbChucVu();
        this.btnChonAnh = gui.getBtnChonAnh();
        this.btnCapNhat = gui.getBtnCapNhat();
        this.btnLamMoi = gui.getBtnLamMoi();
        this.btnLuu = gui.getBtnLuu();
        this.txtMaNV = gui.getTxtMaNV();
        this.txtTenNV = gui.getTxtTenNV();
        this.txtSdt = gui.getTxtSdt();
        this.txtLuong = gui.getTxtLuong();
        this.txtEmail = gui.getTxtEmail();
        this.txtThue = gui.getTxtThue();
        this.dtpNgaySinh = gui.getDtpNgaySinh();
        this.dtpNgayVaoLam = gui.getDtpNgayVaoLam();
        this.lblTrangThai = gui.getLblTrangThai();
        this.imgAnhNV = gui.getImgAnhNV();

        suKien();
    }

    // ========== SỰ KIỆN ==========
    public void suKien() {
        setItemComboBoxGioiTinh();
        setItemComboBoxChucVu();
        khoiTaoCheDoEdit(false);

        btnLamMoi.addActionListener(e -> khoiPhucDuLieu());
        btnCapNhat.addActionListener(e -> khoiTaoCheDoEdit(true));
        btnChonAnh.addActionListener(e -> chonAnhMoi());
        btnLuu.addActionListener(e -> capNhatNhanVien());
    }

    // ========== COMBOBOX ==========
    public void setItemComboBoxGioiTinh() {
        cmbGioiTinh.removeAllItems();
        cmbGioiTinh.addItem("Nam");
        cmbGioiTinh.addItem("Nữ");
    }

    public void setItemComboBoxChucVu() {
        cmbChucVu.removeAllItems();
        cmbChucVu.addItem("Nhân viên quản lý");
        cmbChucVu.addItem("Nhân viên bán hàng");
    }

    // ========== HIỂN THỊ THÔNG TIN NV ==========
    public void setNhanVienHienTai(NhanVien nv) {
        if (nv == null) return;

        txtMaNV.setText(nv.getMaNV());
        txtTenNV.setText(nv.getTenNV());
        cmbChucVu.setSelectedItem(nv.getChucVu());
        txtSdt.setText(tool.chuyenSoDienThoai(nv.getSdt()));
        txtLuong.setText(tool.dinhDangVND(nv.getLuong()));
        cmbGioiTinh.setSelectedItem(nv.getGioiTinh() ? "Nam" : "Nữ");
        dtpNgaySinh.setDate(Date.from(nv.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dtpNgayVaoLam.setDate(Date.from(nv.getNgayVaoLam().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        txtThue.setText(nv.getThue() != null ? nv.getThue().getMaThue() + "" : "");
        lblTrangThai.setText(nv.getTrangThai() ? "Còn làm" : "Đã nghỉ");
        imgAnhNV.setIcon(new ImageIcon(taiAnh(nv.getAnh())));

        new Thread(() -> {
            String email = nvService.layEmailNV(nv.getMaNV());

            SwingUtilities.invokeLater(() -> {
                txtEmail.setText(email);
                luuDuLieuGoc();
            });
        }).start();
    }

    // ========== TẢI HÌNH ẢNH ==========
    public Image taiAnh(String duongDanTuDB) {
        Image img = null;
        try {
            if (duongDanTuDB != null && !duongDanTuDB.trim().isEmpty()) {
                File file = new File(System.getProperty("user.dir") + "/src/main/resources" + duongDanTuDB);
                if (file.exists()) {
                    img = new ImageIcon(file.getAbsolutePath()).getImage();
                }
            }
            if (img == null) {
                img = new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/picture/default.png").getImage();
            }
            return img.getScaledInstance(180, 220, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
            return new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/picture/default.png").getImage();
        }
    }

    // ========== CHỌN ẢNH ==========
    public void chonAnhMoi() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh nhân viên");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            fileAnhDaChon = chooser.getSelectedFile();
            Image imgDaChon = new ImageIcon(fileAnhDaChon.getAbsolutePath()).getImage();
            Image scaledImg = imgDaChon.getScaledInstance(180, 220, Image.SCALE_SMOOTH);
            imgAnhNV.setIcon(new ImageIcon(scaledImg));
        }
    }

    // ========== LƯU DỮ LIỆU GỐC ==========
    public void luuDuLieuGoc() {
        tenNV_goc = txtTenNV.getText();
        sdt_goc = txtSdt.getText();
        luong_goc = txtLuong.getText();
        email_goc = txtEmail.getText();
        chucVu_goc = (String) cmbChucVu.getSelectedItem();
        gioiTinh_goc = (String) cmbGioiTinh.getSelectedItem();

        Icon icon = imgAnhNV.getIcon();
        if (icon instanceof ImageIcon) {
            anh_goc = ((ImageIcon) icon).getImage();
        }
    }

    // ========== KHÔI PHỤC ==========
    public void khoiPhucDuLieu() {
        txtTenNV.setText(tenNV_goc);
        txtSdt.setText(sdt_goc);
        txtLuong.setText(luong_goc);
        txtEmail.setText(email_goc);
        cmbChucVu.setSelectedItem(chucVu_goc);
        cmbGioiTinh.setSelectedItem(gioiTinh_goc);
        imgAnhNV.setIcon(new ImageIcon(anh_goc));
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

    // ========== SETUP CẬP NHẬT ==========
    public void khoiTaoCheDoEdit(boolean isEdit) {
        txtTenNV.setEditable(isEdit);
        txtLuong.setEditable(isEdit);
        txtSdt.setEditable(isEdit);
        txtEmail.setEditable(isEdit);

        cmbChucVu.setEnabled(isEdit);
        cmbGioiTinh.setEnabled(isEdit);

        btnLamMoi.setVisible(isEdit);
        btnChonAnh.setVisible(isEdit);
        btnLuu.setVisible(isEdit);
        btnCapNhat.setVisible(!isEdit);
    }

    // ========== CẬP NHẬT NV ==========
    public void capNhatNhanVien() {
        if (!kiemTraHopLe()) return;

        // Trích xuất dữ liệu UI trên luồng Main Thread để tránh lỗi bất đồng bộ Swing
        String maNV = txtMaNV.getText();
        String tenNV = txtTenNV.getText();
        String chucVu = (String) cmbChucVu.getSelectedItem();
        String sdt = tool.chuyenSoDienThoai(txtSdt.getText());
        boolean gioiTinh = cmbGioiTinh.getSelectedItem().equals("Nam");
        LocalDate ngaySinh = dtpNgaySinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ngayVaoLam = dtpNgayVaoLam.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        double luongTemp = 0;
        try { luongTemp = tool.chuyenTienSangSo(txtLuong.getText()); } catch (Exception e) {}
        double luong = luongTemp;
        boolean trangThai = lblTrangThai.getText().equals("Còn làm");

        String duongDanAnhMoi = null;
        if (fileAnhDaChon != null) {
            duongDanAnhMoi = luuAnh(maNV);
        }
        final String anhUpdate = duongDanAnhMoi;

        new Thread(() -> {
            try {
                NhanVien nvUpdate = nvService.timNhanVienTheoMa(maNV); // Gọi Server
                if (nvUpdate == null) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Không tìm thấy nhân viên trong CSDL!", false));
                    return;
                }

                // Gán dữ liệu mới
                nvUpdate.setTenNV(tenNV);
                nvUpdate.setChucVu(chucVu);
                nvUpdate.setSdt(sdt);
                nvUpdate.setGioiTinh(gioiTinh);
                nvUpdate.setNgaySinh(ngaySinh);
                nvUpdate.setNgayVaoLam(ngayVaoLam);
                nvUpdate.setLuong(luong);
                nvUpdate.setTrangThai(trangThai);
                if (anhUpdate != null) {
                    nvUpdate.setAnh(anhUpdate);
                }

                boolean kq = nvService.capNhatNhanVien(nvUpdate); // Gọi Server

                // Trả kết quả về giao diện
                SwingUtilities.invokeLater(() -> {
                    if (kq) {
                        tool.hienThiThongBao("Thành công", "Cập nhật nhân viên thành công!", true);
                        khoiTaoCheDoEdit(false);
                        luuDuLieuGoc(); // Cập nhật lại bản sao lưu
                    } else {
                        tool.hienThiThongBao("Lỗi", "Không thể cập nhật nhân viên!", false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Đã xảy ra lỗi hệ thống!", false));
            }
        }).start();
    }

    // ========== KIỂM TRA HỢP LỆ ==========
    public boolean kiemTraHopLe() {
        String ten = txtTenNV.getText().trim();
        if (ten.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Tên nhân viên không được để trống!", false);
            return false;
        }
        if (!ten.matches("^[\\p{L}\\s]+$")) {
            tool.hienThiThongBao("Lỗi", "Tên nhân viên chỉ chứa chữ cái!", false);
            return false;
        }
        String sdt = txtSdt.getText().trim();
        if (!sdt.matches("^0\\d{9}$")) {
            tool.hienThiThongBao("Lỗi", "SĐT phải có 10 chữ số!", false);
            return false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            tool.hienThiThongBao("Lỗi", "Email không hợp lệ!", false);
            return false;
        }
        try {
            double luong = tool.chuyenTienSangSo(txtLuong.getText());
            if (luong <= 0) {
                tool.hienThiThongBao("Lỗi", "Lương phải > 0!", false);
                return false;
            }
        } catch (Exception e) {
            tool.hienThiThongBao("Lỗi", "Lương không hợp lệ!", false);
            return false;
        }
        return true;
    }
}