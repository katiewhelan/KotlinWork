package com.example.helloworld

fun main() {
    /*
     what  in the world is this
    Hello this is a comment on a 2nd page

    * */

    val nameTwo: String = "Katie"

    var myAge: Int = 31

    var isSunny = true


    var myName = "Denis"
    myName = "Frank"


    var result = 5 + 3
    //result = result /2
    result /= 2

//print("Hello World my name is $nameTwo and my age is $myAge")

    //  print("What does it look like outside today")

//print(result)


    var age = 22

    if (age >= 21) {
        println("You can vote and drink and drive(but not all at once)")
    } else if (age >= 18) {
        println("You can vote and you can drive")
    } else if (age >= 16) {
        println("You can drive")
    } else {
        println("You are a child")
    }


    when (age) {
        !in 0..20 -> println("You can drink")
        18, 19, 20 -> println("You can Vote")
        16, 17 -> println("You can drive")
        else -> println("You are a child")
    }

    var x = 100
//    while (x in 0..100) {
//        if (x % 2 == 0) {
//            println("$x")
//        }
//        x--
//    }

//    while(x in 0..100){
//        println("$x")
//        x -= 2
//    }

//     for(x in 1..10000){
//
//
//        if(x > 9000){
//            println("It's over 9000")
//        }
//    }

//    var humidityLevel = 80
//    var humidity = "humid"
//    while(humidity == "humid"){
//
//        if(humidityLevel >= 60) {
//            humidityLevel -= 5
//            println("HumidityLevel Decreased")
//        } else {
//            humidity = "comfy"
//            println("It's comfy now")
//        }
//
//    }

    var z = 12
    do {
        print("$z ")
    } while(z <= 10)

}

