package service;

import entity.TaiKhoan;
import network.Request;
import network.Response;
import client.ClientSocketManager;

public class TaiKhoanService {

    public TaiKhoan kiemTraDangNhap(TaiKhoan tkParam) throws Exception {

        Request req = new Request("LOGIN", tkParam);

        Response res = ClientSocketManager.getInstance().sendRequest(req);

        if ("SUCCESS".equals(res.getStatus())) {
            return (TaiKhoan) res.getData();
        }
        return null;
    }
}