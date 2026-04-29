package service;

import client.ClientSocketManager;
import entity.*;
import network.Request;
import network.Response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatHangService {

    @SuppressWarnings("unchecked")
    public ArrayList<PhieuDatHang> layListPhieuDatHang() {
        try {
            Request req = new Request("LAY_LIST_PHIEU_DAT_HANG", null);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return new ArrayList<>((List<PhieuDatHang>) res.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public PhieuDatHang timTheoMa(String maPDH) {
        if (maPDH == null || maPDH.trim().isEmpty()) return null;
        try {
            Request req = new Request("TIM_PHIEU_DAT_HANG_THEO_MA", maPDH.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus())) {
                return (PhieuDatHang) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean themPhieuDatHang(PhieuDatHang pdh) {
        if (pdh == null || pdh.getMaPDH() == null || pdh.getMaPDH().trim().isEmpty()) return false;
        try {
            Request req = new Request("THEM_PHIEU_DAT_HANG", pdh);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatTrangThai(PhieuDatHang pdh, String trangThaiMoi) {
        if (pdh == null || trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) return false;
        try {
            pdh.setTrangThai(trangThaiMoi);
            Request req = new Request("CAP_NHAT_TRANG_THAI_PHIEU_DAT_HANG", pdh);
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            return "SUCCESS".equals(res.getStatus()) && (Boolean) res.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int capNhatTrangThaiPhieu(String maPDH, String trangThaiMoi) {
        if (maPDH == null || maPDH.trim().isEmpty() || trangThaiMoi == null || trangThaiMoi.trim().isEmpty()) return 2;
        try {
            Request req = new Request("CAP_NHAT_TRANG_THAI_PHIEU", new Object[]{maPDH.trim(), trangThaiMoi.trim()});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (int) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object[]> layDanhSachThuocTheoPDH(String maPDH) {
        if (maPDH == null || maPDH.trim().isEmpty()) return new ArrayList<>();

        List<Object[]> listDb = new ArrayList<>();
        try {
            Request req = new Request("LAY_DANH_SACH_THUOC_THEO_PDH", maPDH.trim());
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                listDb = (List<Object[]>) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Object[]> ketQua = new ArrayList<>();
        for (Object[] row : listDb) {
            int soLuong = (int) row[3];
            double donGia = (double) row[6];
            KhuyenMai km = (KhuyenMai) row[7];

            double thanhTien = soLuong * donGia;

            if (km != null && km.getTrangThai()) {
                String loaiKM = km.getLoaiKM();
                if ("Giảm giá".equalsIgnoreCase(loaiKM)) {
                    thanhTien = soLuong * donGia * (1 - km.getMucKM() / 100.0);
                } else if ("Mua tặng".equalsIgnoreCase(loaiKM)) {
                    int mua = km.getSoLuongMua();
                    int tang = km.getSoLuongTang();
                    int soSuatTang = soLuong / (mua + tang);
                    thanhTien = (soLuong - (soSuatTang * tang)) * donGia;
                }
            }

            Object[] newRow = {
                    row[0], row[1], row[2], soLuong, row[4], row[5], donGia, thanhTien
            };
            ketQua.add(newRow);
        }
        return ketQua;
    }

    public int taoPhieuDatHangVaChiTiet(String maPDH, String maKH, String maNV, LocalDate ngayDat, LocalDate ngayHen, String ghiChu, List<Object[]> dsChiTiet) {
        if (maPDH == null || dsChiTiet == null || dsChiTiet.isEmpty()) return -1;

        // Tạo Object PhieuDatHang ở Client
        PhieuDatHang pdh = new PhieuDatHang();
        pdh.setMaPDH(maPDH);
        pdh.setNgayDat(ngayDat);
        pdh.setNgayHen(ngayHen);
        pdh.setGhiChu(ghiChu);
        pdh.setTrangThai("Chờ hàng");
        pdh.setDiaChiHT("456 Nguyễn Huệ, TP.HCM");
        pdh.setTenHT("Hiệu Thuốc Tam Thanh");
        pdh.setHotline("+84-912345689");

        KhachHang kh = new KhachHang(); kh.setMaKH(maKH);
        pdh.setKhachHang(kh);

        NhanVien nv = new NhanVien(); nv.setMaNV(maNV);
        pdh.setNhanVien(nv);

        // Tạo List ChiTiet ở Client
        List<CTPhieuDatHang> listCT = new ArrayList<>();
        for (Object[] row : dsChiTiet) {
            CTPhieuDatHang ct = new CTPhieuDatHang();
            Thuoc thuoc = new Thuoc(); thuoc.setMaThuoc((String) row[0]);
            ct.setThuoc(thuoc);
            ct.setSoLuong((int) row[1]);
            DonViTinh dvt = new DonViTinh(); dvt.setMaDVT((String) row[2]);
            ct.setDonViTinh(dvt);
            ct.setDonGia((double) row[3]);
            listCT.add(ct);
        }

        // Đóng gói gửi cả 2 lên Server xử lý Transaction
        try {
            Request req = new Request("TAO_PHIEU_DAT_HANG_VA_CHI_TIET", new Object[]{pdh, listCT});
            Response res = ClientSocketManager.getInstance().sendRequest(req);
            if ("SUCCESS".equals(res.getStatus()) && res.getData() != null) {
                return (int) res.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}