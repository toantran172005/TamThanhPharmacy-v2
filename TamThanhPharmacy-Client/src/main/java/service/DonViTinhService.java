package service;

import entity.DonViTinh;
import client.ClientSocketManager;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonViTinhService {

    public ArrayList<DonViTinh> layDanhSachTheoTrangThai(boolean trangThai) {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_LAY_DS_THEO_TRANG_THAI", trangThai));
            if ("SUCCESS".equals(res.getStatus())) {
                List<DonViTinh> list = (List<DonViTinh>) res.getData();
                if (list == null) return new ArrayList<>();

                ArrayList<DonViTinh> arrayList = new ArrayList<>(list);
                // Chuyển logic ORDER BY từ SQL lên RAM
                arrayList.sort((d1, d2) -> {
                    try {
                        int num1 = Integer.parseInt(d1.getMaDVT().replace("TTDVT", ""));
                        int num2 = Integer.parseInt(d2.getMaDVT().replace("TTDVT", ""));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        return d1.getMaDVT().compareTo(d2.getMaDVT());
                    }
                });
                return arrayList;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public String timMaDVTTheoTen(String tenDVT) {
        if (tenDVT == null || tenDVT.trim().isEmpty()) return null;
        DonViTinh dvt = timTheoTen(tenDVT.trim()); // Tái sử dụng hàm timTheoTen bên dưới
        return dvt != null ? dvt.getMaDVT() : null;
    }

    public DonViTinh timTheoTen(String tenDVT) {
        if (tenDVT == null || tenDVT.trim().isEmpty()) return null;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_TIM_THEO_TEN", tenDVT.trim()));
            if ("SUCCESS".equals(res.getStatus())) return (DonViTinh) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean themDVT(DonViTinh dvt) {
        if (dvt == null || dvt.getTenDVT() == null || dvt.getTenDVT().trim().isEmpty()) return false;
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_THEM_DVT", dvt));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public ArrayList<DonViTinh> layListDVT() {
        try {
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_LAY_LIST", null));
            if ("SUCCESS".equals(res.getStatus())) {
                List<DonViTinh> list = (List<DonViTinh>) res.getData();
                if (list == null || list.isEmpty()) return new ArrayList<>();

                list.sort((d1, d2) -> {
                    try {
                        int num1 = Integer.parseInt(d1.getMaDVT().replace("TTDVT", ""));
                        int num2 = Integer.parseInt(d2.getMaDVT().replace("TTDVT", ""));
                        return Integer.compare(num1, num2);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                return new ArrayList<>(list);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>();
    }

    public List<DonViTinh> layDanhSachDaXoa() {
        return layDanhSachTheoTrangThai(false);
    }

    public boolean xoaDVT(String maDVT) {
        if (maDVT == null || maDVT.trim().isEmpty()) return false;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maDVT", maDVT.trim());
            data.put("trangThai", false);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_CAP_NHAT_TRANG_THAI", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean khoiPhucDVT(String maDVT) {
        if (maDVT == null || maDVT.trim().isEmpty()) return false;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("maDVT", maDVT.trim());
            data.put("trangThai", true);
            Response res = ClientSocketManager.getInstance().sendRequest(new Request("DVT_CAP_NHAT_TRANG_THAI", data));
            if ("SUCCESS".equals(res.getStatus())) return (boolean) res.getData();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}