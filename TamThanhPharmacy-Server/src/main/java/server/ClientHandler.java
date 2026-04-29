package server;

import entity.KeThuoc;
import entity.KhuyenMai;
import entity.TaiKhoan;
import network.Request;
import network.Response;
import repository.impl.KeThuocRepositoryImpl;
import repository.impl.KhuyenMaiRepositoryImpl;
import repository.impl.TaiKhoanRepositoryImpl;
import repository.impl.ToolRepositoryImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler extends Thread {
    private Socket socket;
    private TaiKhoanRepositoryImpl tkRepo;
    private KeThuocRepositoryImpl keThuocRepo = new KeThuocRepositoryImpl();
    private ToolRepositoryImpl toolRepo = new ToolRepositoryImpl();
    private KhuyenMaiRepositoryImpl kmRepo = new KhuyenMaiRepositoryImpl();

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