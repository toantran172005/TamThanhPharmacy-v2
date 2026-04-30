package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.data.category.DefaultCategoryDataset;

import entity.KhachHang;
import gui.ThongKeKhachHang_GUI;
import service.KhachHangService;
import utils.ToolCtrl;

public class ThongKeKhachHangController {

    public ThongKeKhachHang_GUI thongKekhGUI;
    public ArrayList<KhachHang> listKHTK;

    public KhachHangService khService = new KhachHangService();
    public ToolCtrl tool = new ToolCtrl();

    public Map<String, Integer> tongDonHangCache;
    public Map<String, Double> tongTienCache;

    public ThongKeKhachHangController(ThongKeKhachHang_GUI thongKekhGUI) {
        super();
        this.thongKekhGUI = thongKekhGUI;
    }

    public void xuatFileExcel() {
        try {
            List<KhachHang> list = this.listKHTK;
            if (list == null || list.isEmpty()) {
                tool.hienThiThongBao("Lỗi", "Không thể xuất file vì dữ liệu rỗng!", false);
                return;
            }

            JFileChooser chooser = new JFileChooser(new File("D:/"));
            chooser.setDialogTitle("Lưu file Excel");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setSelectedFile(new File("ThongKeKhachHang.xlsx"));
            chooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));

            int option = chooser.showSaveDialog(null);
            if (option != JFileChooser.APPROVE_OPTION)
                return;

            File file = chooser.getSelectedFile().getAbsoluteFile();
            if (!file.getName().toLowerCase().endsWith(".xlsx")) {
                file = new File(file.getParentFile(), file.getName() + ".xlsx");
            }

            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Thống kê khách hàng");

            CellStyle bold = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            bold.setFont(font);

            Row r0 = sheet.createRow(0);
            String[] headerTongQuan = { "Tổng khách hàng", "Tổng đơn", "Tổng chi tiêu", "Chi tiêu TB" };
            for (int i = 0; i < headerTongQuan.length; i++) {
                Cell cell = r0.createCell(i);
                cell.setCellValue(headerTongQuan[i]);
                cell.setCellStyle(bold);
            }

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(thongKekhGUI.lblTongKH.getText());
            r1.createCell(1).setCellValue(thongKekhGUI.lblTongSLM.getText());
            r1.createCell(2).setCellValue(thongKekhGUI.lblTongCT.getText());
            r1.createCell(3).setCellValue(thongKekhGUI.lblChiTieuTB.getText());

            Row header = sheet.createRow(3);
            String[] cols = { "STT", "Mã KH", "Tên KH", "SĐT", "Số lần mua", "Tổng chi tiêu (VND)" };
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(bold);
            }

            int rowNum = 4;
            for (int i = 0; i < list.size(); i++) {
                KhachHang kh = list.get(i);
                Row row = sheet.createRow(rowNum++);
                String ma = kh.getMaKH();
                int soDon = tongDonHangCache.getOrDefault(ma, 0);
                double tongTien = tongTienCache.getOrDefault(ma, 0.0);

                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(kh.getMaKH());
                row.createCell(2).setCellValue(kh.getTenKH());
                row.createCell(3).setCellValue(kh.getSdt());
                row.createCell(4).setCellValue(soDon);
                row.createCell(5).setCellValue(tongTien);
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            wb.close();

            tool.hienThiThongBao("Xuất file", "Xuất file excel thành công!", true);

            if (tool.hienThiXacNhan("Mở file", "Mở file excel vừa lưu?", null)) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            tool.hienThiThongBao("Lỗi", "Không thể xuất file excel!", false);
        }
    }

    public void lamMoi() {
        thongKekhGUI.model.setRowCount(0);
        if (thongKekhGUI.ngayBD != null) thongKekhGUI.ngayBD.setDate(null);
        if (thongKekhGUI.ngayKT != null) thongKekhGUI.ngayKT.setDate(null);
        if (thongKekhGUI.lblTongKH != null) thongKekhGUI.lblTongKH.setText("0");
        if (thongKekhGUI.lblTongSLM != null) thongKekhGUI.lblTongSLM.setText("0");
        if (thongKekhGUI.lblTongCT != null) thongKekhGUI.lblTongCT.setText("0");
        if (thongKekhGUI.lblChiTieuTB != null) thongKekhGUI.lblChiTieuTB.setText("0");

        if (thongKekhGUI.barChart != null) {
            thongKekhGUI.barChart.getCategoryPlot().setDataset(new DefaultCategoryDataset());
            if (thongKekhGUI.chartPanel != null) thongKekhGUI.chartPanel.repaint();
        }

        if (tongDonHangCache != null) tongDonHangCache.clear();
        if (tongTienCache != null) tongTienCache.clear();
        if (listKHTK != null) listKHTK.clear();
    }

    public void thongKeTopKhach(int topN) {
        if (listKHTK == null) return;
        List<KhachHang> rows = new ArrayList<>(listKHTK);
        rows.sort((a, b) -> Double.compare(tongTienCache.getOrDefault(b.getMaKH(), 0.0),
                tongTienCache.getOrDefault(a.getMaKH(), 0.0)));
        List<KhachHang> topList = rows.size() > topN ? rows.subList(0, topN) : rows;

        thongKekhGUI.model.setRowCount(0);
        int stt = 1;
        for (KhachHang kh : topList) {
            int soLanMua = tongDonHangCache.getOrDefault(kh.getMaKH(), 0);
            double tongTien = tongTienCache.getOrDefault(kh.getMaKH(), 0.0);
            Object[] row = { stt++, kh.getMaKH(), kh.getTenKH(), tool.chuyenSoDienThoai(kh.getSdt()), soLanMua,
                    tool.dinhDangVND(tongTien) };
            thongKekhGUI.model.addRow(row);
        }
    }

    public void thongKeKhachHang() {
        LocalDate ngayBD = tool.utilDateSangLocalDate(thongKekhGUI.ngayBD.getDate());
        LocalDate ngayKT = tool.utilDateSangLocalDate(thongKekhGUI.ngayKT.getDate());

        if (ngayBD == null || ngayKT == null) {
            tool.hienThiThongBao("Lỗi", "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc!", false);
            return;
        } else if (ngayBD.isAfter(ngayKT)) {
            tool.hienThiThongBao("Lỗi ngày", "Ngày bắt đầu phải trước ngày kết thúc!", false);
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                tongDonHangCache = khService.layTongDonHangTheoNgay(ngayBD, ngayKT);
                tongTienCache = khService.layTongTienTheoNgay(ngayBD, ngayKT);
                listKHTK = (ArrayList<KhachHang>) khService.layListKHThongKe(ngayBD, ngayKT);
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (listKHTK == null || listKHTK.isEmpty()) {
                        lamMoi();
                        tool.hienThiThongBao("Thông báo", "Không có dữ liệu trong khoảng thời gian này", true);
                        return;
                    }

                    listKHTK.sort((a, b) -> Double.compare(tongTienCache.getOrDefault(b.getMaKH(), 0.0),
                            tongTienCache.getOrDefault(a.getMaKH(), 0.0)));

                    int topN = 5;
                    List<KhachHang> topList = listKHTK.size() > topN ? listKHTK.subList(0, topN) : listKHTK;

                    thongKekhGUI.model.setRowCount(0);
                    int stt = 1;
                    double tongChiTieu = 0;
                    int tongSLM = 0;
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                    for (KhachHang kh : listKHTK) {
                        double tongTien = tongTienCache.getOrDefault(kh.getMaKH(), 0.0);
                        int soLanMua = tongDonHangCache.getOrDefault(kh.getMaKH(), 0);

                        Object[] row = { stt++, kh.getMaKH(), kh.getTenKH(), tool.chuyenSoDienThoai(kh.getSdt()), soLanMua,
                                tool.dinhDangVND(tongTien) };
                        thongKekhGUI.model.addRow(row);

                        tongChiTieu += tongTien;
                        tongSLM += soLanMua;
                    }

                    for (KhachHang kh : topList) {
                        double tongTien = tongTienCache.getOrDefault(kh.getMaKH(), 0.0);
                        dataset.addValue(tongTien, "Tổng chi tiêu", kh.getTenKH());
                    }
                    thongKekhGUI.barChart.getCategoryPlot().setDataset(dataset);
                    thongKekhGUI.chartPanel.repaint();

                    thongKekhGUI.lblTongKH.setText(String.valueOf(listKHTK.size()));
                    thongKekhGUI.lblTongCT.setText(tool.dinhDangVND(tongChiTieu));
                    thongKekhGUI.lblTongSLM.setText(String.valueOf(tongSLM));

                    double chiTieuTB = listKHTK.isEmpty() ? 0 : tongChiTieu / listKHTK.size();
                    thongKekhGUI.lblChiTieuTB.setText(tool.dinhDangVND(chiTieuTB));

                } catch (Exception e) {
                    e.printStackTrace();
                    tool.hienThiThongBao("Lỗi", "Có lỗi xảy ra khi xử lý dữ liệu thống kê!", false);
                }
            }
        };

        worker.execute();
    }

    public Map<String, Integer> getTongDonHangCache() {
        return tongDonHangCache;
    }

    public Map<String, Double> getTongTienCache() {
        return tongTienCache;
    }
}