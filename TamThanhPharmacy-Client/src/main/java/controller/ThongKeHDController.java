package controller;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import com.toedter.calendar.JDateChooser;

import service.HoaDonService;
import entity.KhachHang;
import gui.ThongKeHoaDon_GUI;
import utils.ToolCtrl;

public class ThongKeHDController {

    public ThongKeHoaDon_GUI gui;
    public HoaDonService hdService = new HoaDonService();
    public ToolCtrl tool = new ToolCtrl();
    public JPanel mainCenter;

    public Map<String, Double> tongTienMap = new HashMap<>();
    public Map<String, Integer> tongDonMap = new HashMap<>();
    public ArrayList<KhachHang> listKH = new ArrayList<>();

    public ThongKeHDController(ThongKeHoaDon_GUI gui) {
        this.gui = gui;
        this.mainCenter = gui.getMainCenter();
        dangKySuKien();
    }

    public void dangKySuKien() {
        gui.getBtnThongKe().addActionListener(e -> thongKeKhachHang());
        gui.getCmbTopTK().addActionListener(e -> thongKeTop());
        gui.getBtnLamMoi().addActionListener(e -> lamMoi());
        gui.getBtnXuatExcel().addActionListener(e -> xuatFileExcel());
    }

    // ========== THỐNG KÊ KHÁCH HÀNG ==========
    public void thongKeKhachHang() {
        LocalDate ngayBD = layNgay(gui.getDpNgayBatDau());
        LocalDate ngayKT = layNgay(gui.getDpNgayKetThuc());

        if (ngayBD == null) {
            tool.hienThiThongBao("Ngày bắt đầu rỗng", "Vui lòng chọn ngày bắt đầu!", false);
            return;
        } else if (ngayKT == null) {
            tool.hienThiThongBao("Ngày kết thúc rỗng", "Vui lòng chọn ngày kết thúc!", false);
            return;
        } else if (ngayBD.isAfter(ngayKT)) {
            tool.hienThiThongBao("Lỗi ngày", "Ngày bắt đầu phải trước ngày kết thúc!", false);
            return;
        }

        String top = (String) gui.getCmbTopTK().getSelectedItem();

        // Chạy tiến trình Vẽ Biểu Đồ (Thread 1)
        veBieuDo(ngayBD, ngayKT);

        // Khóa nút bấm
        gui.getBtnThongKe().setEnabled(false);

        // Chạy tiến trình Lấy dữ liệu Bảng và Nhãn (Thread 2)
        new Thread(() -> {
            try {
                List<KhachHang> khList = hdService.layListKHThongKe(ngayBD, ngayKT);
                Map<String, Double> tempTienMap = new HashMap<>();
                Map<String, Integer> tempDonMap = new HashMap<>();

                for (KhachHang kh : khList) {
                    tempTienMap.put(kh.getMaKH(), hdService.layTongTien(kh.getMaKH()));
                    tempDonMap.put(kh.getMaKH(), hdService.layTongDonHang(kh.getMaKH()));
                }

                // Cập nhật lại UI sau khi đã tính toán xong
                SwingUtilities.invokeLater(() -> {
                    listKH = new ArrayList<>(khList);
                    tongTienMap = tempTienMap;
                    tongDonMap = tempDonMap;

                    setDataChoLabel();
                    setDataChoTable(top);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> gui.getBtnThongKe().setEnabled(true));
            }
        }).start();
    }

    public LocalDate layNgay(JDateChooser chooser) {
        if (chooser.getDate() == null) return null;
        return chooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setDataChoLabel() {
        int tongSoHoaDon = tongDonMap.values().stream().mapToInt(Integer::intValue).sum();
        double tongDoanhThu = tongTienMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double tbDT = tongSoHoaDon == 0 ? 0 : tongDoanhThu / tongSoHoaDon;

        gui.getLblTongHD().setText("Tổng hóa đơn: " + tongSoHoaDon);
        gui.getLblTongDoanhThu().setText("Tổng doanh thu: " + tool.dinhDangVND(tongDoanhThu));
        gui.getLblTBDT().setText("Doanh thu TB/Hóa đơn: " + tool.dinhDangVND(tbDT));
    }

    // ========== VẼ BIỂU ĐỒ DOANH THU ==========
    public void veBieuDo(LocalDate ngayBD, LocalDate ngayKT) {
        new Thread(() -> {
            Map<LocalDate, Double> dbData = hdService.layDoanhThuTheoNgay(ngayBD, ngayKT);

            // Việc dựng và vẽ Component phải trả về luồng EDT của Swing
            SwingUtilities.invokeLater(() -> {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                Map<LocalDate, Double> doanhThuTheoNgay = new LinkedHashMap<>();
                LocalDate current = ngayBD;
                while (!current.isAfter(ngayKT)) {
                    doanhThuTheoNgay.put(current, 0.0);
                    current = current.plusDays(1);
                }

                for (Map.Entry<LocalDate, Double> e : dbData.entrySet()) {
                    doanhThuTheoNgay.put(e.getKey(), e.getValue());
                }

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                for (Map.Entry<LocalDate, Double> e : doanhThuTheoNgay.entrySet()) {
                    dataset.addValue(e.getValue(), "Doanh thu", e.getKey().format(fmt));
                }

                JFreeChart chart = ChartFactory.createLineChart("Doanh thu theo ngày", "Ngày", "Doanh thu (VND)", dataset);

                org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
                org.jfree.chart.axis.CategoryAxis xAxis = plot.getDomainAxis();
                xAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.UP_45);
                xAxis.setMaximumCategoryLabelWidthRatio(1.0f);
                xAxis.setTickLabelsVisible(true);
                xAxis.setLowerMargin(0.01);
                xAxis.setUpperMargin(0.01);
                plot.getRangeAxis().setAutoRange(true);

                ChartPanel newChartPanel = new ChartPanel(chart);
                newChartPanel.setPreferredSize(new Dimension(900, 300));
                newChartPanel.setMaximumSize(new Dimension(900, 300));

                JPanel pnlBieuDo = gui.getPnlBieuDo();
                pnlBieuDo.removeAll();
                pnlBieuDo.add(newChartPanel, BorderLayout.CENTER);
                pnlBieuDo.revalidate();
                pnlBieuDo.repaint();
            });
        }).start();
    }

    public void thongKeTop() {
        String top = (String) gui.getCmbTopTK().getSelectedItem();
        setDataChoTable(top);
    }

    public void setDataChoTable(String top) {
        DefaultTableModel model = (DefaultTableModel) gui.getTblThongKeHD().getModel();
        model.setRowCount(0);

        if (listKH == null || listKH.isEmpty()) return;

        listKH.sort((kh1, kh2) -> Double.compare(
                tongTienMap.getOrDefault(kh2.getMaKH(), 0.0),
                tongTienMap.getOrDefault(kh1.getMaKH(), 0.0)));

        int topKH = 0;
        String value = top;
        if (value != null && !value.equals("Tất cả")) {
            topKH = Integer.parseInt(value);
        }

        java.util.List<KhachHang> tempList = topKH > 0 ? listKH.subList(0, Math.min(topKH, listKH.size())) : listKH;

        int stt = 1;
        for (KhachHang kh : tempList) {
            model.addRow(new Object[]{
                    stt++, kh.getTenKH(), tool.chuyenSoDienThoai(kh.getSdt()),
                    tongDonMap.getOrDefault(kh.getMaKH(), 0),
                    tool.dinhDangVND(tongTienMap.getOrDefault(kh.getMaKH(), 0.0))
            });
        }

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        javax.swing.JTable table = gui.getTblThongKeHD();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public void lamMoi() {
        tongTienMap.clear();
        tongDonMap.clear();
        listKH.clear();
        ((DefaultTableModel) gui.getTblThongKeHD().getModel()).setRowCount(0);
        gui.getLblTongDoanhThu().setText("Tổng doanh thu: 0 VND");
        gui.getLblTongHD().setText("Tổng hóa đơn: 0");
        gui.getLblTBDT().setText("Doanh thu TB/Hóa đơn: 0");
        gui.getDpNgayBatDau().setDate(null);
        gui.getDpNgayKetThuc().setDate(null);
        gui.getCmbTopTK().setSelectedIndex(0);
    }

    // ========== XUẤT EXCEL ==========
    public void xuatFileExcel() {
        DefaultTableModel model = (DefaultTableModel) gui.getTblThongKeHD().getModel();
        if (model.getRowCount() == 0) {
            tool.hienThiThongBao("Lỗi xuất Excel", "Không có dữ liệu để xuất!", false);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu file Excel");
        chooser.setSelectedFile(new File("ThongKeHoaDon.xlsx"));
        int userSelection = chooser.showSaveDialog(gui);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        // Trích xuất dữ liệu bảng sang biến tạm trước khi vứt vào Thread
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) headers.add(model.getColumnName(i));

        List<List<String>> data = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            List<String> rowData = new ArrayList<>();
            for (int c = 0; c < model.getColumnCount(); c++) {
                rowData.add(String.valueOf(model.getValueAt(r, c)));
            }
            data.add(rowData);
        }

        // Tạo Thread riêng biệt để xử lý File I/O
        new Thread(() -> {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Thống kê hoá đơn");
                CellStyle boldStyle = wb.createCellStyle();
                Font boldFont = wb.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers.get(i));
                    cell.setCellStyle(boldStyle);
                }

                for (int r = 0; r < data.size(); r++) {
                    Row row = sheet.createRow(r + 1);
                    for (int c = 0; c < headers.size(); c++) {
                        row.createCell(c).setCellValue(data.get(r).get(c));
                    }
                }

                for (int i = 0; i < headers.size(); i++) sheet.autoSizeColumn(i);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    wb.write(fos);
                }

                SwingUtilities.invokeLater(() -> {
                    tool.hienThiThongBao("Xuất Excel", "Xuất file Excel thành công!", true);
                    try { Desktop.getDesktop().open(file); } catch (Exception ignored) {}
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Xuất Excel", "Xuất file thất bại!", false));
            }
        }).start();
    }
}