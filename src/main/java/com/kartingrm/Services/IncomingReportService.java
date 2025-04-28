package com.kartingrm.Services;

import com.kartingrm.Entities.BookingEntity;
import com.kartingrm.Entities.IncomingReportEntity;
import com.kartingrm.Repositories.IncomingReportRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service

public class IncomingReportService {
    @Autowired
    private IncomingReportRepository incomeReportRepository;
    @Autowired
    private BookingService bookingService;

    public IncomingReportEntity getIncomingReportById(Long id) {
        return incomeReportRepository.findById(id).get();
    }
    public List<IncomingReportEntity> getAllIncomingReports() {
        return incomeReportRepository.findAll();
    }

    public IncomingReportEntity generateIncomeReportByLaps(LocalDate startDate, LocalDate endDate) throws IOException {
        List<BookingEntity> allBookings = bookingService.getBookingsBetweenDates(startDate, endDate);

        Map<Integer, Map<Integer, List<BookingEntity>>> reportData = new TreeMap<>();
        Map<Integer, Double> reportData10 = new TreeMap<>();
        Map<Integer, Double> reportData15 = new TreeMap<>();
        Map<Integer, Double> reportData20 = new TreeMap<>();

        for (BookingEntity booking : allBookings) {
            int month = booking.getBookingDate().getMonthValue();
            int laps = booking.getNumberLap();

            reportData
                    .computeIfAbsent(month, m -> new TreeMap<>())
                    .computeIfAbsent(laps, l -> new ArrayList<>())
                    .add(booking);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Ingresos por Vueltas");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Inicio");
        headerRow.createCell(1).setCellValue(startDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " " + startDate.getYear());

        Row headerRow2 = sheet.createRow(1);
        headerRow2.createCell(0).setCellValue("Fin");
        headerRow2.createCell(1).setCellValue(endDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " " + endDate.getYear());

        sheet.createRow(2);

        Row maxLap = sheet.createRow(3);
        maxLap.createCell(0).setCellValue("Numero de vueltas o tiempo maximo permitido");
        // Inicializar fecha para iterar
        LocalDate current = startDate.withDayOfMonth(1); // Asegura comenzar desde el inicio del mes
        int cellIndex = 1;

        while (!current.isAfter(endDate)) {
            String mes = current.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            mes = mes.substring(0, 1).toUpperCase() + mes.substring(1); // Capitalizar primera letra
            String label = mes;
            maxLap.createCell(cellIndex++).setCellValue(label);
            current = current.plusMonths(1);
        }
        maxLap.createCell(cellIndex).setCellValue("TOTAL");

        //Obtiene los meses que hay entre el inicio y el final
        for (int month = startDate.getMonthValue(); month <= endDate.getMonthValue(); month++) {
            int year = startDate.getYear();

            // Verificar si el mes está dentro del rango de las fechas de inicio y fin
            if (month == endDate.getMonthValue()) {
                year = endDate.getYear();
            }

            List<BookingEntity> bookings10 = bookingService.getBookingsByLapsAndDate(10, month, year);
            List<BookingEntity> bookings15 = bookingService.getBookingsByLapsAndDate(15, month, year);
            List<BookingEntity> bookings20 = bookingService.getBookingsByLapsAndDate(20, month, year);

            // Sumar los ingresos para las reservas de 10, 15 y 20 vueltas en el mes
            double totalIncome10 = 0.0;
            for (BookingEntity booking : bookings10) {
                totalIncome10 += booking.getFinalPrice();
            }
            reportData10.put(month, totalIncome10);

            double totalIncome15 = 0.0;
            for (BookingEntity booking : bookings15) {
                totalIncome15 += booking.getFinalPrice();
            }
            reportData15.put(month, totalIncome15);

            double totalIncome20 = 0.0;
            for (BookingEntity booking : bookings20) {
                totalIncome20 += booking.getFinalPrice();
            }
            reportData20.put(month, totalIncome20);
        }

        // Sumar los ingresos para las reservas de 10, 15 y 20 vueltas en el mes
        double totalIncome10 = 0.0;
        int cellIndex10 = 1;

        Row tenLaps = sheet.createRow(4);
        tenLaps.createCell(0).setCellValue("10 vueltas o max 10min");

        // Inicializar la fecha para iterar
        LocalDate current10 = startDate.withDayOfMonth(1); // Comienza desde el inicio del mes
        double totalIncome10LapsAll = 0.0;
        // Iterar mes a mes desde startDate hasta endDate
        while (!current10.isAfter(endDate)) {

            // Obtener las reservas de 10 vueltas para el mes actual
            List<BookingEntity> bookings10Laps = bookingService.getBookingsByLapsAndDate(
                    10, current10.getMonthValue(), current10.getYear());

            // Acumular los ingresos de las reservas de 10 vueltas en este mes
            double monthlyIncome = 0.0;
            for (BookingEntity booking : bookings10Laps) {
                monthlyIncome += booking.getFinalPrice(); // Calcular el ingreso para esta reserva
            }

            // Escribir el ingreso para este mes
            tenLaps.createCell(cellIndex10++).setCellValue(monthlyIncome);

            // Acumular el total de ingresos de todos los meses
            totalIncome10LapsAll += monthlyIncome;

            // Avanzar al siguiente mes
            current10 = current10.plusMonths(1);
        }

        // Escribir la suma total de los ingresos de 10 vueltas
        tenLaps.createCell(cellIndex10).setCellValue(totalIncome10LapsAll);


        double totalIncome15 = 0.0;
        int cellIndex15 = 1;

        Row fifteenLaps = sheet.createRow(5);
        fifteenLaps.createCell(0).setCellValue("15 vueltas o max 10min");

        // Inicializar la fecha para iterar
        LocalDate current15 = startDate.withDayOfMonth(1); // Comienza desde el inicio del mes
        double totalIncome15LapsAll = 0.0;
        // Iterar mes a mes desde startDate hasta endDate
        while (!current15.isAfter(endDate)) {

            // Obtener las reservas de 10 vueltas para el mes actual
            List<BookingEntity> bookings15Laps = bookingService.getBookingsByLapsAndDate(
                    15, current15.getMonthValue(), current15.getYear());

            // Acumular los ingresos de las reservas de 10 vueltas en este mes
            double monthlyIncome = 0.0;
            for (BookingEntity booking : bookings15Laps) {
                monthlyIncome += booking.getFinalPrice(); // Calcular el ingreso para esta reserva
            }

            // Escribir el ingreso para este mes
            fifteenLaps.createCell(cellIndex15++).setCellValue(monthlyIncome);

            // Acumular el total de ingresos de todos los meses
            totalIncome15LapsAll += monthlyIncome;

            // Avanzar al siguiente mes
            current15 = current15.plusMonths(1);
        }

        // Escribir la suma total de los ingresos de 10 vueltas
        fifteenLaps.createCell(cellIndex15).setCellValue(totalIncome15LapsAll);

        double totalIncome20 = 0.0;
        int cellIndex20 = 1;

        Row twentyLaps = sheet.createRow(6);
        twentyLaps.createCell(0).setCellValue("20 vueltas o max 10min");

        LocalDate current20 = startDate.withDayOfMonth(1);
        double totalIncome20LapsAll = 0.0;

        while (!current20.isAfter(endDate)) {
            List<BookingEntity> bookings20Laps = bookingService.getBookingsByLapsAndDate(
                    20, current20.getMonthValue(), current20.getYear());

            double monthlyIncome = 0.0;
            for (BookingEntity booking : bookings20Laps) {
                monthlyIncome += booking.getFinalPrice();
            }

            twentyLaps.createCell(cellIndex20++).setCellValue(monthlyIncome);
            totalIncome20LapsAll += monthlyIncome;
            current20 = current20.plusMonths(1);
        }
        twentyLaps.createCell(cellIndex20).setCellValue(totalIncome20LapsAll);

        Row totalC = sheet.createRow(7);
        totalC.createCell(0).setCellValue("TOTAL");


        // Crear fila de total general por mes (suma vertical por columna)
        Row totalRow = sheet.createRow(7);
        totalRow.createCell(0).setCellValue("TOTAL");

        int numMeses = Period.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1)).getMonths() + 1;

        for (int col = 1; col <= numMeses + 1; col++) {
            double totalMes = 0.0;

            for (int fila = 4; fila <= 7; fila++) { // filas de 10, 15 y 20 vueltas
                Cell cell = sheet.getRow(fila).getCell(col);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    totalMes += cell.getNumericCellValue();
                }
            }
            totalRow.createCell(col).setCellValue(totalMes);
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        byte[] excelContent = baos.toByteArray();

        // Guardar reporte en la base de datos
        IncomingReportEntity report = new IncomingReportEntity();
        report.setReportType("Vueltas");
        report.setMonthYear(startDate.getYear() + "-" + startDate.getMonthValue() + " a " + endDate.getYear() + "-" + endDate.getMonthValue());
        report.setFileName("Reporte_Ingresos_Vueltas.xlsx");
        report.setTotalIncome(totalIncome10LapsAll + totalIncome15LapsAll + totalIncome20LapsAll);
        report.setFileContent(excelContent);
        incomeReportRepository.save(report);
        return report;
    }


    public IncomingReportEntity generateIncomeReportByGroupSize(LocalDate startDate, LocalDate endDate) throws IOException {
        List<BookingEntity> allBookings = bookingService.getBookingsBetweenDates(startDate, endDate);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte de Ingresos por Personas");

        // Fila de inicio y fin
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Inicio");
        headerRow.createCell(1).setCellValue(startDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " " + startDate.getYear());

        Row headerRow2 = sheet.createRow(1);
        headerRow2.createCell(0).setCellValue("Fin");
        headerRow2.createCell(1).setCellValue(endDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")) + " " + endDate.getYear());

        sheet.createRow(2);

        Row titleRow = sheet.createRow(3);
        titleRow.createCell(0).setCellValue("Numero de personas");

        // Escribir los nombres de los meses
        LocalDate current = startDate.withDayOfMonth(1);
        int cellIndex = 1;
        while (!current.isAfter(endDate)) {
            String mes = current.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
            titleRow.createCell(cellIndex++).setCellValue(mes);
            current = current.plusMonths(1);
        }
        titleRow.createCell(cellIndex).setCellValue("TOTAL");

        // Mapas para acumular ingresos por grupo y mes
        Map<String, Map<YearMonth, Double>> ingresosPorGrupo = new LinkedHashMap<>();
        String[] grupos = {"1-2 personas", "3-5 personas", "6-10 personas", "11-15 personas"};

        for (String grupo : grupos) {
            ingresosPorGrupo.put(grupo, new TreeMap<>());
        }

        for (BookingEntity booking : allBookings) {
            int numPersonas = booking.getParticipants().size(); // o booking.getNumberPeople()
            YearMonth ym = YearMonth.from(booking.getBookingDate());

            String grupo;
            if (numPersonas <= 2) grupo = "1-2 personas";
            else if (numPersonas <= 5) grupo = "3-5 personas";
            else if (numPersonas <= 10) grupo = "6-10 personas";
            else grupo = "11-15 personas";

            ingresosPorGrupo
                    .computeIfAbsent(grupo, k -> new TreeMap<>())
                    .merge(ym, booking.getFinalPrice(), Double::sum);
        }

        int rowIdx = 4;
        Map<YearMonth, Double> totalPorMes = new TreeMap<>();
        double totalGeneral = 0;

        for (String grupo : grupos) {
            Row fila = sheet.createRow(rowIdx++);
            fila.createCell(0).setCellValue(grupo);
            double totalGrupo = 0;

            LocalDate fecha = startDate.withDayOfMonth(1);
            int colIdx = 1;

            while (!fecha.isAfter(endDate)) {
                YearMonth ym = YearMonth.from(fecha);
                double ingreso = ingresosPorGrupo.get(grupo).getOrDefault(ym, 0.0);

                fila.createCell(colIdx++).setCellValue(ingreso);
                totalGrupo += ingreso;
                totalPorMes.merge(ym, ingreso, Double::sum);
                fecha = fecha.plusMonths(1);
            }
            fila.createCell(colIdx).setCellValue(totalGrupo);
            totalGeneral += totalGrupo;
        }

        // Fila de totales por mes
        Row totalRow = sheet.createRow(rowIdx);
        totalRow.createCell(0).setCellValue("TOTAL");

        LocalDate fecha = startDate.withDayOfMonth(1);
        int col = 1;
        while (!fecha.isAfter(endDate)) {
            YearMonth ym = YearMonth.from(fecha);
            totalRow.createCell(col++).setCellValue(totalPorMes.getOrDefault(ym, 0.0));
            fecha = fecha.plusMonths(1);
        }
        totalRow.createCell(col).setCellValue(totalGeneral);

        // Guardar Excel y persistir reporte
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        IncomingReportEntity report = new IncomingReportEntity();
        report.setReportType("Grupo");
        report.setMonthYear(startDate.getYear() + "-" + startDate.getMonthValue() + " a " + endDate.getYear() + "-" + endDate.getMonthValue());
        report.setFileName("Reporte_Ingresos_Grupo.xlsx");
        report.setTotalIncome(totalGeneral);
        report.setFileContent(baos.toByteArray());
        incomeReportRepository.save(report);
        return report;
    }
}