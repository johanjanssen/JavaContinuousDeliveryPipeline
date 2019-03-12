package com.example.demo;

public class PiTestExample {

    public int getValueOrBoundary(int value, int boundary) {
        if (value < boundary) {
            return value;
        }
        return boundary;
    }
}
