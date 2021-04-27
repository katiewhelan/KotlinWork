package com.example.helloworld

fun main(){

    var list: ArrayList<Double> = ArrayList<Double>()

    list.add(5.0)
    list.add(5.0)
    list.add(5.0)
    list.add(5.0)
    list.add(5.0)

    var totalSum: Double = 0.0

    for(i in list) {

        totalSum += i
    }

    var result = totalSum/list.count().toDouble()

    println("The average is $result")


}