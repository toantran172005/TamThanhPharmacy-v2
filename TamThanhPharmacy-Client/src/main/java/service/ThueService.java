package service;

import entity.Thue;
import client.ClientSocketManager;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.List;

public class ThueService {

    // Đã xóa repo cục bộ
    public ThueService() {
    }

    public ArrayList<Thue> layListThue() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THUE_LAY_LIST", null));
            if ("SUCCESS".equals(res.getStatus())) {
                List<Thue> list = (List<Thue>) res.getData();
                return list != null ? new ArrayList<>(list) : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean themThue(Thue t) {
        if (t == null || t.getMaThue() == null || t.getMaThue().trim().isEmpty()) return false;
        t.setTrangThai(true);
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THUE_THEM", t));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatThue(Thue t) {
        if (t == null || t.getMaThue() == null || t.getMaThue().trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THUE_CAP_NHAT", t));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean xoaThue(String maThue) {
        if (maThue == null || maThue.trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THUE_XOA", maThue.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Thue> timKiem(String tuKhoa) {
        if (tuKhoa == null) tuKhoa = "";
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("THUE_TIM_KIEM", tuKhoa.trim()));
            if ("SUCCESS".equals(res.getStatus())) {
                List<Thue> list = (List<Thue>) res.getData();
                return list != null ? new ArrayList<>(list) : new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}