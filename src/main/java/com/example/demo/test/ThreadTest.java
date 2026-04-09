package com.example.demo.test;

import java.util.concurrent.*;

public class ThreadTest {

    public static void main(String[] args) throws InterruptedException {
        try {
            Thread.Builder builder = Thread.ofVirtual().name("Worker-", 1);
            Runnable task = () -> {
                System.out.println("Thread ID: " + Thread.currentThread().threadId());
            };
            // thread 1
            Thread t1  = builder.start(task);
            t1.join();
            System.out.println(t1.getName() + " terminated");

            // thread 2
            Thread t2 = builder.start(task);
            t2.join();
            System.out.println(t2.getName() + " terminated");
            try(ExecutorService myExecutor = Executors.newVirtualThreadPerTaskExecutor()){
                Future<?> future = myExecutor.submit(() -> System.out.println("Running thread"));
                future.get();
                System.out.println("Task completed");
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new InterruptedException();
        }
    }

}
