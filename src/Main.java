import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Main {

    private static final int POPULATION_THRESHOLD = 50000;

    WeatherAPI weatherAPI;
    // use Java Thread Pool
    ExecutorService executorService;

    public Main(WeatherAPI weatherAPI) {
        this.weatherAPI = weatherAPI;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public List<City> topCitiesByAggregation(Set<String> cityIds, AggregationType aggregationType) throws InterruptedException, ExecutionException {

        Set<City> cities = weatherAPI.getAllCitiesByIds(cityIds);
        List<City> bigCities = cities.stream()
                .filter(city -> city.getPopulation() > POPULATION_THRESHOLD)
                .toList();
        List<Future<AbstractMap.SimpleEntry<City, Double>>> futureCityResults = bigCities.stream()
                .map(city -> executorService.submit(() -> new AbstractMap.SimpleEntry<>(city, aggregate(city.getId(), aggregationType))))
                .toList();
        List<Map.Entry<City, Double>> cityAggregations = new ArrayList<>();

        for (Future<AbstractMap.SimpleEntry<City, Double>> futureCityResult : futureCityResults) {
            cityAggregations.add(futureCityResult.get());
        }

        return cityAggregations.stream()
                // descending order
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public double aggregate(String cityId, AggregationType aggregationType) {

        List<DailyTemp> dailyTemperatures = weatherAPI.getLastYearTemperature(cityId);
        if (dailyTemperatures.isEmpty()) return 0.0;

        switch (aggregationType) {
            case AVG:
                return dailyTemperatures.stream().mapToDouble(DailyTemp::getTemperature).average().orElse(0.0);
            case MAX:
                return dailyTemperatures.stream().mapToDouble(DailyTemp::getTemperature).max().orElse(0.0);
            case MEDIAN:
                // for the Median aggregator, we need to sort the temperatures
                double[] sortedTemperatures = dailyTemperatures.stream()
                        .mapToDouble(DailyTemp::getTemperature)
                        .sorted()
                        .toArray();
                int size = sortedTemperatures.length;
                return size % 2 == 0 ?
                        (sortedTemperatures[size / 2 - 1] + sortedTemperatures[size / 2]) / 2.0 :
                        sortedTemperatures[size / 2];
            default:
                throw new UnsupportedOperationException("Unsupported aggregation type: " + aggregationType);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Not enough arguments were provided. Use: java Main <cityIdsCommaSeparated> <aggregationType>");
            System.out.println("Example: java Main CHI,LA,NY,SF,TLV MAX");
            return;
        }

        String[] cityIdsArray = args[0].split(",");
        Set<String> cityIds = new HashSet<>(Arrays.asList(cityIdsArray));
        WeatherAPI weatherAPI = new MockWeatherAPI();
        Main aggregator = new Main(weatherAPI);
        AggregationType aggregationType;

        try {
            aggregationType = AggregationType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.printf("Invalid aggregation type. Allowed values: AVG, MAX, MEDIAN. Got: %s%n", args[1]);
            return;
        }

        try {
            List<City> topCities = aggregator.topCitiesByAggregation(cityIds, aggregationType);
            // print results
            topCities.forEach(city -> System.out.println(city.getName()));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            aggregator.shutdown();
        }
    }
}