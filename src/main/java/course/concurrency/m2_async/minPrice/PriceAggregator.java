package course.concurrency.m2_async.minPrice;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PriceAggregator {

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        // place for your code
        List<CompletableFuture<Double>> futures = shopIds
                .stream()
                .map(shopId -> CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .exceptionally(throwable -> null)
                        .completeOnTimeout(null, 2900, TimeUnit.MILLISECONDS)
                )
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<Double> minFuture = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join) // Извлекаем результаты
                        .filter(Objects::nonNull)     // Фильтруем нулы
                        .min(Double::compareTo)       // Находим минимальное значение
                        .orElse(Double.NaN)
        );
        return minFuture.join();
    }
}
