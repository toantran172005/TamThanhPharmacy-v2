package service;

import client.ClientSocketManager;
import entity.CTHoaDon;
import entity.HoaDon;
import entity.KhachHang;
import network.Request;
import network.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoaDonService {

    @SuppressWarnings("unchecked")
    public List<HoaDon> layListHoaDon() {
        try {
            Request req = new Request("LAY_LIST_HOA_DON", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<HoaDon>) res.getData();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public List<HoaDon> layListHDDaXoa() {
        try {
            Request req = new Request("LAY_LIST_HD_DA_XOA", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<HoaDon>) res.getData();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public Map<LocalDate, Double> layDoanhThuTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if(ngayBD == null || ngayKT == null) throw new IllegalArgumentException("Ngày không hợp lệ!");
        try {
            Request req = new Request("LAY_DOANH_THU_THEO_NGAY", new Object[]{ngayBD, ngayKT});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (Map<LocalDate, Double>) res.getData();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public List<KhachHang> layListKHThongKe(LocalDate ngayBD, LocalDate ngayKT) {
        try {
            Request req = new Request("LAY_LIST_KH_THONG_KE", new Object[]{ngayBD, ngayKT});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<KhachHang>) res.getData();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public HoaDon timHoaDonTheoMa(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) return null;
        try {
            Request req = new Request("TIM_HOA_DON_THEO_MA", maHD.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (HoaDon) res.getData() : null;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean themHoaDon(HoaDon hd) {
        if (hd == null || hd.getMaHD() == null) return false;
        try {
            Request req = new Request("THEM_HOA_DON", hd);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean themChiTietHoaDon(CTHoaDon cthd) {
        if(cthd == null || cthd.getHoaDon() == null || cthd.getThuoc() == null) return false;
        if(cthd.getSoLuong() <= 0) return false;
        try {
            Request req = new Request("THEM_CHI_TIET_HOA_DON", cthd);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean xoaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) return false;
        try {
            Request req = new Request("XOA_HD", maHD.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean khoiPhucHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) return false;
        try {
            Request req = new Request("KHOI_PHUC_HD", maHD.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public int layTongDonHang(String maKH) {
        if (maKH == null || maKH.isEmpty()) return 0;
        try {
            Request req = new Request("LAY_TONG_DON_HANG", maKH);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (int) res.getData() : 0;
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double layTongTien(String maKH) {
        if (maKH == null || maKH.isEmpty()) return 0.0;
        try {
            Request req = new Request("LAY_TONG_TIEN", maKH);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (double) res.getData() : 0.0;
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public double layTongTienTheoSanPham(String maHD, String maSP) {
        try {
            Request req = new Request("LAY_TONG_TIEN_THEO_SAN_PHAM", new Object[]{maHD, maSP});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (double) res.getData() : 0.0;
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    public double tinhTongTienTheoHoaDon(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) return 0.0;
        try {
            Request req = new Request("TINH_TONG_TIEN_THEO_HOA_DON", maHD.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (double) res.getData() : 0.0;
        } catch (Exception e) { e.printStackTrace(); }
        return 0.0;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> layChiTietHoaDon(String maHD) {
        try {
            Request req = new Request("LAY_CHI_TIET_HOA_DON", maHD);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<Object[]>) res.getData();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public String layMaHoaDonMoiNhat() {
        try {
            Request req = new Request("LAY_MA_HOA_DON_MOI_NHAT", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) ? (String) res.getData() : null;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}