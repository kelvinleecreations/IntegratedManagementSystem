package com.example.integratedmanagementsystem.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RateLimitingService {
  
  // use concurrent hashmap to handle multiple requests from different users simultaneously, without throwing errors or slowing down in performance
  private final Map<String, Integer> requestCount = new ConcurrentHashMap<>();
  private final Map<String, Long> resetTimeStamp = new ConcurrentHashMap<>();

  // synchronized locks and checks the request one at a time
  // this is to prevent accidental exceeding if users happens to send two requests at the exact same time
  public synchronized boolean hasNotExceeded(String key, Integer maximumRequests, Long cooldownDuration) {
    // in milliseconds, fetch the current time
    Long now = System.currentTimeMillis();
    // looks up the key and returns the value (time when cooldown officially ends)
    Long resetTs = resetTimeStamp.get(key);

    // there is no value for timestamp (meaning it is first request as user has never made a request before) or the current time has passed the reset time stamp,
    if (resetTs == null || now > resetTs) {
      // assign key-value to requestCount, increment count by 1
      requestCount.put(key, 1);
      // assign key-value to resetTimeStamp, assign the next reset time by adding current time and cooldown duration converted to seconds
      resetTimeStamp.put(key, now + (cooldownDuration * 1000));

      return true;
    }

    // fetch the request counter
    Integer currentRequestCount = requestCount.get(key);
    // if the request counter is less than maximum request assigned,
    if (currentRequestCount < maximumRequests) {
      // assign key-value to requestCount, increment count by 1
      requestCount.put(key, currentRequestCount + 1);
      return true;
    }

    // else, request has been made and current time has not passed the reset time stamp and current request count is more than maximum request
    // user request reached limit
    return false;
  }
}
