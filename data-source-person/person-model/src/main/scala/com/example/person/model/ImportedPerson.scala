package com.example.person.model

import com.example.common.model.Hobby

case class Person(name: String, age: Option[Int], hobbies: Seq[Hobby])
