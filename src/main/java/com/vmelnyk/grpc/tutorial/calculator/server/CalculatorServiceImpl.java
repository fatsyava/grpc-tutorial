package com.vmelnyk.grpc.tutorial.calculator.server;

import com.vmelnyk.grpc.tutorial.calculator.CalculatorServiceGrpc;
import com.vmelnyk.grpc.tutorial.calculator.func.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
  @Override
  public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
    responseObserver.onNext(
        SumResponse.newBuilder()
            .setResult(request.getFirstNumber() + request.getSecondNumber())
            .build());
    responseObserver.onCompleted();
  }

  @Override
  public void prime(PrimeRequest request, StreamObserver<PrimeResponse> responseObserver) {
    int n = request.getNumber();
    int k = 2;
    while (k <= n) {
      if (n % k == 0) {
        responseObserver.onNext(PrimeResponse.newBuilder().setPrimeFactor(k).build());
        n = n / k;
      } else {
        k = k + 1;
      }
    }
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<AvgRequest> avg(StreamObserver<AvgResponse> responseObserver) {
    return new StreamObserver<>() {
      int sum = 0;
      double count = 0;

      @Override
      public void onNext(AvgRequest request) {
        sum += request.getNumber();
        count += 1;
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(AvgResponse.newBuilder().setAvg(sum / count).build());
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public StreamObserver<MaxRequest> max(StreamObserver<MaxResponse> responseObserver) {
    return new StreamObserver<>() {
      int max = Integer.MIN_VALUE;

      @Override
      public void onNext(MaxRequest value) {
        if (value.getNumber() > max) {
          max = value.getNumber();

          System.out.println("Response max=" + max);

          responseObserver.onNext(MaxResponse.newBuilder().setMax(max).build());
        }
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onError(t);
      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public void sqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
    int number = request.getNumber();
    if (number < 0) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("The number being sent cannot be negative")
              .augmentDescription("number : " + number)
              .asRuntimeException());
      return;
    }
    responseObserver.onNext(SqrtResponse.newBuilder().setResult(Math.sqrt(number)).build());
    responseObserver.onCompleted();
  }
}
