package service;

import client.ClientSocketManager;
import entity.NhanVien;
import entity.TaiKhoan;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.List;

public class NhanVienService {

    public NhanVien timNhanVienTheoMa(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;
        try {
            Request req = new Request("TIM_NHAN_VIEN_THEO_MA", maNV.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (NhanVien) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<NhanVien> layListNhanVien() {
        try {
            Request req = new Request("LAY_LIST_NHAN_VIEN", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return new ArrayList<>((List<NhanVien>) res.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<NhanVien> layNhanVienDangLam() {
        try {
            Request req = new Request("LAY_NHAN_VIEN_DANG_LAM", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return new ArrayList<>((List<NhanVien>) res.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<NhanVien> layNhanVienNghiLam() {
        try {
            Request req = new Request("LAY_NHAN_VIEN_NGHI_LAM", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return new ArrayList<>((List<NhanVien>) res.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean xoaNhanVien(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return false;
        try {
            Request req = new Request("XOA_NHAN_VIEN", maNV.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean khoiPhucNhanVien(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return false;
        try {
            Request req = new Request("KHOI_PHUC_NHAN_VIEN", maNV.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        if (nv == null || nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) return false;
        try {
            Request req = new Request("CAP_NHAT_NHAN_VIEN", nv);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themNhanVien(NhanVien nv) {
        if (nv == null || nv.getMaNV() == null || nv.getMaNV().trim().isEmpty()) return false;
        try {
            Request req = new Request("THEM_NHAN_VIEN", nv);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themTaiKhoan(TaiKhoan tk) {
        if (tk == null || tk.getMaTK() == null || tk.getMaTK().trim().isEmpty()) return false;
        try {
            Request req = new Request("THEM_TAI_KHOAN", tk);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String layEmailNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return "";
        try {
            Request req = new Request("LAY_EMAIL_NV", maNV.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (String) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String layAnhNV(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;
        try {
            Request req = new Request("LAY_ANH_NV", maNV.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (String) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}