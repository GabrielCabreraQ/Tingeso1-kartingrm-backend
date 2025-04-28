package com.kartingrm.Services;

import com.kartingrm.Entities.BookingEntity;
import com.kartingrm.Entities.IncomingReportEntity;
import com.kartingrm.Repositories.IncomingReportRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IncomingReportServiceTest {

    @InjectMocks
    private IncomingReportService incomingReportService;

    @Mock
    private IncomingReportRepository incomingReportRepository;

    @Mock
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetIncomingReportById() {
        IncomingReportEntity report = new IncomingReportEntity(1L, "Vueltas", "2025-05", "Reporte.xlsx", 1000.0, new byte[0]);
        when(incomingReportRepository.findById(1L)).thenReturn(Optional.of(report));

        IncomingReportEntity result = incomingReportService.getIncomingReportById(1L);

        assertNotNull(result);
        assertEquals(report, result);
        verify(incomingReportRepository, times(1)).findById(1L);
    }

    @Test
    void testGenerateIncomeReportByLaps() throws IOException {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        BookingEntity booking1 = new BookingEntity();
        booking1.setBookingDate(LocalDate.of(2025, 1, 15));
        booking1.setNumberLap(10);
        booking1.setFinalPrice(15000.0);

        BookingEntity booking2 = new BookingEntity();
        booking2.setBookingDate(LocalDate.of(2025, 2, 20));
        booking2.setNumberLap(15);
        booking2.setFinalPrice(20000.0);

        when(bookingService.getBookingsBetweenDates(startDate, endDate)).thenReturn(List.of(booking1, booking2));

        incomingReportService.generateIncomeReportByLaps(startDate, endDate);

        verify(incomingReportRepository, times(1)).save(any(IncomingReportEntity.class));
    }

    @Test
    void testGenerateIncomeReportByGroupSize() throws IOException {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        BookingEntity booking1 = new BookingEntity();
        booking1.setBookingDate(LocalDate.of(2025, 1, 15));
        booking1.setParticipants(List.of());
        booking1.setFinalPrice(15000.0);

        BookingEntity booking2 = new BookingEntity();
        booking2.setBookingDate(LocalDate.of(2025, 2, 20));
        booking2.setParticipants(List.of());
        booking2.setFinalPrice(20000.0);

        when(bookingService.getBookingsBetweenDates(startDate, endDate)).thenReturn(List.of(booking1, booking2));

        incomingReportService.generateIncomeReportByGroupSize(startDate, endDate);

        verify(incomingReportRepository, times(1)).save(any(IncomingReportEntity.class));
    }

    @Test
    void testGeneratedExcelContent() throws IOException {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        BookingEntity booking = new BookingEntity();
        booking.setBookingDate(LocalDate.of(2025, 1, 15));
        booking.setNumberLap(10);
        booking.setFinalPrice(15000.0);

        when(bookingService.getBookingsBetweenDates(startDate, endDate)).thenReturn(List.of(booking));

        incomingReportService.generateIncomeReportByLaps(startDate, endDate);

        verify(incomingReportRepository, times(1)).save(argThat(report -> {
            try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(report.getFileContent()))) {
                assertNotNull(workbook.getSheet("Reporte de Ingresos por Vueltas"));
                return true;
            } catch (IOException e) {
                fail("Error al leer el contenido del Excel");
                return false;
            }
        }));
    }
    @Test
    void testGetAllIncomingReports() {
        // Datos de prueba
        IncomingReportEntity report1 = new IncomingReportEntity(1L, "Vueltas", "2025-05", "Reporte1.xlsx", 1000.0, new byte[0]);
        IncomingReportEntity report2 = new IncomingReportEntity(2L, "Grupo", "2025-06", "Reporte2.xlsx", 2000.0, new byte[0]);

        when(incomingReportRepository.findAll()).thenReturn(List.of(report1, report2));

        // Llamada al método
        List<IncomingReportEntity> result = incomingReportService.getAllIncomingReports();

        // Verificaciones
        assertNotNull(result, "La lista de reportes no debe ser nula.");
        assertEquals(2, result.size(), "Debe haber 2 reportes en la lista.");
        assertTrue(result.contains(report1), "La lista debe contener el reporte 1.");
        assertTrue(result.contains(report2), "La lista debe contener el reporte 2.");
        verify(incomingReportRepository, times(1)).findAll();
    }
}