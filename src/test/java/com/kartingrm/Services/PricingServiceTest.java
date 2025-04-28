package com.kartingrm.Services;

import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Entities.PricingEntity;
import com.kartingrm.Repositories.PricingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PricingServiceTest {

    @Mock
    private PricingRepository pricingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private SpecialDayService specialDayService;

    @InjectMocks
    private PricingService pricingService;

    private PricingEntity pricing, princing2; // Declarar como atributo de clase

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .discount3To5People(0.1)
                .discount6To10People(0.2)
                .discount11To15People(0.3)
                .discountRegular(0.1)
                .discountFrequent(0.2)
                .discountVeryFrequent(0.3)
                .birthdayDiscount(0.5)
                .iva(0.19)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);
    }

    @Test
    void testIsBirthDay() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .discount3To5People(0.1)
                .discount6To10People(0.2)
                .discount11To15People(0.3)
                .discountRegular(0.1)
                .discountFrequent(0.2)
                .discountVeryFrequent(0.3)
                .birthdayDiscount(0.5)
                .iva(0.19)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);

        ClientEntity client = new ClientEntity();
        client.setBirthDate(LocalDate.of(1990, 5, 15));

        assertTrue(pricingService.isBirthDay(client, LocalDate.of(2025, 5, 15)));
        assertFalse(pricingService.isBirthDay(client, LocalDate.of(2025, 6, 15)));

    }

    @Test
    void testGetAllPricing() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .build();

        PricingEntity pricing2 = PricingEntity.builder()
                .price10Laps(25000.0)
                .price15Laps(30000.0)
                .price20Laps(45000.0)
                .build();
        when(pricingRepository.findAll()).thenReturn(List.of(pricing, pricing2));
        List<PricingEntity> result = pricingService.getAllPricing();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(pricing, result.get(0));
        verify(pricingRepository, times(1)).findAll();
    }

    @Test
    void testGetLastPricing() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);

        PricingEntity result = pricingService.getLastPricing();
        assertNotNull(result);
        assertEquals(pricing, result);
        verify(pricingRepository, times(1)).findTopByOrderByIdDesc();
    }

    @Test
    void testDeletePricing() {
        doNothing().when(pricingRepository).deleteById(1L);

        pricingService.deletePricing(1L);

        verify(pricingRepository, times(1)).deleteById(1L);

}
    @Test
    void testGetPricingById() {

        PricingEntity pricing5 = PricingEntity.builder()
                .price10Laps(234450.0)
                .price15Laps(267980.0)
                .price20Laps(293330.0)
                .build();
        PricingEntity pricing6 = PricingEntity.builder()
                .price10Laps(12400.0)
                .price15Laps(21200.0)
                .price20Laps(25600.0)
                .build();
        when(pricingRepository.findById(1L)).thenReturn(Optional.of(pricing5));

        PricingEntity result = pricingService.getPricingById(1L);

        assertNotNull(result);
        assertEquals(pricing5, result);
        verify(pricingRepository, times(1)).findById(1L);
    }
    @Test
    void testSavePricing() {
        PricingEntity pricing4 = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .build();

        when(pricingRepository.save(pricing4)).thenReturn(pricing4);

        PricingEntity result = pricingService.savePricing(pricing4);

        assertNotNull(result);
        assertEquals(pricing4, result);
        verify(pricingRepository, times(1)).save(pricing4);
    }

    @Test
    void testUpdatePricing() {

        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .build();

        when(pricingRepository.save(pricing)).thenReturn(pricing);

        PricingEntity result = pricingService.updatePricing(pricing);

        assertNotNull(result);
        assertEquals(pricing, result);
        verify(pricingRepository, times(1)).save(pricing);
    }

    @Test
    void testCalculateTotalPriceForBirthdayClients() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .birthdayDiscount(0.5)
                .iva(0.19)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);
        when(specialDayService.isSpecialDay(any(LocalDate.class))).thenReturn(false);
        when(specialDayService.isWeekend(any(LocalDate.class))).thenReturn(true);
        when(bookingService.getVisitCount(anyLong(), anyInt(), anyInt())).thenReturn(3L);

        ClientEntity client1 = new ClientEntity();
        client1.setId(1L);
        client1.setBirthDate(LocalDate.of(1990, 5, 15));

        ClientEntity client2 = new ClientEntity();
        client2.setId(2L);
        client2.setBirthDate(LocalDate.of(1990, 6, 15));

        List<ClientEntity> clients = List.of(client1, client2);

        double result = pricingService.calculateTotalPriceForBirthdayClients(clients, 10, 4, 1, LocalDate.of(2025, 5, 15), pricing);
        assertTrue(result > 0);
    }


    @Test
    void testCalculatePricePerPerson() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .weekendRise(0.1)
                .holydayRise(0.2)
                .discount3To5People(0.1)
                .discountRegular(0.1)
                .birthdayDiscount(0.5)
                .iva(0.19)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);
        when(specialDayService.isSpecialDay(any(LocalDate.class))).thenReturn(false);
        when(specialDayService.isWeekend(any(LocalDate.class))).thenReturn(true);
        when(bookingService.getVisitCount(anyLong(), anyInt(), anyInt())).thenReturn(3L);

        ClientEntity client = new ClientEntity();
        client.setId(1L);
        client.setBirthDate(LocalDate.of(1990, 5, 15));

        double result = pricingService.calculatePricePerPerson(client, LocalDate.of(2025, 5, 15), 4, 10, pricing, 1);
        assertTrue(result > 0);
    }

    @Test
    void testGetBasePriceForLaps() {
        PricingEntity pricing = PricingEntity.builder()
                .price10Laps(15000.0)
                .price15Laps(20000.0)
                .price20Laps(25000.0)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);

        assertEquals(15000.0, pricingService.getBasePriceForLaps(10));
        assertEquals(20000.0, pricingService.getBasePriceForLaps(15));
        assertEquals(25000.0, pricingService.getBasePriceForLaps(20));
        assertEquals(0.0, pricingService.getBasePriceForLaps(5));
    }

    @Test
    void testCalculateGroupDiscount() {
        PricingEntity pricing = PricingEntity.builder()
                .discount3To5People(0.1)
                .discount6To10People(0.2)
                .discount11To15People(0.3)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);

        assertEquals(0.1, pricingService.calculateGroupDiscount(4));
        assertEquals(0.2, pricingService.calculateGroupDiscount(7));
        assertEquals(0.3, pricingService.calculateGroupDiscount(12));
        assertEquals(0.0, pricingService.calculateGroupDiscount(2));
    }

    @Test
    void testCalculateFrequencyDiscount() {
        PricingEntity pricing = PricingEntity.builder()
                .discountRegular(0.1)
                .discountFrequent(0.2)
                .discountVeryFrequent(0.3)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);
        when(bookingService.getVisitCount(anyLong(), anyInt(), anyInt())).thenReturn(3L, 5L, 7L);

        ClientEntity client = new ClientEntity();
        client.setId(1L);

        assertEquals(0.1, pricingService.calculateFrequencyDiscount(client, LocalDate.of(2025, 5, 15)));
        assertEquals(0.2, pricingService.calculateFrequencyDiscount(client, LocalDate.of(2025, 5, 15)));
        assertEquals(0.3, pricingService.calculateFrequencyDiscount(client, LocalDate.of(2025, 5, 15)));
    }

    @Test
    void testCalculateBirthdayDiscount() {
        PricingEntity pricing = PricingEntity.builder()
                .birthdayDiscount(0.5)
                .build();

        when(pricingRepository.findTopByOrderByIdDesc()).thenReturn(pricing);

        ClientEntity client = new ClientEntity();
        client.setBirthDate(LocalDate.of(1990, 5, 15));

        assertEquals(0.5, pricingService.calculateBirthdayDiscount(client, LocalDate.of(2025, 5, 15)));
        assertEquals(0.0, pricingService.calculateBirthdayDiscount(client, LocalDate.of(2025, 6, 15)));
    }


}

