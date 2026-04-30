package controller;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import entity.DonViTinh;
import entity.QuocGia;
import entity.Thue;
import entity.Thuoc;
import gui.NhapThuoc_GUI;
import service.ThuocService;
import utils.ToolCtrl;

public class NhapThuocController {

    private final NhapThuoc_GUI nhapThuoc_gui;
    private final ToolCtrl tool = new ToolCtrl();
    private final ThuocService thuocService = new ThuocService();

    public NhapThuocController(NhapThuoc_GUI ntGUI) {
        this.nhapThuoc_gui = ntGUI;
    }

    public void chonFileExcel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn tệp Excel để nhập thuốc");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "File Excel (.xlsx, .xls)", "xlsx", "xls"));

            if (fileChooser.showOpenDialog(nhapThuoc_gui) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                docFileExcel(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void docFileExcel(File file) {
        DefaultTableModel model = (DefaultTableModel) nhapThuoc_gui.tblNhapThuoc.getModel();
        model.setRowCount(0);

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            boolean isFirstRow = true;
            int stt = 1;

            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if(isFirstRow) { isFirstRow = false; continue; }

                Object[] rowData = new Object[12];
                rowData[0] = stt++;

                for(int i = 1; i < 12; i++) {
                    Cell cell = row.getCell(i);
                    if(cell == null) {
                        rowData[i] = "";
                    } else {
                        if (i == 6 && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            Date date = cell.getDateCellValue();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            rowData[i] = sdf.format(date);
                        } else {
                            switch (cell.getCellType()) {
                                case STRING:
                                    rowData[i] = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if(DateUtil.isCellDateFormatted(cell)) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                        rowData[i] = sdf.format(cell.getDateCellValue());
                                    } else {
                                        double val = cell.getNumericCellValue();
                                        if (val == Math.floor(val)) {
                                            rowData[i] = (long) val;
                                        } else {
                                            rowData[i] = val;
                                        }
                                    }
                                    break;
                                case BOOLEAN:
                                    rowData[i] = cell.getBooleanCellValue();
                                    break;
                                default:
                                    rowData[i] = "";
                            }
                        }
                    }
                }
                model.addRow(rowData);
            }
            tool.hienThiThongBao("Thông báo", "Đọc file Excel thành công", true);

        } catch (Exception e) {
            e.printStackTrace();
            tool.hienThiThongBao("Lỗi", "Không thể đọc file Excel: " + e.getMessage(), false);
        }
    }

    // Đã bọc Thread chống đơ UI vì vòng for chứa quá nhiều call mạng
    public void luuDataTuTable() {
        DefaultTableModel model = (DefaultTableModel) nhapThuoc_gui.tblNhapThuoc.getModel();
        int rowCount = model.getRowCount();

        if(rowCount == 0) {
            tool.hienThiThongBao("Lỗi", "Không có dữ liệu trên bảng để lưu", false);
            return;
        }

        // Tách ra một list tạm để copy data từ model (tránh lỗi xung đột thread UI)
        List<Object[]> tableData = new ArrayList<>();
        for(int i = 0; i < rowCount; i++) {
            Object[] row = new Object[model.getColumnCount()];
            for(int j = 0; j < model.getColumnCount(); j++) {
                row[j] = model.getValueAt(i, j);
            }
            tableData.add(row);
        }

        new Thread(() -> {
            try {
                List<Thuoc> listThuoc = new ArrayList<>();
                for(Object[] row : tableData) {
                    String maThuoc = row[1].toString();
                    String tenThuoc = row[2].toString();
                    String dangThuoc = row[3].toString();
                    String tenDvt = row[4].toString();
                    String tenQuocGia = row[5].toString();
                    LocalDate hanDung = tool.convertExcelDate(row[6]);
                    int soLuong = (int) Double.parseDouble(row[7].toString());
                    double donGia = Double.parseDouble(row[8].toString());
                    double tyLeThueVal = Double.parseDouble(row[9].toString());
                    String loaiThue = row[10].toString();

                    Thue thueObj = new Thue();
                    thueObj.setLoaiThue(loaiThue);
                    thueObj.setTyLeThue(tyLeThueVal);
                    String maThue = thuocService.layHoacTaoThue(thueObj);
                    if (maThue == null) {
                        throw new RuntimeException("Không tạo được mã thuế cho: " + loaiThue);
                    }
                    thueObj.setMaThue(maThue);

                    DonViTinh dvtObj = new DonViTinh();
                    dvtObj.setTenDVT(tenDvt);
                    String maDVT = thuocService.layHoacTaoDVT(dvtObj);
                    dvtObj.setMaDVT(maDVT);

                    String maQG = thuocService.layMaQuocGiaTheoTen(tenQuocGia);
                    QuocGia qg = thuocService.layQuocGiaTheoMa(maQG);

                    Thuoc thuoc = new Thuoc();
                    thuoc.setMaThuoc(maThuoc);
                    thuoc.setTenThuoc(tenThuoc);
                    thuoc.setDangThuoc(dangThuoc);
                    thuoc.setDonViTinh(dvtObj);
                    thuoc.setQuocGia(qg);
                    thuoc.setHanSuDung(hanDung);
                    thuoc.setSoLuong(soLuong);
                    thuoc.setGiaBan(donGia);
                    thuoc.setThue(thueObj);
                    thuoc.setTrangThai(true);

                    listThuoc.add(thuoc);
                }

                String maPNT = tool.taoKhoaChinh("PNT");
                String maNCC = "TTNCC1";
                String maNV = "TTNV1";
                LocalDate ngayNhap = LocalDate.now();

                boolean res = thuocService.luuData(maPNT, maNCC, maNV, ngayNhap, listThuoc);

                SwingUtilities.invokeLater(() -> {
                    if(res) {
                        tool.hienThiThongBao("Thông báo", "Lưu dữ liệu thành công", true);
                        lamMoiBang();
                    } else {
                        tool.hienThiThongBao("Lỗi", "Lưu dữ liệu thất bại (Lỗi hệ thống)", false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> tool.hienThiThongBao("Lỗi", "Dữ liệu không hợp lệ: " + e.getMessage(), false));
            }
        }).start();
    }

    public void lamMoiBang() {
        DefaultTableModel model = (DefaultTableModel) nhapThuoc_gui.tblNhapThuoc.getModel();
        model.setRowCount(0);
    }
}