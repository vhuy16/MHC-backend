package com.topick.superapp.mhc.enums;

public enum BookingStatus {
    PENDING,    // chờ thanh toán
    CONFIRMED,  // đã thanh toán
    DONE,       // hoàn thành (có hoặc không có patient)
    MISSED,     // scheduled job detect — không ai join
    CANCELLED   // huỷ chủ động
}
