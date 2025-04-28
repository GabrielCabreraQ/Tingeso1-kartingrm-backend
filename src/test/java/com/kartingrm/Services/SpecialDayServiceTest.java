package com.kartingrm.Services;

import com.kartingrm.Entities.SpecialDaysEntity;
import com.kartingrm.Repositories.SpecialDayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpecialDayServiceTest {

    @InjectMocks
    private SpecialDayService specialDayService;

    @Mock
    private SpecialDayRepository specialDayRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsSpecialDayTrue() {
        LocalDate date = LocalDate.of(2025, 5, 15);
        when(specialDayRepository.findByDate(date)).thenReturn(Optional.of(new SpecialDaysEntity(1L, date, "Holiday")));

        boolean result = specialDayService.isSpecialDay(date);

        assertTrue(result);
        verify(specialDayRepository, times(1)).findByDate(date);
    }

    @Test
    void testIsSpecialDayFalse() {
        LocalDate date = LocalDate.of(2025, 5, 15);
        when(specialDayRepository.findByDate(date)).thenReturn(Optional.empty());

        boolean result = specialDayService.isSpecialDay(date);

        assertFalse(result);
        verify(specialDayRepository, times(1)).findByDate(date);
    }

    @Test
    void testIsWeekendTrueSaturday() {
        LocalDate saturday = LocalDate.of(2025, 5, 17);

        boolean result = specialDayService.isWeekend(saturday);

        assertTrue(result);
    }

    @Test
    void testIsWeekendTrueSunday() {
        LocalDate sunday = LocalDate.of(2025, 5, 18);

        boolean result = specialDayService.isWeekend(sunday);

        assertTrue(result);
    }

    @Test
    void testIsWeekendFalse() {
        LocalDate weekday = LocalDate.of(2025, 5, 15);

        boolean result = specialDayService.isWeekend(weekday);

        assertFalse(result);
    }

    @Test
    void testGetAvailableHoursSpecialDay() {
        LocalDate specialDay = LocalDate.of(2025, 5, 15);
        when(specialDayRepository.findByDate(specialDay)).thenReturn(Optional.of(new SpecialDaysEntity(1L, specialDay, "Holiday")));

        LocalTime[] result = specialDayService.getAvailableHours(specialDay);

        assertArrayEquals(new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)}, result);
    }

    @Test
    void testGetAvailableHoursWeekend() {
        LocalDate weekend = LocalDate.of(2025, 5, 17); // Saturday

        LocalTime[] result = specialDayService.getAvailableHours(weekend);

        assertArrayEquals(new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)}, result);
    }

    @Test
    void testGetAvailableHoursWeekday() {
        LocalDate weekday = LocalDate.of(2025, 5, 15);
        when(specialDayRepository.findByDate(weekday)).thenReturn(Optional.empty());

        LocalTime[] result = specialDayService.getAvailableHours(weekday);

        assertArrayEquals(new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(22, 0)}, result);
    }
    @Test
    void testGetSpecialDays() {
        SpecialDaysEntity specialDay1 = new SpecialDaysEntity(1L, LocalDate.of(2025, 5, 15), "Holiday");
        SpecialDaysEntity specialDay2 = new SpecialDaysEntity(2L, LocalDate.of(2025, 12, 25), "Christmas");
        when(specialDayRepository.findAll()).thenReturn(List.of(specialDay1, specialDay2));

        List<SpecialDaysEntity> result = specialDayService.getSpecialDays();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(specialDay1, result.get(0));
        assertEquals(specialDay2, result.get(1));
        verify(specialDayRepository, times(1)).findAll();
    }

    @Test
    void testGetSpecialDayById() {
        SpecialDaysEntity specialDay = new SpecialDaysEntity(1L, LocalDate.of(2025, 5, 15), "Holiday");
        when(specialDayRepository.findById(1L)).thenReturn(Optional.of(specialDay));

        SpecialDaysEntity result = specialDayService.getSpecialDayById(1L);

        assertNotNull(result);
        assertEquals(specialDay, result);
        verify(specialDayRepository, times(1)).findById(1L);
    }

    @Test
    void testSaveSpecialDay() {
        SpecialDaysEntity specialDay = new SpecialDaysEntity(null, LocalDate.of(2025, 5, 15), "Holiday");
        SpecialDaysEntity savedSpecialDay = new SpecialDaysEntity(1L, LocalDate.of(2025, 5, 15), "Holiday");
        when(specialDayRepository.save(specialDay)).thenReturn(savedSpecialDay);

        SpecialDaysEntity result = specialDayService.saveSpecialDay(specialDay);

        assertNotNull(result);
        assertEquals(savedSpecialDay, result);
        verify(specialDayRepository, times(1)).save(specialDay);
    }

    @Test
    void testUpdateSpecialDay() {
        SpecialDaysEntity specialDay = new SpecialDaysEntity(1L, LocalDate.of(2025, 5, 15), "Updated Holiday");
        when(specialDayRepository.save(specialDay)).thenReturn(specialDay);

        SpecialDaysEntity result = specialDayService.updateSpecialDay(specialDay);

        assertNotNull(result);
        assertEquals(specialDay, result);
        verify(specialDayRepository, times(1)).save(specialDay);
    }

    @Test
    void testDeleteSpecialDay() {
        doNothing().when(specialDayRepository).deleteById(1L);

        specialDayService.deleteSpecialDay(1L);

        verify(specialDayRepository, times(1)).deleteById(1L);
    }
}