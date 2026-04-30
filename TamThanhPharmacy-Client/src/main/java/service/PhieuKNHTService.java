package service;

import client.ClientSocketManager;
import entity.PhieuKhieuNaiHoTroKH;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.List;

public class PhieuKNHTService {
    public List<PhieuKhieuNaiHoTroKH> getAll() {
        try {
            Request req = new Request("LAY_TAT_CA_PHIEU_KNHT", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if (res != null && res.getStatus().equals("SUCCESS")) {
                return (List<PhieuKhieuNaiHoTroKH>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean themPhieu(PhieuKhieuNaiHoTroKH phieu) {
        if (phieu == null) return false;

        try {
            Request req = new Request("THEM_PHIEU_KNHT", phieu);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return res != null && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatPhieu(PhieuKhieuNaiHoTroKH phieu) {
        if (phieu == null || phieu.getMaPhieu().isEmpty()) return false;
        try {
            Request req = new Request("CAP_NHAT_PHIEU_KNHT", phieu);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return res != null && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThai(String maPhieu, String status) {
        if (maPhieu == null || maPhieu.isEmpty()) return false;
        try {
            Request req = new Request("DOI_TRANG_THAI_PHIEU_KNHT", new Object[]{maPhieu, status});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return res != null && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}