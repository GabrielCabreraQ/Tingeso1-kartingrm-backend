package com.kartingrm.Services;

import com.itextpdf.text.DocumentException;
import com.kartingrm.Entities.BookingEntity;
import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Entities.PricingEntity;
import com.kartingrm.Entities.SpecialDaysEntity;
import com.kartingrm.Repositories.BookingRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private PricingService pricingService;

    @Mock
    private SpecialDayService specialDayService;

    @Mock
    private JavaMailSender javaMailSender;

    private BookingEntity booking;
    private ClientEntity cliente1, cliente2;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks

        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .discount1To2People(0.0)
                .discount3To5People(0.1)
                .discount6To10People(0.2)
                .discount11To15People(0.3)
                .discountVeryFrequent(0.3)
                .discountFrequent(0.2)
                .discountRegular(0.1)
                .discountNonFrequent(0.0)
                .weekendRise(0.15)
                .holydayRise(0.25)
                .weekendDiscount(0.2)
                .holidayDiscount(0.25)
                .birthdayDiscount(0.5)
                .iva(0.19)
                .build();

        cliente1 = new ClientEntity(1L, "918273645", "Javiera Reyes Soto", LocalDate.of(1995, 3, 5), "javiera.reyes92@mail.com");
        cliente2 = new ClientEntity(2L, "837465920", "Tomás Vidal Pino", LocalDate.of(1987, 7, 22), "tomas.vidal87@mail.com");
        List<ClientEntity> participants = new ArrayList<>();
        participants.add(cliente1);
        participants.add(cliente2);

        booking = new BookingEntity();
        booking.setId(1L);
        booking.setNumberLap(10);
        booking.setClient(cliente1);
        booking.setBookingDate(LocalDate.of(2025, 05, 03));
        booking.setGroupSize(2);
        booking.setStartTime(LocalTime.of(15, 00));
        booking.setParticipants(participants);
        booking.setFinalPrice(20000.0);

        when(specialDayService.getAvailableHours(booking.getBookingDate()))
                .thenReturn(new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)});
        when(specialDayService.isSpecialDay(booking.getBookingDate())).thenReturn(false);
        when(specialDayService.isWeekend(booking.getBookingDate())).thenReturn(true);
    }

    @Test
    void testSaveBooking() {
        try {
            // Configurar cliente principal y participantes
            cliente1 = new ClientEntity(1L, "918273645", "Javiera Reyes Soto", LocalDate.of(1995, 3, 5), "javiera.reyes92@mail.com");
            cliente2 = new ClientEntity(2L, "837465920", "Tomás Vidal Pino", LocalDate.of(1987, 7, 22), "tomas.vidal87@mail.com");
            List<ClientEntity> participants = new ArrayList<>();
            participants.add(cliente1);
            participants.add(cliente2);

            // Configurar la reserva
            booking = new BookingEntity();
            booking.setId(1L);
            booking.setNumberLap(10);
            booking.setClient(cliente1);
            booking.setBookingDate(LocalDate.of(2025, 5, 3));
            booking.setGroupSize(2);
            booking.setStartTime(LocalTime.of(15, 0));
            booking.setParticipants(participants);
            booking.setFinalPrice(20000.0);

            // Configurar mocks
            MimeMessage mockMessage = mock(MimeMessage.class);
            when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
            when(clientService.getClientByRut(cliente1.getRut())).thenReturn(Optional.of(cliente1));
            when(clientService.getClientByRut(cliente2.getRut())).thenReturn(Optional.of(cliente2));
            when(pricingService.getLastPricing()).thenReturn(PricingEntity.builder()
                    .price10Laps(15000.0)
                    .iva(0.19)
                    .build());
            when(bookingRepository.save(any(BookingEntity.class))).thenReturn(booking);

            BookingEntity savedBooking = bookingService.saveBooking(booking);

            // Verificar resultados
            assertNotNull(savedBooking, "La reserva guardada no debe ser nula.");
            assertEquals(booking.getClient(), savedBooking.getClient(), "El cliente de la reserva no coincide.");
            verify(bookingRepository, times(2)).save(any(BookingEntity.class)); // Permitir 2 invocaciones
        } catch (Exception e) {
            fail("Se produjo una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetBookings() {
        List<BookingEntity> bookings = new ArrayList<>();
        bookings.add(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void testGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingEntity result = bookingService.getBookingById(1L);

        assertNotNull(result);
        assertEquals(booking, result);
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookingByMonth() {
        List<BookingEntity> bookings = List.of(booking);
        when(bookingRepository.findByMonth(5)).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingByMonth(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findByMonth(5);
    }

    @Test
    void testGetBookingByDay() {
        List<BookingEntity> bookings = List.of(booking);
        when(bookingRepository.findByDay(3)).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingByDay(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findByDay(3);
    }

    @Test
    void testGetBookingByYear() {
        List<BookingEntity> bookings = List.of(booking);
        when(bookingRepository.findByYear(2025)).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingByYear(2025);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findByYear(2025);
    }

    @Test
    void testGetBookingsByDate() {
        List<BookingEntity> bookings = List.of(booking);
        when(bookingRepository.findByDate(LocalDate.of(2025, 5, 3))).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingsByDate(2025, 5, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findByDate(LocalDate.of(2025, 5, 3));
    }

    @Test
    void testGetBookingsBetweenDates() {
        List<BookingEntity> bookings = List.of(booking);
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);
        when(bookingRepository.findBookingsBetweenDates(startDate, endDate)).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingsBetweenDates(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findBookingsBetweenDates(startDate, endDate);
    }

    @Test
    void testGetBookingsByLapsAndDate() {
        List<BookingEntity> bookings = List.of(booking);
        when(bookingRepository.findBookingLapsByMY(10, 5, 2025)).thenReturn(bookings);

        List<BookingEntity> result = bookingService.getBookingsByLapsAndDate(10, 5, 2025);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(booking, result.get(0));
        verify(bookingRepository, times(1)).findBookingLapsByMY(10, 5, 2025);
    }

    @Test
    void testUpdateBooking() {
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingEntity result = bookingService.updateBooking(booking);

        assertNotNull(result);
        assertEquals(booking, result);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testDeleteBooking() throws Exception {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        boolean result = bookingService.deleteBooking(1L);

        assertTrue(result);
        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void testGetVisitCount() {
        when(bookingRepository.countVisitsInMonthAndYear(1L, 5, 2025)).thenReturn(3L);

        long result = bookingService.getVisitCount(1L, 5, 2025);

        assertEquals(3L, result);
        verify(bookingRepository, times(1)).countVisitsInMonthAndYear(1L, 5, 2025);
    }

    @Test
    void testCalculateEndTime() {
        LocalTime startTime = LocalTime.of(15, 0);
        int laps = 10;

        LocalTime result = bookingService.calculateEndTime(startTime, laps);

        assertNotNull(result);
        assertEquals(LocalTime.of(15, 30), result, "El tiempo final no coincide con el esperado.");
    }


    @Test
    void testIsOverlapping() {
        LocalTime newStart = LocalTime.of(15, 0);
        LocalTime newEnd = LocalTime.of(15, 30);
        LocalTime existingStart = LocalTime.of(15, 15);
        LocalTime existingEnd = LocalTime.of(15, 45);

        boolean result = bookingService.isOverlapping(newStart, newEnd, existingStart, existingEnd);

        assertTrue(result);
    }

    @Test
    void testIsAvailable() {
        // Simular una reserva existente en el mismo horario
        BookingEntity existingBooking = new BookingEntity();
        existingBooking.setStartTime(LocalTime.of(15, 0));
        existingBooking.setNumberLap(10);

        when(bookingRepository.findByDate(booking.getBookingDate())).thenReturn(List.of(existingBooking));
        when(specialDayService.getAvailableHours(booking.getBookingDate()))
                .thenReturn(new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)});

        boolean result = bookingService.isAvailable(booking);

        assertFalse(result, "El método debería devolver false si hay una reserva en el mismo horario.");
    }
    @Test
    void testBirthDayLimitDiscount() {
        int groupSize = 4;

        int result = bookingService.birthDayLimitDiscount(groupSize);

        assertEquals(1, result);
    }

    @Test
    void testGeneratePaymentReceiptExcel() throws IOException {
        // Configurar el mock de PricingEntity
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .weekendRise(0.15)
                .holydayRise(0.25)
                .iva(0.19)
                .build();
        when(pricingService.getLastPricing()).thenReturn(pricing);

        // Ejecutar el método
        byte[] result = bookingService.generatePaymentReceiptExcel(booking);

        // Verificar el resultado
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testWriteParticipantRow() {
        // Configurar mocks
        Sheet sheet = mock(Sheet.class);
        Row row = mock(Row.class);
        Cell cell = mock(Cell.class);

        // Configurar comportamiento de los mocks
        when(sheet.createRow(anyInt())).thenReturn(row);
        when(row.createCell(anyInt())).thenReturn(cell);

        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .weekendRise(0.15)
                .holydayRise(0.25)
                .iva(0.19)
                .build();
        when(pricingService.getLastPricing()).thenReturn(pricing);

        // Ejecutar el método
        int result = bookingService.writeParticipantRow(sheet, 0, cliente1, booking.getBookingDate(), 2, 10, pricing, true);

        // Verificar el resultado
        assertEquals(1, result);
    }
    @Test
    void testSaveExcelToDesktop() throws IOException {
        byte[] excelData = new byte[]{1, 2, 3};
        bookingService.saveExcelToDesktop(excelData, booking);

        // No se puede verificar directamente, pero asegúrate de que no haya excepciones.
    }

    @Test
    void testConvertExcelToPdf() throws IOException, DocumentException {
        // Crear un archivo Excel válido
        Path tempPath = Files.createTempFile("test", ".xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hoja1");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Test Data");

            try (FileOutputStream fos = new FileOutputStream(tempPath.toFile())) {
                workbook.write(fos);
            }
        }

        // Ejecutar el método
        byte[] result = bookingService.convertExcelToPdf(tempPath);

        // Verificar el resultado
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Eliminar el archivo temporal
        Files.delete(tempPath);
    }

    @Test
    void testGetEmailsFromBooking() {
        List<String> result = bookingService.getEmailsFromBooking(booking);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(cliente1.getEmail()));
        assertTrue(result.contains(cliente2.getEmail()));
    }

    @Test
    void testSendEmailWithAttachment() throws MessagingException {
        // Crear un mock de MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        MimeMessageHelper mockHelper = mock(MimeMessageHelper.class);

        // Configurar el mock de javaMailSender para devolver el MimeMessage simulado
        when(javaMailSender.createMimeMessage()).thenReturn(mockMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        // Ejecutar el método
        bookingService.sendEmailWithAttachment(
                List.of("test@mail.com"),
                "Subject",
                "Body",
                new byte[]{1, 2, 3},
                "attachment.pdf"
        );

        // Verificar que javaMailSender.createMimeMessage() fue llamado
        verify(javaMailSender, times(1)).createMimeMessage();

        // Verificar que javaMailSender.send() fue llamado con el MimeMessage simulado
        verify(javaMailSender, times(1)).send(mockMessage);

        // Opcional: Verificar que el mensaje tiene los valores esperados
        verify(mockMessage).setSubject("Subject");
    }
    @Test
    void testGetBookingExcelFileContent() {
        // Caso 1: La reserva existe y tiene contenido en el archivo Excel
        byte[] excelContent = new byte[]{1, 2, 3};
        booking.setExcelFileContent(excelContent);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<byte[]> result = bookingService.getBookingExcelFileContent(1L);

        assertTrue(result.isPresent(), "El resultado debe estar presente.");
        assertArrayEquals(excelContent, result.get(), "El contenido del archivo Excel no coincide.");

        // Caso 2: La reserva no existe
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());

        result = bookingService.getBookingExcelFileContent(2L);

        assertFalse(result.isPresent(), "El resultado no debe estar presente.");
    }
}