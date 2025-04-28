package com.kartingrm.Services;

import com.kartingrm.Entities.SpecialDaysEntity;
import com.kartingrm.Repositories.SpecialDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class SpecialDayService {
    @Autowired
    SpecialDayRepository specialDayRepository;

    public boolean isSpecialDay(LocalDate date) {
        Optional<SpecialDaysEntity> specialDay = specialDayRepository.findByDate(date);
        return specialDay.isPresent();
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public LocalTime[] getAvailableHours(LocalDate bookingDate) {
        if (isSpecialDay(bookingDate)) {
            // Si es un día especial, el horario es de 10:00 a 22:00
            return new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)};
        } else if (isWeekend(bookingDate)) {
            // Si es fin de semana, el horario es de 10:00 a 22:00 (igual a días especiales)
            return new LocalTime[]{LocalTime.of(10, 0), LocalTime.of(22, 0)};
        } else {
            // Si no es un día especial ni fin de semana, el horario es de 14:00 a 22:00
            return new LocalTime[]{LocalTime.of(14, 0), LocalTime.of(22, 0)};
        }
    }

    public List<SpecialDaysEntity> getSpecialDays() {
        return specialDayRepository.findAll();
    }

    public SpecialDaysEntity getSpecialDayById(Long id) {
        return specialDayRepository.findById(id).get();
    }

    public SpecialDaysEntity saveSpecialDay(SpecialDaysEntity specialDay) {
        return specialDayRepository.save(specialDay);
    }

    public SpecialDaysEntity updateSpecialDay(SpecialDaysEntity specialDay) {
        return specialDayRepository.save(specialDay);
    }

    public void deleteSpecialDay(Long id) {
        specialDayRepository.deleteById(id);
    }
}


