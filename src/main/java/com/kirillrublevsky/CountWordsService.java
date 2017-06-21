package com.kirillrublevsky;

import spark.Request;
import spark.Response;

import java.util.Random;

import static spark.Spark.get;

/**
 * Created by Kirill on 19.06.2017.
 */
public class CountWordsService {

    private static final Random RANDOM = new Random();

    private static volatile long count200;  //number of successful responses with http code 200
    private static volatile long count500;  //number of errors with http code 500
    private static volatile long finish500; //time in Timestamp when 'get' method stops returning errors with code 500
    private static volatile long finish403; //time in Timestamp when 'get' method stops returning errors with code 403

    public static void main(String[] args) {
        get("/count", CountWordsService::getResponse);
    }

    //Method returns either number of words in string or http error. I synchronized to avoid race conditions between
    //different client threads
    private static synchronized Object getResponse(final Request req, final Response res) {
        if (System.currentTimeMillis() < finish403) {   //checks if 'timer' for returning 403 is still on
            res.status(403);
            return "Forbidden";
        } else if (System.currentTimeMillis() < finish500) {    //checks if 'timer' for returning 500 is still on
            count500++; //if yes, increments error 500 counter
            if (count500 % 5 == 0) {    //if counter divides by 5 - starts 403 timer
                finish403 = System.currentTimeMillis() + getRandomSeconds();
            }
            res.status(500);
            return "Internal Server Error";
        } else {    //if no error timers are active - counts and returns number of words
            count200++; //increments response 200 counter
            if (count200 % 10 == 0) {   //if counter divides by 10 - starts 500 timer
                finish500 = System.currentTimeMillis() + getRandomSeconds();
            }
            return countWords(req.queryParams("str"));
        }
    }

    //Returns random time in milliseconds in range 10000 to 90000
    private static long getRandomSeconds() {
        return 10000L + (long) (90000L * RANDOM.nextDouble());
    }

    //Counts number of words in string
    private static int countWords(final String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        } else {
            return input.split("\\s+").length;
        }
    }
}
