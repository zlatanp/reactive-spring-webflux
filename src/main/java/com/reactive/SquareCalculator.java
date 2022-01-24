package com.reactive;

import java.util.concurrent.*;

public class SquareCalculator {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
//        Future<Integer> future = new SquareCalculator().calculate(10);
//
//        while(!future.isDone()) {
//            System.out.println("Calculating...");
//            Thread.sleep(300);
//        }
//
//        Integer result = future.get();
//        executor.shutdown();
//        System.out.println("Result is: " + result);
        var completableFuture = calculateAsync();
        while(!completableFuture.isDone()) {
            System.out.println("Calculating...");
            Thread.sleep(300);
        }
        System.out.println(completableFuture.get());
        executor.shutdown();

    }

    public static Future<String> calculateAsync() throws InterruptedException {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        executor.submit(() -> {
            Thread.sleep(500);
            completableFuture.complete("Hello");
            return null;
        });

        return completableFuture;
    }
    
    public Future<Integer> calculate(Integer input) {
        return executor.submit(() -> {
            Thread.sleep(1000);
            return input * input;
        });
    }
}