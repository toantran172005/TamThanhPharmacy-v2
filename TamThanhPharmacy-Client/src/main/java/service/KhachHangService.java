package service;

import entity.KhachHang;
import client.ClientSocketManager;
import network.Request;
import network.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KhachHangService {

    public KhachHangService() {
        // Đã xóa khởi tạo Repository
    }

    // ================== CÁC HÀM THÊM / CẬP NHẬT / XÓA ==================

    public boolean themKhachHang(KhachHang kh) {
        if (kh == null || kh.getMaKH() == null || kh.getMaKH().trim().isEmpty() ||
                kh.getTenKH() == null || kh.getTenKH().trim().isEmpty()) {
            return false;
        }
        if (kh.getTuoi() <= 0) {
            return false;
        }
        kh.setTrangThai(true);
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_THEM", kh));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean themKhachHang(String maKH, String tenKH, String sdt, String tuoiStr) {
        if (tuoiStr == null || tuoiStr.trim().isEmpty()) return false;
        try {
            int tuoi = Integer.parseInt(tuoiStr.trim());
            KhachHang kh = new KhachHang(maKH, tenKH, sdt, tuoi, true);
            return themKhachHang(kh);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        if (kh == null || kh.getMaKH() == null || kh.getMaKH().trim().isEmpty()) return false;
        if (kh.getTuoi() <= 0) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_CAP_NHAT", kh));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean xoaKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_XOA", maKH.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean khoiPhucKhachHang(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_KHOI_PHUC", maKH.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ================== CÁC HÀM LẤY DANH SÁCH & THỐNG KÊ ==================

    public List<KhachHang> layListKhachHang() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_LAY_LIST", null));
            if ("SUCCESS".equals(res.getStatus())) {
                List<KhachHang> list = (List<KhachHang>) res.getData();
                return list != null ? list : new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<KhachHang> layListKHThongKe(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null) throw new IllegalArgumentException("Ngày không được để trống.");
        if (ngayBD.isAfter(ngayKT)) throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");

        try {
            Map<String, LocalDate> data = new HashMap<>();
            data.put("ngayBD", ngayBD);
            data.put("ngayKT", ngayKT);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_LAY_LIST_THONG_KE", data));
            if ("SUCCESS".equals(res.getStatus())) {
                List<KhachHang> list = (List<KhachHang>) res.getData();
                return list != null ? list : new ArrayList<>();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public Map<String, Integer> layTongDonHangTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null || ngayBD.isAfter(ngayKT)) return new HashMap<>();
        try {
            Map<String, LocalDate> data = new HashMap<>();
            data.put("ngayBD", ngayBD);
            data.put("ngayKT", ngayKT);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TONG_DON_HANG_NGAY", data));
            if ("SUCCESS".equals(res.getStatus())) return (Map<String, Integer>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new HashMap<>();
    }

    public Map<String, Double> layTongTienTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null || ngayBD.isAfter(ngayKT)) return new HashMap<>();
        try {
            Map<String, LocalDate> data = new HashMap<>();
            data.put("ngayBD", ngayBD);
            data.put("ngayKT", ngayKT);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TONG_TIEN_NGAY", data));
            if ("SUCCESS".equals(res.getStatus())) return (Map<String, Double>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new HashMap<>();
    }

    public Map<String, Integer> layTatCaTongDonHang() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TAT_CA_TONG_DON", null));
            if ("SUCCESS".equals(res.getStatus())) return (Map<String, Integer>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new HashMap<>();
    }

    public Map<String, Double> layTatCaTongTien() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TAT_CA_TONG_TIEN", null));
            if ("SUCCESS".equals(res.getStatus())) return (Map<String, Double>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new HashMap<>();
    }

    // ================== CÁC HÀM TÌM KIẾM ĐƠN LẺ ==================

    public KhachHang timKhachHangTheoSDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TIM_THEO_SDT", sdt.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (KhachHang) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public KhachHang timKhachHangTheoMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KH_TIM_THEO_MA", maKH.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (KhachHang) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}