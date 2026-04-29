package service;

import client.ClientSocketManager;
import entity.PhieuDoiTra;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.List;

public class PhieuDoiTraService {

    public boolean kiemTraHoaDonDaDoiTra(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) return false;
        try {
            Request req = new Request("KIEM_TRA_HOA_DON_DA_DOI_TRA", maHD.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<PhieuDoiTra> layListPDT() {
        try {
            Request req = new Request("LAY_LIST_PDT", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<PhieuDoiTra>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean themPDT(PhieuDoiTra pdt) {
        if (pdt == null || pdt.getMaPhieuDT() == null || pdt.getMaPhieuDT().isEmpty()) return false;
        try {
            Request req = new Request("THEM_PDT", pdt);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themChiTietPDT(Object chiTietPDT) {
        if (chiTietPDT == null) return false;
        try {
            Request req = new Request("THEM_CHI_TIET_PDT", chiTietPDT);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String layMaPDTMoiNhat() {
        try {
            Request req = new Request("LAY_MA_PDT_MOI_NHAT", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (String) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> layDanhSachThuocTheoPhieuDT(String maPhieuDT) {
        if (maPhieuDT == null || maPhieuDT.trim().isEmpty()) return null;
        try {
            Request req = new Request("LAY_DANH_SACH_THUOC_THEO_PHIEU_DT", maPhieuDT.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (List<Object[]>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public double tinhTongTienHoanTheoPhieuDT(String maPhieuDT) {
        if (maPhieuDT == null || maPhieuDT.trim().isEmpty()) return 0.0;
        try {
            Request req = new Request("TINH_TONG_TIEN_HOAN_THEO_PHIEU_DT", maPhieuDT.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (double) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public PhieuDoiTra timPhieuDoiTraTheoMa(String maPhieuDT) {
        if (maPhieuDT == null || maPhieuDT.trim().isEmpty()) return null;
        try {
            Request req = new Request("TIM_PHIEU_DOI_TRA_THEO_MA", maPhieuDT.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (PhieuDoiTra) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int tongSoLuongDaDoiTra(String maHD, String maThuoc, String maDVT) {
        if (maHD == null || maThuoc == null || maDVT == null) return 0;
        try {
            Request req = new Request("TONG_SO_LUONG_DA_DOI_TRA", new Object[]{maHD, maThuoc, maDVT});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (int) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}