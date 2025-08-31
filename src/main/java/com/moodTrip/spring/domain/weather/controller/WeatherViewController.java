package com.moodTrip.spring.domain.weather.controller;


import com.moodTrip.spring.domain.attraction.dto.response.AttractionDetailResponse;
import com.moodTrip.spring.domain.attraction.dto.response.AttractionResponse;
import com.moodTrip.spring.domain.attraction.service.AttractionService;
import com.moodTrip.spring.domain.emotion.service.AttractionEmotionService;
import com.moodTrip.spring.domain.weather.dto.response.WeatherResponse;
import com.moodTrip.spring.domain.weather.service.WeatherAttractionService;
import com.moodTrip.spring.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/attraction/weather")
public class WeatherViewController {


    private final AttractionService attractionService;
    private final AttractionEmotionService attractionEmotionService;
    private final WeatherAttractionService weatherAttractionService;
    private final WeatherService weatherService;

    @GetMapping("/detail")
    public String detail(@RequestParam("contentId") Long contentId, Model model) {
        var weather = weatherService.getSeoulCurrentWeather(contentId);
        var recommended = weatherAttractionService.getSeoulAttractionsByWeather(contentId);
        var detail = attractionService.getDetailResponse(contentId);
        var tags = attractionService.getEmotionTagNames(contentId);

        model.addAttribute("weather", weather);
        model.addAttribute("detail", detail);
        model.addAttribute("tags", tags);
        model.addAttribute("recommended", recommended);
        return "recommand-tourist-attractions-detail/weather-detail-page";
    }


}