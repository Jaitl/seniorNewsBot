package com.github.jaitl.seniornews.storage

import org.scalatest.FunSuite
import org.scalatest.Matchers

class StopWordFilterTest extends FunSuite with Matchers {
  test("title has stop words") {
    val title = "best blockchain"

    val result = StopWordFilter.hasStopWords(title)

    result shouldBe true
  }

  test("title has no stop words") {
    val title = "The history and opportunity of the modern mortgage [video]"

    val result = StopWordFilter.hasStopWords(title)

    result shouldBe false
  }
}
