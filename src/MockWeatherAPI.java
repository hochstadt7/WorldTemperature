import java.util.*;

class MockWeatherAPI implements WeatherAPI {

    private static final Map<String, City> CITIES_BY_IDS = Map.of(
            "CHI", new City("CHI", "Chicago", 270000),
            "LA", new City("LA", "Los Angeles", 400000),
            "NY", new City("NY", "New York", 80000),
            "SF", new City("SF", "San Francisco", 90000),
            "TLV", new City("TLV", "Tel Aviv", 4500000)
    );

    @Override
    public Set<City> getAllCitiesByIds(Set<String> cityIds) {
        Set<City> cities = new HashSet<>();
        for (String cityId : cityIds) {
            if (CITIES_BY_IDS.containsKey(cityId)) {
                cities.add(CITIES_BY_IDS.get(cityId));
            }
        }
        return cities;
    }

    @Override
    public List<DailyTemp> getLastYearTemperature(String cityId) {

        Random random = new Random();
        Calendar calendar = Calendar.getInstance();
        // go back one year
        calendar.add(Calendar.YEAR, -1);
        List<DailyTemp> dailyTemperatures = new ArrayList<>();

        for (int i = 0; i < 365; i++) {
            // move to the next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            double temperature = 15 + random.nextDouble() * 20; // generate random temperatures between 15°C and 35°C
            dailyTemperatures.add(new DailyTemp(temperature, calendar.getTime()));
        }

        return dailyTemperatures;
    }
}
