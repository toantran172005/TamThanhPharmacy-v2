package controller;

import java.time.LocalDate;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import entity.DonViTinh;
import entity.KeThuoc;
import entity.QuocGia;
import entity.Thue;
import entity.Thuoc;
import gui.ThemThuoc_GUI;
import service.DonViTinhService;
import service.KeThuocService;
import service.ThueService;
import service.ThuocService;
import utils.ToolCtrl;

public class ThemThuocController {

    private ThuocService thuocService = new ThuocService();
    private KeThuocService keThuocService = new KeThuocService();
    private DonViTinhService dvtService = new DonViTinhService();
    private ThueService thueService = new ThueService();

    private ToolCtrl tool = new ToolCtrl();
    private ThemThuoc_GUI themThuoc;
    private List<Thue> dsThue;

    public ThemThuocController(ThemThuoc_GUI themThuoc) {
        this.themThuoc = themThuoc;
    }

    public ThemThuocController() {
        super();
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
                    for (QuocGia qg : get()) {
                        themThuoc.cmbQuocGia.addItem(qg.getTenQuocGia());
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
                    for (KeThuoc kt : get()) {
                        themThuoc.cmbKeThuoc.addItem(kt.getLoaiKe());
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
                    for (DonViTinh dvt : get()) {
                        themThuoc.cmbDonVi.addItem(dvt.getTenDVT());
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
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
                    dsThue = get();
                    themThuoc.cmbThue.removeAllItems();
                    for (Thue th : dsThue) {
                        if (!th.getLoaiThue().equalsIgnoreCase("Thuế TNCN")) {
                            Double tyLe = th.getTyLeThue() * 100;
                            String text = th.getLoaiThue() + " " + tyLe + "%";
                            themThuoc.cmbThue.addItem(text);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    public Thue getThueDangChon() {
        String selected = (String) themThuoc.cmbThue.getSelectedItem();
        if (selected == null || dsThue == null) return null;

        for (Thue th : dsThue) {
            String text = th.getLoaiThue() + " " + (th.getTyLeThue() * 100) + "%";
            if (text.equals(selected)) return th;
        }
        return null;
    }

    // Tách logic kiểm tra mạng vào Thread
    public void themThuoc() {
        // 1. Đọc dữ liệu UI (Trên luồng chính)
        String tenThuoc = themThuoc.txtTenThuoc.getText().trim();
        String dangThuoc = themThuoc.txtDangThuoc.getText().trim();
        String donVi = themThuoc.cmbDonVi.getSelectedItem() != null ? themThuoc.cmbDonVi.getSelectedItem().toString() : "";
        String quocGia = themThuoc.cmbQuocGia.getSelectedItem() != null ? themThuoc.cmbQuocGia.getSelectedItem().toString() : "";
        String keThuoc = themThuoc.cmbKeThuoc.getSelectedItem() != null ? themThuoc.cmbKeThuoc.getSelectedItem().toString() : "";

        int soLuong = (int) themThuoc.spSoLuongTon.getValue();
        String giaBanStr = themThuoc.txtGiaBan.getText().trim();
        java.util.Date dateChooser = themThuoc.dpHanSuDung.getDate();
        String anh = themThuoc.urlAnh;

        // 2. Validate UI cơ bản
        if (tenThuoc.isEmpty() || dangThuoc.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Tên thuốc và dạng thuốc không được để trống!", false);
            return;
        }
        if (donVi.equals("Tất cả") || keThuoc.equals("Tất cả") || quocGia.equals("Tất cả") || donVi.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn đầy đủ Đơn vị / Quốc gia / Kệ thuốc!", false);
            return;
        }
        if (soLuong < 0) {
            tool.hienThiThongBao("Lỗi", "Số lượng tồn phải >= 0!", false);
            return;
        }
        if (giaBanStr.isEmpty()) {
            tool.hienThiThongBao("Lỗi", "Giá bán không được để trống!", false);
            return;
        }
        if (dateChooser == null) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn hạn sử dụng!", false);
            return;
        }
        LocalDate hanSuDung = tool.utilDateSangLocalDate(dateChooser);
        if (hanSuDung.isBefore(LocalDate.now())) {
            tool.hienThiThongBao("Lỗi", "Hạn sử dụng phải sau ngày hiện tại!", false);
            return;
        }
        if (anh == null) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn ảnh thuốc!", false);
            return;
        }

        double giaBanTemp;
        try {
            giaBanTemp = Double.parseDouble(giaBanStr);
            if (giaBanTemp <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            tool.hienThiThongBao("Lỗi", "Giá bán không hợp lệ!", false);
            return;
        }
        final double giaBan = giaBanTemp;
        Thue thue = getThueDangChon();

        // 3. Gọi mạng trên Thread riêng
        new Thread(() -> {
            try {
                String maQG = thuocService.layMaQuocGiaTheoTen(quocGia);

                if (thuocService.kiemTraTrungTenVaQuocGia(tenThuoc, maQG)) {
                    SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Cảnh báo",
                            "Thuốc '" + tenThuoc + "' thuộc quốc gia '" + quocGia + "' đã tồn tại!", false));
                    return;
                }

                QuocGia qgObj = new QuocGia(maQG, quocGia);
                Thuoc thuoc = new Thuoc();
                thuoc.setMaThuoc(tool.taoKhoaChinh("TH"));
                thuoc.setTenThuoc(tenThuoc);
                thuoc.setDangThuoc(dangThuoc);
                thuoc.setSoLuong(soLuong);
                thuoc.setGiaBan(giaBan);
                thuoc.setHanSuDung(hanSuDung);
                thuoc.setQuocGia(qgObj);
                thuoc.setAnh(anh);
                thuoc.setDonViTinh(dvtService.timTheoTen(donVi));
                thuoc.setKeThuoc(keThuocService.timTheoTen(keThuoc));
                thuoc.setThue(thue);

                boolean ketQua = thuocService.themThuoc(thuoc);

                // 4. Cập nhật UI
                SwingUtilities.invokeLater(() -> {
                    if (ketQua) {
                        tool.hienThiThongBao("Thành công", "Thêm thuốc thành công!", true);
                        resetForm();
                    } else {
                        tool.hienThiThongBao("Thất bại", "Thêm thuốc thất bại!", false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi mạng", "Có lỗi xảy ra khi thêm thuốc!", false));
            }
        }).start();
    }

    private void resetForm() {
        themThuoc.txtTenThuoc.setText("");
        themThuoc.txtDangThuoc.setText("");
        themThuoc.txtGiaBan.setText("");
        themThuoc.spSoLuongTon.setValue(0);
        themThuoc.dpHanSuDung.setDate(null);
        themThuoc.imgThuoc.setIcon(null);

        if(themThuoc.cmbDonVi.getItemCount() > 0) themThuoc.cmbDonVi.setSelectedIndex(0);
        if(themThuoc.cmbQuocGia.getItemCount() > 0) themThuoc.cmbQuocGia.setSelectedIndex(0);
        if(themThuoc.cmbKeThuoc.getItemCount() > 0) themThuoc.cmbKeThuoc.setSelectedIndex(0);
        if(themThuoc.cmbThue.getItemCount() > 0) themThuoc.cmbThue.setSelectedIndex(0);
    }
}