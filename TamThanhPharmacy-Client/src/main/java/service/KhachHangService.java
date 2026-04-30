package service;

import client.ClientSocketManager;
import entity.KhachHang;
import network.Request;
import network.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KhachHangService {
    private ClientSocketManager socket = ClientSocketManager.getInstance();

    public List<KhachHang> layListKhachHang() {
        try {
            Request req = new Request("LAY_LIST_KHACH_HANG", null);
            Response res = socket.sendRequest(req);
            return (List<KhachHang>) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public KhachHang timKhachHangTheoSDT(String sdt) {
        try {
            Request req = new Request("TIM_KH_SDT", sdt);
            Response res = socket.sendRequest(req);
            return (KhachHang) res.getData();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean themKhachHang(KhachHang kh) {
        try {
            if (kh == null || kh.getMaKH() == null) return false;
            kh.setTrangThai(true);

            Request req = new Request("THEM_KHACH_HANG", kh);
            Response res = socket.sendRequest(req);
            return (boolean) res.getData();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean capNhatKhachHang(KhachHang kh) {
        try {
            Request req = new Request("CAP_NHAT_KHACH_HANG", kh);
            Response res = socket.sendRequest(req);
            return (boolean) res.getData();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean xoaKhachHang(String maKH) {
        try {
            Request req = new Request("XOA_KHACH_HANG", maKH);
            Response res = socket.sendRequest(req);
            return (boolean) res.getData();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean khoiPhucKhachHang(String maKH) {
        try {
            Request req = new Request("KHOI_PHUC_KHACH_HANG", maKH);
            Response res = socket.sendRequest(req);
            return (boolean) res.getData();
        } catch (Exception e) {
            return false;
        }
    }

    public List<KhachHang> layListKHThongKe(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null) throw new IllegalArgumentException("Ngày không được để trống.");
        if (ngayBD.isAfter(ngayKT)) throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");

        try {
            Request req = new Request("LAY_LIST_KHACHHANG_THONG_KE", new Object[]{ngayBD, ngayKT});
            Response res = socket.sendRequest(req);
            if (res != null && "SUCCESS".equals(res.getStatus())) {
                return (List<KhachHang>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Map<String, Integer> layTongDonHangTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null || ngayBD.isAfter(ngayKT)) return new HashMap<>();

        try {
            Request req = new Request("LAY_TONG_DON_HANG_THEO_NGAY", new Object[]{ngayBD, ngayKT});
            Response res = socket.sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (Map<String, Integer>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public Map<String, Double> layTongTienTheoNgay(LocalDate ngayBD, LocalDate ngayKT) {
        if (ngayBD == null || ngayKT == null || ngayBD.isAfter(ngayKT)) return new HashMap<>();

        try {
            Request req = new Request("LAY_TONG_TIEN_THEO_NGAY", new Object[]{ngayBD, ngayKT});
            Response res = socket.sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (Map<String, Double>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
