package com.example.helloworld

fun main() {

    var samsung : MobilePhone = MobilePhone("Sams","Samsung", "123")
    var iphone: MobilePhone = MobilePhone("apple", "apple", "5s")
    var pixel: MobilePhone = MobilePhone("Pixel", "google", "6")

    samsung.chargeBattery(30)


}

class MobilePhone(osBName: String, brand: String, model: String){

    init {
        var battery: Int = 50
        println("Phone Created $osBName $brand $model")

    }

    fun chargeBattery(chargeLevel: Int) {
        println("current Level of charge is $chargeLevel")
    }

}