package com.vmelnyk.grpc.tutorial.calculator.client;

import com.vmelnyk.grpc.tutorial.calculator.CalculatorServiceGrpc;
import com.vmelnyk.grpc.tutorial.calculator.func.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

  public static void main(String[] args) throws InterruptedException {
    if (args.length == 0) {
      System.out.println("need 1 argument");
      return;
    }

    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50052).usePlaintext().build();

    switch (args[0]) {
      case "sum":
        doSum(channel);
        break;
      case "prime":
        fetchPrime(channel);
        break;
      case "avg":
        calculateAvg(channel);
        break;
      case "max":
        doMax(channel);
        break;
      case "sqrt":
        doSqrt(channel);
        break;
      default:
        System.out.println("invalid keyword");
        break;
    }
  }

  private static void doSqrt(ManagedChannel channel) {
    System.out.println("entered doSqrt");
    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
        CalculatorServiceGrpc.newBlockingStub(channel);
    SqrtResponse response = stub.sqrt(SqrtRequest.newBuilder().setNumber(25).build());
    System.out.println("sqrt 25 = " + response.getResult());
    try {
      response = stub.sqrt(SqrtRequest.newBuilder().setNumber(-1).build());
      System.out.println("sqrt -1 = " + response.getResult());
    } catch (RuntimeException e) {
      System.out.println("Got an exception for sqrt : ");
      e.printStackTrace();
    }
  }

  private static void doMax(ManagedChannel channel) throws InterruptedException {
    System.out.println("entered doMax");

    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<MaxRequest> stream =
        stub.max(
            new StreamObserver<>() {
              @Override
              public void onNext(MaxResponse value) {
                System.out.println("max = " + value.getMax());
              }

              @Override
              public void onError(Throwable t) {
                System.out.println("Got an exception for max : ");
                t.printStackTrace();
              }

              @Override
              public void onCompleted() {
                latch.countDown();
              }
            });


      int i = 10;
      while (i-- > 0) {
          int number = (int) (Math.random() * 100);

          System.out.println("Request for max by " + number);

          stream.onNext(MaxRequest.newBuilder().setNumber(number).build());
          Thread.sleep(100);
      }

    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void calculateAvg(ManagedChannel channel) throws InterruptedException {
    System.out.println("Entered calculateAvg()");

    CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);

    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<AvgRequest> stream =
        stub.avg(
            new StreamObserver<>() {
              @Override
              public void onNext(AvgResponse response) {
                System.out.println("Average : " + response.getAvg());
              }

              @Override
              public void onError(Throwable t) {}

              @Override
              public void onCompleted() {
                latch.countDown();
              }
            });

    int i = 10;
    while (i-- > 0) {
      int number = (int) (Math.random() * 100);
      stream.onNext(AvgRequest.newBuilder().setNumber(number).build());
      Thread.sleep(100);
    }

    stream.onCompleted();
    latch.await(3, TimeUnit.SECONDS);
  }

  private static void fetchPrime(ManagedChannel channel) {
    System.out.println("Entered fetchPrime");

    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
        CalculatorServiceGrpc.newBlockingStub(channel);

    stub.prime(PrimeRequest.newBuilder().setNumber(120).build())
        .forEachRemaining(
            primeResponse -> {
              System.out.println("factor: " + primeResponse.getPrimeFactor());
            });
  }

  private static void doSum(ManagedChannel channel) {
    System.out.println("enter doSum()");

    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
        CalculatorServiceGrpc.newBlockingStub(channel);

    int i = 10;
    while (i-- > 0) {

      final int firstNumber = (int) (Math.random() * 100);
      final int secondNumber = (int) (Math.random() * 100);

      SumResponse response =
          stub.sum(
              SumRequest.newBuilder()
                  .setFirstNumber(firstNumber)
                  .setSecondNumber(secondNumber)
                  .build());

      System.out.println("sum " + firstNumber + "+" + secondNumber + " = " + response.getResult());
    }
  }
}
