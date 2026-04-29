package service;

import client.ClientSocketManager;
import entity.KhuyenMai;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.Request;
import network.Response;

public class KhuyenMaiService {

    public KhuyenMaiService() {
    }

    public KhuyenMai layKhuyenMaiTheoMa(String maKM) throws Exception {
        if (maKM == null || maKM.trim().isEmpty()) return null;
        Request req = new Request("LAY_KHUYEN_MAI_THEO_MA", maKM);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") ? (KhuyenMai) res.getData() : null;
    }

    public void capNhatTrangThaiHetHan() throws Exception {
        Request req = new Request("CAP_NHAT_TRANG_THAI_HET_HAN", null);
        ClientSocketManager.getInstance().sendRequest(req);
    }

    public boolean capNhatKhuyenMai(KhuyenMai km, List<String> danhSachMaThuoc) throws Exception {
        if (km == null || km.getMaKM() == null) return false;
        Map<String, Object> data = new HashMap<>();
        data.put("km", km);
        data.put("danhSachMaThuoc", danhSachMaThuoc);

        Request req = new Request("CAP_NHAT_KHUYEN_MAI", data);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") && (boolean) res.getData();
    }

    @SuppressWarnings("unchecked")
    public List<KhuyenMai> layDanhSachKM() throws Exception {
        Request req = new Request("LAY_DANH_SACH_KM", null);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") ? (List<KhuyenMai>) res.getData() : null;
    }

    @SuppressWarnings("unchecked")
    public List<KhuyenMai> layDanhSachDaXoa() throws Exception {
        Request req = new Request("LAY_DANH_SACH_DA_XOA", null);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") ? (List<KhuyenMai>) res.getData() : null;
    }

    public boolean xoaKM(String maKM) throws Exception {
        if (maKM == null || maKM.trim().isEmpty()) return false;
        Request req = new Request("XOA_KM", maKM);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") && (boolean) res.getData();
    }

    public boolean khoiPhucKM(String maKM) throws Exception {
        if (maKM == null || maKM.trim().isEmpty()) return false;
        Request req = new Request("KHOI_PHUC_KM", maKM);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") && (boolean) res.getData();
    }

    public boolean themThuocVaoChiTietKM(String maThuoc, String maKM) throws Exception {
        if (maThuoc == null || maKM == null) return false;
        Map<String, String> data = new HashMap<>();
        data.put("maThuoc", maThuoc);
        data.put("maKM", maKM);

        Request req = new Request("THEM_THUOC_VAO_CHI_TIET_KM", data);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") && (boolean) res.getData();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> layDanhSachChiTiet(String maKM) throws Exception {
        if (maKM == null || maKM.trim().isEmpty()) return null;
        Request req = new Request("LAY_DANH_SACH_CHI_TIET", maKM);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") ? (List<Object[]>) res.getData() : null;
    }

    public boolean themKM(KhuyenMai km, List<Object[]> listChon) throws Exception {
        if (km == null) return false;
        Map<String, Object> data = new HashMap<>();
        data.put("km", km);
        data.put("listChon", listChon);

        Request req = new Request("THEM_KM", data);
        Response res = ClientSocketManager.getInstance().sendRequest(req);
        return res.getStatus().equals("SUCCESS") && (boolean) res.getData();
    }
}