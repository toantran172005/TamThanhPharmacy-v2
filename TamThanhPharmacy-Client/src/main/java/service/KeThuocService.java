package service;

import entity.KeThuoc;
import entity.Thuoc;
import network.Request;
import network.Response;
import client.ClientSocketManager;

import java.util.ArrayList;
import java.util.List;

public class KeThuocService {

    public List<KeThuoc> layListKeThuoc() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_KE_THUOC", null));
            if ("SUCCESS".equals(res.getStatus())) return (List<KeThuoc>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<String> layTatCaTenKe() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_TAT_CA_TEN_KE", null));
            if ("SUCCESS".equals(res.getStatus())) return (List<String>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<Thuoc> layListThuocTrongKe(String maKe) {
        if (maKe == null || maKe.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("LAY_LIST_THUOC_TRONG_KE", maKe));
            if ("SUCCESS".equals(res.getStatus())) return (List<Thuoc>) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public boolean xoaKeThuoc(String maKe) {
        if (maKe == null || maKe.trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("XOA_KE_THUOC", maKe));
            return (boolean) res.getData();
        } catch (Exception e) { return false; }
    }

    public boolean khoiPhucKeThuoc(String maKe) {
        if (maKe == null || maKe.trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("KHOI_PHUC_KE_THUOC", maKe));
            return (boolean) res.getData();
        } catch (Exception e) { return false; }
    }

    public boolean capNhatKeThuoc(KeThuoc kt) {
        if (kt == null || kt.getMaKe() == null || kt.getMaKe().trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("CAP_NHAT_KE_THUOC", kt));
            return (boolean) res.getData();
        } catch (Exception e) { return false; }
    }

    public boolean themKeThuoc(KeThuoc kt) {
        if (kt == null || kt.getMaKe() == null) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THEM_KE_THUOC", kt));
            return (boolean) res.getData();
        } catch (Exception e) { return false; }
    }

    public KeThuoc timTheoTen(String tenKe) {
        if (tenKe == null || tenKe.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("TIM_KE_THUOC_THEO_TEN", tenKe));
            if ("SUCCESS".equals(res.getStatus())) return (KeThuoc) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}