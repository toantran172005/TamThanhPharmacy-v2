package server;

import entity.KeThuoc;
import entity.TaiKhoan;
import network.Request;
import network.Response;
import repository.impl.KeThuocRepositoryImpl;
import repository.impl.TaiKhoanRepositoryImpl;
import repository.impl.ToolRepositoryImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private TaiKhoanRepositoryImpl tkRepo;
    private KeThuocRepositoryImpl keThuocRepo = new KeThuocRepositoryImpl();
    private ToolRepositoryImpl toolRepo = new ToolRepositoryImpl();

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