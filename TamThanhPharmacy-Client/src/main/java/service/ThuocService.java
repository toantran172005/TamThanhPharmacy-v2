package service;

import entity.DonViTinh;
import entity.QuocGia;
import entity.Thue;
import entity.Thuoc;
import client.ClientSocketManager;
import network.Request;
import network.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThuocService {

    public boolean capNhatTrangThaiThuoc(String maThuoc, boolean isKhuyenPhuc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return false;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maThuoc", maThuoc.trim());
            data.put("trangThai", isKhuyenPhuc);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("CAP_NHAT_TRANG_THAI_THUOC", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<Thuoc> layListThuocHoanChinh() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_THUOC_HOAN_CHINH", null));
            if ("SUCCESS".equals(res.getStatus())) return (List<Thuoc>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public ArrayList<Thuoc> layListThuoc() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_THUOC", null));
            if ("SUCCESS".equals(res.getStatus())) {
                List<Thuoc> listDb = (List<Thuoc>) res.getData();
                if (listDb == null || listDb.isEmpty()) return new ArrayList<>();
                listDb.sort((t1, t2) -> {
                    try {
                        int num1 = Integer.parseInt(t1.getMaThuoc().replace("TTTH", ""));
                        int num2 = Integer.parseInt(t2.getMaThuoc().replace("TTTH", ""));
                        return Integer.compare(num1, num2);
                    } catch (Exception e) { return 0; }
                });
                return new ArrayList<>(listDb);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<Object[]> thongKeThuocBanChay(LocalDate bd, LocalDate kt) {
        if (bd.isAfter(kt)) throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("ngayBD", bd);
            data.put("ngayKT", kt);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_TH_THONG_KE", data));
            if ("SUCCESS".equals(res.getStatus())) return (List<Object[]>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public boolean themThuoc(Thuoc thuoc) {
        if (thuoc == null || thuoc.getTenThuoc() == null || thuoc.getTenThuoc().trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THEM_THUOC", thuoc));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean capNhatThuoc(Thuoc thuoc) {
        if (thuoc == null || thuoc.getMaThuoc() == null) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("CAP_NHAT_THUOC", thuoc));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public Thuoc timThuocTheoMa(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("TIM_THUOC_THEO_MA", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (Thuoc) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<QuocGia> layListQG() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_QG", null));
            if ("SUCCESS".equals(res.getStatus())) return (List<QuocGia>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public String timTenQGTheoMaThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("TIM_TEN_QG_THEO_MA_THUOC", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String layMaKMTheoMaThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_MA_KM_THEO_MA_THUOC", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String layMaThuocTheoTen(String tenThuoc) {
        if (tenThuoc == null || tenThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_MA_THUOC_THEO_TEN", tenThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public ArrayList<QuocGia> layListQuocGiaTheoThuoc(String tenThuoc) {
        if (tenThuoc == null || tenThuoc.trim().isEmpty()) return new ArrayList<>();
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_QG_THEO_THUOC", tenThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) {
                List<QuocGia> list = (List<QuocGia>) res.getData();
                return list != null ? new ArrayList<>(list) : new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public String layTenDonViTinhTheoMaThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_TEN_DVT_THEO_MA_THUOC", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String layMaThuocTheoTenVaQG(String tenThuoc, String tenQG) {
        if (tenThuoc == null || tenThuoc.trim().isEmpty() || tenQG == null || tenQG.trim().isEmpty()) return null;
        try {
            Map<String, String> data = new HashMap<>();
            data.put("tenThuoc", tenThuoc.trim());
            data.put("tenQG", tenQG.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_MA_THUOC_THEO_TEN_VA_QG", data));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public int laySoLuongTon(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return 0;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_SO_LUONG_TON", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (int) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public boolean capNhatSoLuongTon(String maThuoc, String maDVT, int soLuong, boolean isTang) {
        if (maThuoc == null || maThuoc.trim().isEmpty() || maDVT == null || maDVT.trim().isEmpty() || soLuong < 0) return false;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maThuoc", maThuoc);
            data.put("maDVT", maDVT);
            data.put("soLuong", soLuong);
            data.put("isTang", isTang);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("CAP_NHAT_SL_TON", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean luuData(String maPNT, String maNCC, String maNV, LocalDate ngayNhap, List<Thuoc> listThuoc) {
        if (listThuoc == null || listThuoc.isEmpty()) return false;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maPNT", maPNT);
            data.put("maNCC", maNCC);
            data.put("maNV", maNV);
            data.put("ngayNhap", ngayNhap);
            data.put("listThuoc", listThuoc);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LUU_DATA", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String layHoacTaoThue(Thue thue) {
        if (thue == null || thue.getLoaiThue() == null) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_HOAC_TAO_THUE", thue));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String layHoacTaoDVT(DonViTinh dvt) {
        if (dvt == null || dvt.getTenDVT() == null || dvt.getTenDVT().trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_HOAC_TAO_DVT", dvt));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public List<Thuoc> layListThuoc(boolean trangThai) {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_THUOC_TRANG_THAI", trangThai));
            if ("SUCCESS".equals(res.getStatus())) return (List<Thuoc>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<Object[]> layDanhSachThuocChoKM() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_DS_THUOC_CHO_KM", null));
            if ("SUCCESS".equals(res.getStatus())) return (List<Object[]>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public Thuoc layThuocDeDat(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_THUOC_DE_DAT", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (Thuoc) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean kiemTraTrungTenVaQuocGia(String tenThuoc, String maQG) {
        if (tenThuoc == null || tenThuoc.trim().isEmpty() || maQG == null || maQG.trim().isEmpty()) return false;
        try {
            Map<String, String> data = new HashMap<>();
            data.put("tenThuoc", tenThuoc);
            data.put("maQG", maQG);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KIEM_TRA_TRUNG_TEN_VA_QG", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String layMaQuocGiaTheoTen(String tenQG) {
        if (tenQG == null || tenQG.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_MA_QG_THEO_TEN", tenQG.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public String timMaQGTheoTen(String tenQG) {
        if (tenQG == null || tenQG.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("TIM_MA_QG_THEO_TEN", tenQG.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (String) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public QuocGia layQuocGiaTheoMa(String maQG) {
        if (maQG == null || maQG.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_QUOC_GIA_THEO_MA", maQG.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (QuocGia) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public Number layGiaBanTheoDVT(String maThuoc, String maDVT) {
        if (maThuoc == null || maThuoc.trim().isEmpty() || maDVT == null || maDVT.trim().isEmpty()) return 0;
        try {
            Map<String, String> data = new HashMap<>();
            data.put("maThuoc", maThuoc);
            data.put("maDVT", maDVT);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_GIA_BAN_THEO_DVT", data));
            if ("SUCCESS".equals(res.getStatus())) return (Number) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double layDonGiaTheoMaThuoc(String maThuoc) {
        if (maThuoc == null || maThuoc.trim().isEmpty()) return 0.0;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_DON_GIA_THEO_MA_THUOC", maThuoc.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (double) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public boolean giamSoLuongTon(String maThuoc, String maDVT, int soLuongBan) {
        if (maThuoc == null || maThuoc.isBlank()) throw new IllegalArgumentException("Mã thuốc không hợp lệ");
        if (maDVT == null || maDVT.isBlank()) throw new IllegalArgumentException("Mã đơn vị tính không hợp lệ");
        if (soLuongBan <= 0) throw new IllegalArgumentException("Số lượng bán phải > 0");

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maThuoc", maThuoc);
            data.put("maDVT", maDVT);
            data.put("soLuongBan", soLuongBan);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("GIAM_SL_TON", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
            else throw new RuntimeException("Không thể giảm số lượng tồn");
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mạng khi giảm số lượng tồn", e);
        }
    }
}