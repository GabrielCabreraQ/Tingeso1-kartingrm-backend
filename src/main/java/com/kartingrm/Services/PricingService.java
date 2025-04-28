package com.kartingrm.Services;


import com.kartingrm.Entities.ClientEntity;
import com.kartingrm.Entities.PricingEntity;
import com.kartingrm.Repositories.BookingRepository;
import com.kartingrm.Repositories.PricingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PricingService {
    @Autowired
    private PricingRepository pricingRepository;

    @Autowired
    @Lazy
    private BookingService bookingService;

    @Autowired
    private SpecialDayService specialDayService;

    public PricingEntity getLastPricing() {
        return pricingRepository.findTopByOrderByIdDesc();
    }

    public double getBasePriceForLaps(int numberLap) {
        if (numberLap == 10) return getLastPricing().getPrice10Laps();
        else if (numberLap == 15) return getLastPricing().getPrice15Laps();
        else if (numberLap == 20) return getLastPricing().getPrice20Laps();
        else return 0;

    }

    public double calculateGroupDiscount(int groupSize) {
        double discount = 0;
        if (groupSize >= 3 && groupSize <= 5) discount = getLastPricing().getDiscount3To5People();
        else if (groupSize >= 6 && groupSize <= 10) discount = getLastPricing().getDiscount6To10People();
        else if (groupSize >= 11 && groupSize <= 15) discount = getLastPricing().getDiscount11To15People();
        return discount;
    }


    public double calculateFrequencyDiscount(ClientEntity participant, LocalDate bookingDate) {
        int month = bookingDate.getMonthValue();
        int year = bookingDate.getYear();
        long monthlyVisits = bookingService.getVisitCount(participant.getId(), month, year);

        double discount = 0;
        if (monthlyVisits >= 2 && monthlyVisits <= 4) discount = getLastPricing().getDiscountRegular();
        else if (monthlyVisits >= 5 && monthlyVisits <= 6) discount = getLastPricing().getDiscountFrequent();
        else if (monthlyVisits >= 7) discount = getLastPricing().getDiscountVeryFrequent();
        return discount;
    }

    public double calculateBirthdayDiscount(ClientEntity participant, LocalDate bookingDate) {

        boolean isBirthday = isBirthDay(participant,bookingDate);
        if (isBirthday) {
            return getLastPricing().getBirthdayDiscount();
        } else {
            return 0;
        }
    }

    public double calculatePricePerPerson(ClientEntity participant, LocalDate bookingDate, int groupSize, int numberLap, PricingEntity pricing, int applyBirthday) {
        double basePrice = getBasePriceForLaps(numberLap);
        if (specialDayService.isSpecialDay(bookingDate)) {
            basePrice = basePrice + basePrice*pricing.getHolydayRise();  // Aumento en el precio base por día especial
        }
        if (specialDayService.isWeekend(bookingDate)) {
            basePrice = basePrice + basePrice*pricing.getWeekendRise();
        }

        double groupDiscount = calculateGroupDiscount(groupSize);
        double freqDiscount = calculateFrequencyDiscount(participant, bookingDate);
        double birthdayDiscount = (applyBirthday > 0) ? calculateBirthdayDiscount(participant, bookingDate) : 0;

        //double weekendDiscount = (specialDayService.isWeekend(bookingDate)) ? calculateWeekendDiscound(pricing) : 0;
        //double specialDayDiscount = (specialDayService.isSpecialDay(bookingDate)) ? calculateSpecialDayDiscount(pricing) : 0;

        double maxDiscount = Math.max(groupDiscount, Math.max(freqDiscount,birthdayDiscount));
        double finalPrice = Math.round(basePrice * (1 - maxDiscount));
        double iva = Math.round(finalPrice * getLastPricing().getIva());

        return Math.round(finalPrice+iva);


    }

    public double calculateTotalPriceForBirthdayClients(List<ClientEntity> birthdayClients, int numberLap, int groupSize, int birthdayLimit, LocalDate bookingDate, PricingEntity pricing) {
        double total = 0;

        List<ClientEntity> sortedBirthdayClients = birthdayClients.stream()
                .sorted(Comparator.comparingDouble(client -> {
                    double basePrice = getBasePriceForLaps(numberLap);
                    double groupDiscount = calculateGroupDiscount(groupSize);
                    double freqDiscount = calculateFrequencyDiscount(client, bookingDate);
                    return Math.max(groupDiscount, freqDiscount);
                }))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedBirthdayClients.size(); i++) {
            ClientEntity participant = sortedBirthdayClients.get(i);
            boolean applyBirthdayDiscount = i < birthdayLimit;

            double individualPrice = calculatePricePerPerson(
                    participant,
                    bookingDate,
                    groupSize,
                    numberLap,
                    pricing,
                    applyBirthdayDiscount ? 1 : 0
            );

            total += individualPrice;
        }

        return total;
    }

    public boolean isBirthDay(ClientEntity client, LocalDate bookingDate) {
        return client.getBirthDate().getDayOfMonth() == bookingDate.getDayOfMonth()
                && client.getBirthDate().getMonthValue() == bookingDate.getMonthValue();
    }

    public List<PricingEntity> getAllPricing() {
        return pricingRepository.findAll();
    }

    public PricingEntity getPricingById(Long id) {
        return pricingRepository.findById(id).get();
    }

    public PricingEntity savePricing(PricingEntity pricing) {
        return pricingRepository.save(pricing);
    }

    public PricingEntity updatePricing(PricingEntity pricing) {
        return pricingRepository.save(pricing);
    }

    public void deletePricing(Long id) {
        pricingRepository.deleteById(id);
    }



}
