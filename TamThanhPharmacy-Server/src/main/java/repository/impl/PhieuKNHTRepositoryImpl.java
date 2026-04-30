package repository.impl;

import entity.PhieuKhieuNaiHoTroKH;
import jakarta.persistence.EntityManager;
import repository.GenericJpa;
import repository.intf.PhieuKNHTRepository;

import java.util.List;

public class PhieuKNHTRepositoryImpl extends GenericJpa implements PhieuKNHTRepository {

    @Override
    public boolean themPhieu(PhieuKhieuNaiHoTroKH knht) {
        try {
            inTransaction(em -> {
                if (knht.getMaPhieu() == null || knht.getMaPhieu().trim().isEmpty()) {
                    knht.setMaPhieu(sinhMaPhieuTuDong(em));
                }

                if (knht.getKhachHang() != null) {
                    knht.setKhachHang(em.merge(knht.getKhachHang()));
                }

                if (knht.getNhanVien() != null) {
                    knht.setNhanVien(em.merge(knht.getNhanVien()));

                }
                em.persist(knht);
                em.flush();
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String sinhMaPhieuTuDong(EntityManager em) {
        String jpql = "SELECT MAX(CAST(SUBSTRING(p.maPhieu, 6, LENGTH(p.maPhieu)) AS integer)) FROM PhieuKhieuNaiHoTroKH p";
        try {
            Integer maxNumber = em.createQuery(jpql, Integer.class).getSingleResult();
            if (maxNumber == null) {
                return "TTPKN1";
            }
            return "TTPKN" + (maxNumber + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "TTPKN" + System.currentTimeMillis();
        }
    }

    @Override
    public boolean capNhatPhieu(PhieuKhieuNaiHoTroKH knht) {
        try {
            inTransaction(em -> {
                em.merge(knht);
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean doiTrangThaiPhieu(String maPhieu, String trangThaiMoi) {
        try {
            inTransaction(em -> {
                PhieuKhieuNaiHoTroKH phieu = em.find(PhieuKhieuNaiHoTroKH.class, maPhieu);
                if (phieu != null) {
                    phieu.setTrangThai(trangThaiMoi);
                    em.merge(phieu);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<PhieuKhieuNaiHoTroKH> layTatCaPhieu() {
        return doInTransaction(em -> {
            String jpql = "SELECT p FROM PhieuKhieuNaiHoTroKH p JOIN FETCH p.nhanVien JOIN FETCH p.khachHang";
            return em.createQuery(jpql, PhieuKhieuNaiHoTroKH.class).getResultList();
        });
    }
}