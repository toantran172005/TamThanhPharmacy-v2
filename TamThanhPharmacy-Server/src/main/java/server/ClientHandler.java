package server;

import entity.*;
import network.Request;
import network.Response;
import repository.impl.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ClientHandler extends Thread {
    private Socket socket;
    private TaiKhoanRepositoryImpl tkRepo;
    private KeThuocRepositoryImpl keThuocRepo = new KeThuocRepositoryImpl();
    private ToolRepositoryImpl toolRepo = new ToolRepositoryImpl();
    private KhuyenMaiRepositoryImpl kmRepo = new KhuyenMaiRepositoryImpl();
    private NhanVienRepositoryImpl nvRepo = new NhanVienRepositoryImpl();
    private PhieuDatHangRepositoryImpl pdhRepo = new PhieuDatHangRepositoryImpl();
    private PhieuDoiTraRepositoryImpl pdtRepo = new PhieuDoiTraRepositoryImpl();
    private HoaDonRepositoryImpl hdRepo = new HoaDonRepositoryImpl();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.tkRepo = new TaiKhoanRepositoryImpl();
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                Request req = (Request) in.readObject();
                Response res = null;

                switch (req.getAction()) {
                    case "LOGIN":
                        TaiKhoan tkParam = (TaiKhoan) req.getData();
                        TaiKhoan tkThucTe = tkRepo.kiemTraDangNhap(tkParam);

                        if (tkThucTe != null) {
                            res = new Response("SUCCESS", tkThucTe, "Đăng nhập thành công");
                        } else {
                            res = new Response("ERROR", null, "Sai tài khoản hoặc mật khẩu");
                        }
                        break;

                    case "LAY_LIST_KE_THUOC":
                        res = new Response("SUCCESS", keThuocRepo.layListKeThuoc(), "Lấy ds kệ thuốc");
                        break;

                    case "LAY_TAT_CA_TEN_KE":
                        res = new Response("SUCCESS", keThuocRepo.layTatCaTenKe(), "Lấy tên kệ");
                        break;

                    case "LAY_LIST_THUOC_TRONG_KE":
                        String maKe = (String) req.getData();
                        res = new Response("SUCCESS", keThuocRepo.layListThuocTrongKe(maKe), "Lấy thuốc trong kệ");
                        break;

                    case "XOA_KE_THUOC":
                        boolean xoaOk = keThuocRepo.xoaKeThuoc((String) req.getData());
                        res = new Response(xoaOk ? "SUCCESS" : "ERROR", xoaOk, "Xóa kệ thuốc");
                        break;

                    case "KHOI_PHUC_KE_THUOC":
                        boolean kpOk = keThuocRepo.khoiPhucKeThuoc((String) req.getData());
                        res = new Response(kpOk ? "SUCCESS" : "ERROR", kpOk, "Khôi phục kệ thuốc");
                        break;

                    case "CAP_NHAT_KE_THUOC":
                        boolean capNhatOk = keThuocRepo.capNhatKeThuoc((KeThuoc) req.getData());
                        res = new Response(capNhatOk ? "SUCCESS" : "ERROR", capNhatOk, "Cập nhật kệ thuốc");
                        break;

                    case "THEM_KE_THUOC":
                        boolean themOk = keThuocRepo.themKeThuoc((KeThuoc) req.getData());
                        res = new Response(themOk ? "SUCCESS" : "ERROR", themOk, "Thêm kệ thuốc");
                        break;

                    case "TIM_KE_THUOC_THEO_TEN":
                        res = new Response("SUCCESS", keThuocRepo.timTheoTen((String) req.getData()), "Tìm theo tên");
                        break;

                    case "TAO_KHOA_CHINH":
                        String tenBangVietTat = (String) req.getData();
                        String maMoi = toolRepo.taoKhoaChinh(tenBangVietTat);

                        if (maMoi != null) {
                            res = new Response("SUCCESS", maMoi, "Tạo mã thành công");
                        } else {
                            res = new Response("ERROR", null, "Không tìm thấy bảng tương ứng");
                        }
                        break;

                    case "LAY_KHUYEN_MAI_THEO_MA": {
                        String maKM = (String) req.getData();
                        KhuyenMai km = kmRepo.layKhuyenMaiTheoMa(maKM);
                        if (km != null) {
                            res = new Response("SUCCESS", km, "Thành công");
                        } else {
                            res = new Response("ERROR", null, "Không tìm thấy khuyến mãi");
                        }
                        break;
                    }

                    case "CAP_NHAT_TRANG_THAI_HET_HAN": {
                        kmRepo.capNhatTrangThaiHetHan();
                        res = new Response("SUCCESS", null, "Cập nhật thành công");
                        break;
                    }

                    case "CAP_NHAT_KHUYEN_MAI": {
                        Map<String, Object> dataMap = (Map<String, Object>) req.getData();
                        KhuyenMai kmUpdate = (KhuyenMai) dataMap.get("km");
                        List<String> dsThuoc = (List<String>) dataMap.get("danhSachMaThuoc");
                        boolean kqUpdate = kmRepo.capNhatKhuyenMai(kmUpdate, dsThuoc);
                        res = new Response(kqUpdate ? "SUCCESS" : "ERROR", kqUpdate, "Kết quả cập nhật");
                        break;
                    }

                    case "LAY_DANH_SACH_KM": {
                        List<KhuyenMai> listKM = kmRepo.layDanhSachKM();
                        res = new Response("SUCCESS", listKM, "Thành công");
                        break;
                    }

                    case "LAY_DANH_SACH_DA_XOA": {
                        List<KhuyenMai> listDaXoa = kmRepo.layDanhSachDaXoa();
                        res = new Response("SUCCESS", listDaXoa, "Thành công");
                        break;
                    }

                    case "XOA_KM": {
                        String maXoa = (String) req.getData();
                        boolean kqXoa = kmRepo.xoaKM(maXoa);
                        res = new Response(kqXoa ? "SUCCESS" : "ERROR", kqXoa, "Kết quả xóa");
                        break;
                    }

                    case "KHOI_PHUC_KM": {
                        String maKhoiPhuc = (String) req.getData();
                        boolean kqKhoiPhuc = kmRepo.khoiPhucKM(maKhoiPhuc);
                        res = new Response(kqKhoiPhuc ? "SUCCESS" : "ERROR", kqKhoiPhuc, "Kết quả khôi phục");
                        break;
                    }

                    case "THEM_THUOC_VAO_CHI_TIET_KM": {
                        Map<String, String> dataMap = (Map<String, String>) req.getData();
                        String maThuoc = dataMap.get("maThuoc");
                        String maKMDetail = dataMap.get("maKM");
                        boolean kqThemChiTiet = kmRepo.themThuocVaoChiTietKM(maThuoc, maKMDetail);
                        res = new Response(kqThemChiTiet ? "SUCCESS" : "ERROR", kqThemChiTiet, "Kết quả thêm");
                        break;
                    }

                    case "LAY_DANH_SACH_CHI_TIET": {
                        String maKMQuery = (String) req.getData();
                        List<Object[]> listChiTiet = kmRepo.layDanhSachChiTiet(maKMQuery);
                        res = new Response("SUCCESS", listChiTiet, "Thành công");
                        break;
                    }

                    case "THEM_KM": {
                        Map<String, Object> dataMap = (Map<String, Object>) req.getData();
                        KhuyenMai kmNew = (KhuyenMai) dataMap.get("km");
                        List<Object[]> listChon = (List<Object[]>) dataMap.get("listChon");
                        boolean kqThem = kmRepo.themKM(kmNew, listChon);
                        res = new Response(kqThem ? "SUCCESS" : "ERROR", kqThem, "Kết quả thêm mới");
                        break;
                    }


                    // NHÂN VIÊN
                    case "TIM_NHAN_VIEN_THEO_MA":
                        String maNV = (String) req.getData();
                        NhanVien nv = nvRepo.timNhanVienTheoMa(maNV);
                        res = new Response("SUCCESS", nv, "Tìm nhân viên thành công");
                        break;

                    case "LAY_LIST_NHAN_VIEN":
                        List<NhanVien> listTatCa = nvRepo.layListNhanVien();
                        res = new Response("SUCCESS", listTatCa, "Lấy danh sách thành công");
                        break;

                    case "LAY_NHAN_VIEN_DANG_LAM":
                        List<NhanVien> listDangLam = nvRepo.layNhanVienDangLam();
                        res = new Response("SUCCESS", listDangLam, "Lấy danh sách đang làm thành công");
                        break;

                    case "LAY_NHAN_VIEN_NGHI_LAM":
                        List<NhanVien> listNghiLam = nvRepo.layNhanVienNghiLam();
                        res = new Response("SUCCESS", listNghiLam, "Lấy danh sách nghỉ làm thành công");
                        break;

                    case "XOA_NHAN_VIEN":
                        String maXoa = (String) req.getData();
                        boolean kqXoa = nvRepo.xoaNhanVien(maXoa);
                        res = new Response(kqXoa ? "SUCCESS" : "ERROR", kqXoa, kqXoa ? "Xóa thành công" : "Xóa thất bại");
                        break;

                    case "KHOI_PHUC_NHAN_VIEN":
                        String maKP = (String) req.getData();
                        boolean kqKP = nvRepo.khoiPhucNhanVien(maKP);
                        res = new Response(kqKP ? "SUCCESS" : "ERROR", kqKP, kqKP ? "Khôi phục thành công" : "Khôi phục thất bại");
                        break;

                    case "CAP_NHAT_NHAN_VIEN":
                        NhanVien nvCapNhat = (NhanVien) req.getData();
                        boolean kqCN = nvRepo.capNhatNhanVien(nvCapNhat);
                        res = new Response(kqCN ? "SUCCESS" : "ERROR", kqCN, kqCN ? "Cập nhật thành công" : "Cập nhật thất bại");
                        break;

                    case "THEM_NHAN_VIEN":
                        NhanVien nvThem = (NhanVien) req.getData();
                        boolean kqThem = nvRepo.themNhanVien(nvThem);
                        res = new Response(kqThem ? "SUCCESS" : "ERROR", kqThem, kqThem ? "Thêm thành công" : "Thêm thất bại");
                        break;

                    case "THEM_TAI_KHOAN":
                        TaiKhoan tkThem = (TaiKhoan) req.getData();
                        boolean kqThemTK = nvRepo.themTaiKhoan(tkThem);
                        res = new Response(kqThemTK ? "SUCCESS" : "ERROR", kqThemTK, kqThemTK ? "Thêm tài khoản thành công" : "Thêm tài khoản thất bại");
                        break;

                    case "LAY_EMAIL_NV":
                        String maEmail = (String) req.getData();
                        String email = nvRepo.layEmailNV(maEmail);
                        res = new Response("SUCCESS", email, "Lấy email thành công");
                        break;

                    case "LAY_ANH_NV":
                        String maAnh = (String) req.getData();
                        String anh = nvRepo.layAnhNV(maAnh);
                        res = new Response("SUCCESS", anh, "Lấy ảnh thành công");
                        break;


                    // PHIẾU ĐẶT HÀNG
                    case "LAY_LIST_PHIEU_DAT_HANG":
                        List<PhieuDatHang> listPDH = pdhRepo.layListPhieuDatHang();
                        res = new Response("SUCCESS", listPDH, "Lấy danh sách thành công");
                        break;

                    case "TIM_PHIEU_DAT_HANG_THEO_MA":
                        String maPDH = (String) req.getData();
                        PhieuDatHang pdh = pdhRepo.timTheoMa(maPDH);
                        res = new Response("SUCCESS", pdh, "Tìm thành công");
                        break;

                    case "THEM_PHIEU_DAT_HANG":
                        PhieuDatHang pdhThem = (PhieuDatHang) req.getData();
                        boolean kqThemPDH = pdhRepo.themPhieuDatHang(pdhThem);
                        res = new Response(kqThemPDH ? "SUCCESS" : "ERROR", kqThemPDH, kqThemPDH ? "Thêm thành công" : "Thêm thất bại");
                        break;

                    case "CAP_NHAT_TRANG_THAI_PHIEU_DAT_HANG":
                        PhieuDatHang pdhCN = (PhieuDatHang) req.getData();
                        boolean kqCNPDH = pdhRepo.capNhatTrangThai(pdhCN);
                        res = new Response(kqCNPDH ? "SUCCESS" : "ERROR", kqCNPDH, kqCNPDH ? "Cập nhật thành công" : "Cập nhật thất bại");
                        break;

                    case "CAP_NHAT_TRANG_THAI_PHIEU":
                        Object[] dataCN = (Object[]) req.getData();
                        int kqTrangThai = pdhRepo.capNhatTrangThaiPhieu((String) dataCN[0], (String) dataCN[1]);
                        res = new Response("SUCCESS", kqTrangThai, "Cập nhật trạng thái phiếu thành công");
                        break;

                    case "LAY_DANH_SACH_THUOC_THEO_PDH":
                        String maPDHThuoc = (String) req.getData();
                        List<Object[]> dsThuoc = pdhRepo.layDanhSachThuocTheoPDH(maPDHThuoc);
                        res = new Response("SUCCESS", dsThuoc, "Lấy danh sách thuốc thành công");
                        break;

                    case "TAO_PHIEU_DAT_HANG_VA_CHI_TIET":
                        @SuppressWarnings("unchecked")
                        Object[] dataTao = (Object[]) req.getData();
                        PhieuDatHang pdhTao = (PhieuDatHang) dataTao[0];
                        List<CTPhieuDatHang> dsCT = (List<CTPhieuDatHang>) dataTao[1];
                        int kqTao = pdhRepo.taoPhieuDatHangVaChiTiet(pdhTao, dsCT);
                        res = new Response("SUCCESS", kqTao, "Tạo phiếu và chi tiết thành công");
                        break;


                    //PHIẾU ĐỔI TRẢ
                    case "KIEM_TRA_HOA_DON_DA_DOI_TRA":
                        String maHDKiemTra = (String) req.getData();
                        boolean kqKT = pdtRepo.kiemTraHoaDonDaDoiTra(maHDKiemTra);
                        res = new Response("SUCCESS", kqKT, "Kiểm tra hóa đơn thành công");
                        break;

                    case "LAY_LIST_PDT":
                        List<PhieuDoiTra> listPDT = pdtRepo.layListPDT();
                        res = new Response("SUCCESS", listPDT, "Lấy danh sách thành công");
                        break;

                    case "THEM_PDT":
                        PhieuDoiTra pdtThem = (PhieuDoiTra) req.getData();
                        boolean kqThemPDT = pdtRepo.themPDT(pdtThem);
                        res = new Response(kqThemPDT ? "SUCCESS" : "ERROR", kqThemPDT, kqThemPDT ? "Thêm thành công" : "Thêm thất bại");
                        break;

                    case "THEM_CHI_TIET_PDT":
                        Object ctThem = req.getData();
                        boolean kqThemCT = pdtRepo.themChiTietPDT(ctThem);
                        res = new Response(kqThemCT ? "SUCCESS" : "ERROR", kqThemCT, kqThemCT ? "Thêm chi tiết thành công" : "Thêm chi tiết thất bại");
                        break;

                    case "LAY_MA_PDT_MOI_NHAT":
                        String maMoiNhat = pdtRepo.layMaPDTMoiNhat();
                        res = new Response("SUCCESS", maMoiNhat, "Lấy mã mới nhất thành công");
                        break;

                    case "LAY_DANH_SACH_THUOC_THEO_PHIEU_DT":
                        String maPhieuLayThuoc = (String) req.getData();
                        List<Object[]> dsThuocPDT = pdtRepo.layDanhSachThuocTheoPhieuDT(maPhieuLayThuoc);
                        res = new Response("SUCCESS", dsThuocPDT, "Lấy danh sách thuốc thành công");
                        break;

                    case "TINH_TONG_TIEN_HOAN_THEO_PHIEU_DT":
                        String maPDTTinhTien = (String) req.getData();
                        double tongTien = pdtRepo.tinhTongTienHoanTheoPhieuDT(maPDTTinhTien);
                        res = new Response("SUCCESS", tongTien, "Tính tổng tiền thành công");
                        break;

                    case "TIM_PHIEU_DOI_TRA_THEO_MA":
                        String maTimKiem = (String) req.getData();
                        PhieuDoiTra pdtTimDuoc = pdtRepo.timPhieuDoiTraTheoMa(maTimKiem);
                        res = new Response("SUCCESS", pdtTimDuoc, "Tìm phiếu thành công");
                        break;

                    case "TONG_SO_LUONG_DA_DOI_TRA":
                        Object[] dataSL = (Object[]) req.getData();
                        int tongSL = pdtRepo.tongSoLuongDaDoiTra((String) dataSL[0], (String) dataSL[1], (String) dataSL[2]);
                        res = new Response("SUCCESS", tongSL, "Lấy tổng số lượng thành công");
                        break;


                    //HOÁ ĐƠN
                    case "LAY_LIST_HOA_DON":
                        List<HoaDon> listHD = hdRepo.layListHoaDon();
                        res = new Response("SUCCESS", listHD, "Lấy danh sách thành công");
                        break;

                    case "LAY_LIST_HD_DA_XOA":
                        List<HoaDon> listHDDaXoa = hdRepo.layListHDDaXoa();
                        res = new Response("SUCCESS", listHDDaXoa, "Lấy danh sách đã xóa thành công");
                        break;

                    case "LAY_DOANH_THU_THEO_NGAY":
                        Object[] dataDT = (Object[]) req.getData();
                        Map<LocalDate, Double> mapDT = hdRepo.layDoanhThuTheoNgay((LocalDate) dataDT[0], (LocalDate) dataDT[1]);
                        res = new Response("SUCCESS", mapDT, "Lấy doanh thu thành công");
                        break;

                    case "LAY_LIST_KH_THONG_KE":
                        Object[] dataKHTK = (Object[]) req.getData();
                        List<KhachHang> listKHTK = hdRepo.layListKHThongKe((LocalDate) dataKHTK[0], (LocalDate) dataKHTK[1]);
                        res = new Response("SUCCESS", listKHTK, "Lấy danh sách KH thống kê thành công");
                        break;

                    case "TIM_HOA_DON_THEO_MA":
                        String maHDTim = (String) req.getData();
                        HoaDon hdTim = hdRepo.timHoaDonTheoMa(maHDTim);
                        res = new Response("SUCCESS", hdTim, "Tìm hóa đơn thành công");
                        break;

                    case "THEM_HOA_DON":
                        HoaDon hdThem = (HoaDon) req.getData();
                        boolean kqThemHD = hdRepo.themHoaDon(hdThem);
                        res = new Response(kqThemHD ? "SUCCESS" : "ERROR", kqThemHD, kqThemHD ? "Thêm thành công" : "Thêm thất bại");
                        break;

                    case "THEM_CHI_TIET_HOA_DON":
                        CTHoaDon cthdThem = (CTHoaDon) req.getData();
                        boolean kqThemCTHD = hdRepo.themChiTietHoaDon(cthdThem);
                        res = new Response(kqThemCTHD ? "SUCCESS" : "ERROR", kqThemCTHD, kqThemCTHD ? "Thêm CT thành công" : "Thêm CT thất bại");
                        break;

                    case "XOA_HD":
                        String maHDXoa = (String) req.getData();
                        boolean kqXoaHD = hdRepo.xoaHD(maHDXoa);
                        res = new Response(kqXoaHD ? "SUCCESS" : "ERROR", kqXoaHD, kqXoaHD ? "Xóa thành công" : "Xóa thất bại");
                        break;

                    case "KHOI_PHUC_HD":
                        String maHDKP = (String) req.getData();
                        boolean kqKPHD = hdRepo.khoiPhucHD(maHDKP);
                        res = new Response(kqKPHD ? "SUCCESS" : "ERROR", kqKPHD, kqKPHD ? "Khôi phục thành công" : "Khôi phục thất bại");
                        break;

                    case "LAY_TONG_DON_HANG":
                        String maKHTongDon = (String) req.getData();
                        int tongDon = hdRepo.layTongDonHang(maKHTongDon);
                        res = new Response("SUCCESS", tongDon, "Lấy tổng đơn thành công");
                        break;

                    case "LAY_TONG_TIEN":
                        String maKHTongTien = (String) req.getData();
                        double tongTienKH = hdRepo.layTongTien(maKHTongTien);
                        res = new Response("SUCCESS", tongTienKH, "Lấy tổng tiền thành công");
                        break;

                    case "LAY_TONG_TIEN_THEO_SAN_PHAM":
                        Object[] dataTTSP = (Object[]) req.getData();
                        double tongTienSP = hdRepo.layTongTienTheoSanPham((String) dataTTSP[0], (String) dataTTSP[1]);
                        res = new Response("SUCCESS", tongTienSP, "Lấy tổng tiền SP thành công");
                        break;

                    case "TINH_TONG_TIEN_THEO_HOA_DON":
                        String maHDTTHD = (String) req.getData();
                        double tongTienHD = hdRepo.tinhTongTienTheoHoaDon(maHDTTHD);
                        res = new Response("SUCCESS", tongTienHD, "Tính tổng tiền HD thành công");
                        break;

                    case "LAY_CHI_TIET_HOA_DON":
                        String maHDCT = (String) req.getData();
                        List<Object[]> listCTHD = hdRepo.layChiTietHoaDon(maHDCT);
                        res = new Response("SUCCESS", listCTHD, "Lấy chi tiết HD thành công");
                        break;

                    case "LAY_MA_HOA_DON_MOI_NHAT":
                        String maHDMoi = hdRepo.layMaHoaDonMoiNhat();
                        res = new Response("SUCCESS", maHDMoi, "Lấy mã HD mới nhất thành công");
                        break;





                }
                if (res != null) {
                    out.writeObject(res);
                    out.flush();
                }
            }
        } catch (Exception e) {
            System.out.println("Client ngắt kết nối: " + socket.getInetAddress());
        }
    }
}