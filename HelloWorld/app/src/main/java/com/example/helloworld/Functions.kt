package com.example.helloworld

fun main() {

    var num1: Double = 5.0
    var num2: Double = 6.0

    fun average(a :Double, b: Double) : Double {
        var result : Double = 0.0

            result = (a + b)/ 2

        return result
    }

    var final = average(num1,num2)

    print("$final")

}
