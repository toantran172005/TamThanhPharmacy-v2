package repository.impl;

import repository.GenericJpa;
import java.util.HashMap;
import java.util.Map;

public class ToolRepositoryImpl extends GenericJpa {

    public String taoKhoaChinh(String tenBangVietTat) {
        Map<String, String> mapBang = new HashMap<>();
        mapBang.put("PNT", "PhieuNhapThuoc");
        mapBang.put("K", "Kho");
        mapBang.put("TK", "TaiKhoan");
        mapBang.put("PKN", "Phieu_KhieuNai_HoTroKH");
        mapBang.put("T", "Thue");
        mapBang.put("KH", "KhachHang");
        mapBang.put("NV", "NhanVien");
        mapBang.put("KT", "KeThuoc");
        mapBang.put("NCC", "NhaCungCap");
        mapBang.put("TH", "Thuoc");
        mapBang.put("HD", "HoaDon");
        mapBang.put("KM", "KhuyenMai");
        mapBang.put("PDT", "PhieuDoiTra");
        mapBang.put("PDTH", "PhieuDatThuoc");
        mapBang.put("DVT", "DonViTinh");
        mapBang.put("PDH", "PhieuDatHang");

        if (!mapBang.containsKey(tenBangVietTat)) {
            return null; // Không ném Exception qua mạng, trả về null để Client tự xử lý
        }

        String tenBang = mapBang.get(tenBangVietTat);
        String cotKhoa;

        switch (tenBangVietTat) {
            case "TH": cotKhoa = "maThuoc"; break;
            case "KT": cotKhoa = "maKe"; break;
            case "K": cotKhoa = "maKho"; break;
            case "PDT": cotKhoa = "maPhieuDT"; break;
            case "PDTH": cotKhoa = "maPDT"; break;
            case "PKN": cotKhoa = "maPhieu"; break;
            case "T": cotKhoa = "maThue"; break;
            default: cotKhoa = "ma" + tenBangVietTat; break;
        }

        String prefix = "TT" + tenBangVietTat;
        int cutIndex = prefix.length() + 1;

        return doInTransaction(em -> {
            try {
                String jpql = "SELECT MAX(CAST(SUBSTRING(e." + cotKhoa + ", " + cutIndex + ") AS int)) FROM " + tenBang + " e";
                Integer maxId = em.createQuery(jpql, Integer.class).getSingleResult();
                int nextId = (maxId != null ? maxId : 0) + 1;
                return prefix + nextId;
            } catch (Exception e) {
                e.printStackTrace();
                return prefix + "1";
            }
        });
    }
}