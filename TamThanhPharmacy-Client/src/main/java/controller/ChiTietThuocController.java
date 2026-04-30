package controller;

import java.awt.Image;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import service.DonViTinhService;
import service.KeThuocService;
import service.ThueService;
import service.ThuocService;
import utils.ToolCtrl;

import entity.DonViTinh;
import entity.KeThuoc;
import entity.QuocGia;
import entity.Thue;
import entity.Thuoc;
import gui.ChiTietThuoc_GUI;
import gui.TimKiemThuoc_GUI;

public class ChiTietThuocController {

    public ToolCtrl tool = new ToolCtrl();
    public ThuocService thuocService = new ThuocService();
    public KeThuocService keThuocService = new KeThuocService();
    public ThueService thueService = new ThueService();
    public DonViTinhService dvtService = new DonViTinhService();

    public ChiTietThuoc_GUI ctThuoc;
    public String duongDanAnhHienTai = null;
    public ArrayList<Thue> dsThue;

    public ChiTietThuocController(ChiTietThuoc_GUI ctThuoc) {
        this.ctThuoc = ctThuoc;
    }

    // Đã bọc SwingWorker
    public void setCmbQuocGia() {
        new SwingWorker<List<QuocGia>, Void>() {
            @Override
            protected List<QuocGia> doInBackground() {
                return thuocService.layListQG();
            }
            @Override
            protected void done() {
                try {
                    for (QuocGia qg : get()) ctThuoc.cmbQuocGia.addItem(qg.getTenQuocGia());
                } catch (Exception e) {}
            }
        }.execute();
    }

    // Đã bọc SwingWorker
    public void setCmbKeThuoc() {
        new SwingWorker<List<KeThuoc>, Void>() {
            @Override
            protected List<KeThuoc> doInBackground() {
                return keThuocService.layListKeThuoc();
            }
            @Override
            protected void done() {
                try {
                    for (KeThuoc kt : get()) ctThuoc.cmbKeThuoc.addItem(kt.getLoaiKe());
                } catch (Exception e) {}
            }
        }.execute();
    }

    // Đã bọc SwingWorker
    public void setCmbDonVi() {
        new SwingWorker<List<DonViTinh>, Void>() {
            @Override
            protected List<DonViTinh> doInBackground() {
                return dvtService.layListDVT();
            }
            @Override
            protected void done() {
                try {
                    for (DonViTinh dvt : get()) ctThuoc.cmbDonViTinh.addItem(dvt.getTenDVT());
                } catch (Exception e) {}
            }
        }.execute();
    }

    private String taoChuoiHienThiThue(Thue t) {
        double tyLe = t.getTyLeThue() * 100;
        if (tyLe == (long) tyLe) {
            return String.format("%s %d%%", t.getLoaiThue(), (long) tyLe);
        } else {
            return String.format("%s %s%%", t.getLoaiThue(), tyLe);
        }
    }

    // Đã bọc SwingWorker
    public void setCmbThue() {
        new SwingWorker<List<Thue>, Void>() {
            @Override
            protected List<Thue> doInBackground() {
                return thueService.layListThue();
            }
            @Override
            protected void done() {
                try {
                    dsThue = new ArrayList<>(get());
                    ctThuoc.cmbThue.removeAllItems();
                    for (Thue th : dsThue) {
                        if (!th.getLoaiThue().equalsIgnoreCase("Thuế TNCN")) {
                            ctThuoc.cmbThue.addItem(taoChuoiHienThiThue(th));
                        }
                    }
                } catch (Exception e) {}
            }
        }.execute();
    }

    public Thue getThueDangChon() {
        String selected = (String) ctThuoc.cmbThue.getSelectedItem();
        if (selected == null || dsThue == null) return null;
        for (Thue th : dsThue) {
            if (taoChuoiHienThiThue(th).equals(selected)) return th;
        }
        return null;
    }

    // Đã bọc SwingWorker tải thuốc chi tiết
    public void xemChiTietThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) {
            tool.hienThiThongBao("Lỗi!", "Mã thuốc không hợp lệ", false);
            return;
        }

        new SwingWorker<Thuoc, Void>() {
            @Override
            protected Thuoc doInBackground() {
                return thuocService.timThuocTheoMa(maThuoc);
            }
            @Override
            protected void done() {
                try {
                    Thuoc t = get();
                    if (t != null) {
                        duongDanAnhHienTai = t.getAnh();
                        hienThiAnhLenLabel(duongDanAnhHienTai);

                        ctThuoc.txtMaSP.setText(maThuoc);
                        ctThuoc.txtTenSP.setText(t.getTenThuoc());
                        ctThuoc.txtDangThuoc.setText(t.getDangThuoc());
                        ctThuoc.txtGiaBan.setText(tool.dinhDangVND(t.getGiaBan()));
                        ctThuoc.dpHanSuDung.setDate(tool.localDateSangUtilDate(t.getHanSuDung()));

                        if (t.getDonViTinh() != null) ctThuoc.cmbDonViTinh.setSelectedItem(t.getDonViTinh().getTenDVT());
                        if (t.getThue() != null) ctThuoc.cmbThue.setSelectedItem(taoChuoiHienThiThue(t.getThue()));
                        if (t.getKeThuoc() != null) ctThuoc.cmbKeThuoc.setSelectedItem(t.getKeThuoc().getLoaiKe());
                        if (t.getQuocGia() != null && t.getQuocGia().getTenQuocGia() != null) {
                            ctThuoc.cmbQuocGia.setSelectedItem(t.getQuocGia().getTenQuocGia());
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    public void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh thuốc");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png", "jpeg"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String projectPath = System.getProperty("user.dir");
                String folderPath = projectPath + File.separator + "resource" + File.separator + "picture"
                        + File.separator + "thuoc";

                File folder = new File(folderPath);
                if (!folder.exists()) folder.mkdirs();

                String fileName = selectedFile.getName();
                File destFile = new File(folderPath + File.separator + fileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                duongDanAnhHienTai = "/picture/thuoc/" + fileName;

                ImageIcon icon = new ImageIcon(destFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                ctThuoc.lblAnh.setIcon(new ImageIcon(img));
                ctThuoc.lblAnh.setText("");

            } catch (Exception e) {
                e.printStackTrace();
                tool.hienThiThongBao("Lỗi", "Lỗi khi lưu ảnh: " + e.getMessage(), false);
            }
        }
    }

    public void hienThiAnhLenLabel(String pathRel) {
        try {
            ctThuoc.lblAnh.setIcon(null);
            ctThuoc.lblAnh.setText("Đang tải...");

            if (pathRel == null || pathRel.isEmpty()) {
                ctThuoc.lblAnh.setText("Không có ảnh");
                return;
            }

            ImageIcon icon = null;
            java.net.URL imgURL = getClass().getResource(pathRel);
            if (imgURL != null) {
                icon = new ImageIcon(imgURL);
            }

            if (icon == null) {
                String projectPath = System.getProperty("user.dir");
                String absolutePath = projectPath + "/resource" + pathRel;
                File file = new File(absolutePath);
                if (file.exists()) icon = new ImageIcon(absolutePath);
            }

            if (icon != null) {
                Image img = icon.getImage().getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                ctThuoc.lblAnh.setIcon(new ImageIcon(img));
                ctThuoc.lblAnh.setText("");
            } else {
                ctThuoc.lblAnh.setText("Ảnh lỗi");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctThuoc.lblAnh.setText("Lỗi ảnh");
        }
    }

    // Tách kiểm tra UI ra riêng (EDT)
    private boolean kiemTraUI() {
        if (ctThuoc.txtTenSP.getText().trim().isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Tên thuốc không được để trống", false);
            ctThuoc.txtTenSP.requestFocus();
            return false;
        }
        if (ctThuoc.txtGiaBan.getText().trim().isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Giá bán không được để trống", false);
            ctThuoc.txtGiaBan.requestFocus();
            return false;
        }
        if (ctThuoc.dpHanSuDung.getDate() == null) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn hạn sử dụng", false);
            return false;
        }
        return true;
    }

    // Đẩy xử lý mạng (kiểm tra sức chứa kệ & lưu Data) vào Thread
    public void xuLyCapNhat() {
        String trangThaiHienTai = ctThuoc.btnCapNhat.getText();

        if (trangThaiHienTai.equalsIgnoreCase("Cập nhật")) {
            ctThuoc.thietLapKhoaChinhSua(true);
            ctThuoc.btnCapNhat.setText("Lưu");

            String giaHienTai = ctThuoc.txtGiaBan.getText();
            try {
                String giaSo = giaHienTai.replaceAll("[^0-9]", "");
                ctThuoc.txtGiaBan.setText(giaSo);
            } catch (Exception e) {
                ctThuoc.txtGiaBan.setText("0");
            }
            ctThuoc.txtTenSP.requestFocus();

        } else {
            // Validate text trước tiên
            if (!kiemTraUI()) return;

            // Đọc UI thành biến cục bộ
            String maThuoc = ctThuoc.txtMaSP.getText();
            String tenThuoc = ctThuoc.txtTenSP.getText().trim();
            String dangThuoc = ctThuoc.txtDangThuoc.getText().trim();
            String giaBanStr = ctThuoc.txtGiaBan.getText().trim().replaceAll("[^0-9]", "");
            double giaBanTemp;
            try {
                giaBanTemp = giaBanStr.isEmpty() ? 0 : Double.parseDouble(giaBanStr);
            } catch (Exception e) {
                tool.hienThiThongBao("Lỗi nhập liệu", "Giá bán chứa ký tự không hợp lệ!", false);
                return;
            }
            final double giaBan = giaBanTemp;

            LocalDate hanSuDung = tool.utilDateSangLocalDate(ctThuoc.dpHanSuDung.getDate());
            String tenDVT = ctThuoc.cmbDonViTinh.getSelectedItem() != null ? ctThuoc.cmbDonViTinh.getSelectedItem().toString() : "";
            Thue thue = getThueDangChon();
            if (thue == null) {
                tool.hienThiThongBao("Lỗi", "Vui lòng chọn loại thuế!", false);
                return;
            }
            String tenKe = ctThuoc.cmbKeThuoc.getSelectedItem() != null ? ctThuoc.cmbKeThuoc.getSelectedItem().toString() : "";
            String tenQG = ctThuoc.cmbQuocGia.getSelectedItem() != null ? ctThuoc.cmbQuocGia.getSelectedItem().toString() : null;

            // Xử lý mạng trong Thread
            new Thread(() -> {
                try {
                    // Kiểm tra sức chứa (Network-heavy)
                    List<KeThuoc> listKe = keThuocService.layListKeThuoc();
                    boolean keDay = false;
                    for (KeThuoc ke : listKe) {
                        if (tenKe.equalsIgnoreCase(ke.getLoaiKe())) {
                            int sucChuaHienTai = ke.getSucChua() - keThuocService.layListThuocTrongKe(ke.getMaKe()).size();
                            if (sucChuaHienTai <= 0) keDay = true;
                            break;
                        }
                    }

                    if (keDay) {
                        SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Cập nhật", "Kệ thuốc " + tenKe + " đã đầy!", false));
                        return;
                    }

                    // Mapping DVT, Ke, QG (Network)
                    DonViTinh dvt = dvtService.timTheoTen(tenDVT);
                    KeThuoc ke = keThuocService.timTheoTen(tenKe);
                    QuocGia qg = null;
                    if (tenQG != null) {
                        for (QuocGia q : thuocService.layListQG()) {
                            if (q.getTenQuocGia().equalsIgnoreCase(tenQG)) {
                                qg = q; break;
                            }
                        }
                    }

                    Thuoc t = new Thuoc();
                    t.setMaThuoc(maThuoc);
                    t.setTenThuoc(tenThuoc);
                    t.setDangThuoc(dangThuoc);
                    t.setGiaBan(giaBan);
                    t.setHanSuDung(hanSuDung);
                    t.setDonViTinh(dvt);
                    t.setThue(thue);
                    t.setKeThuoc(ke);
                    t.setQuocGia(qg);
                    t.setTrangThai(true);
                    t.setAnh(duongDanAnhHienTai);

                    boolean ketQua = thuocService.capNhatThuoc(t);

                    SwingUtilities.invokeLater(() -> {
                        if (ketQua) {
                            tool.hienThiThongBao("Thành công", "Cập nhật thông tin thuốc thành công!", true);
                            ctThuoc.thietLapKhoaChinhSua(false);
                            ctThuoc.btnCapNhat.setText("Cập nhật");
                            ctThuoc.txtGiaBan.setText(tool.dinhDangVND(t.getGiaBan()));
                            xemChiTietThuoc(t.getMaThuoc()); // Tải lại qua SwingWorker
                        } else {
                            tool.hienThiThongBao("Thất bại", "Cập nhật thất bại, vui lòng kiểm tra lại!", false);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Lỗi xử lý dữ liệu mạng!", false));
                }
            }).start();
        }
    }

    public void quayLaiTrangTimKiem() {
        tool.doiPanel(ctThuoc, new TimKiemThuoc_GUI());
    }
}