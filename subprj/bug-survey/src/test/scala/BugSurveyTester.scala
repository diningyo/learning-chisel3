// See LICENSE for license details.

import chisel3.iotesters._

class BugSurveyUnitTester(c: BugSurvey) extends PeekPokeTester(c) {
  override def reset(n: Int): Unit = {
    poke(c.io.in1, true)
    if (c.enIn2)  {
      poke(c.io.in2.get, true)
    }
    poke(c.io.in3, 0xff)
    super.reset(n)
  }
}


abstract class BugSurveyBaseTester extends ChiselFlatSpec {
  behavior of "BugSurvey"

  val dstDir = "-td=test_run_dir/BugSurvey"
  val args = Array(
    "-tiv=on",
    "-tn=BugSurvey",
    "-tgvo=on"
  )
}

class BugSurveyTesterTreadle extends BugSurveyBaseTester {

  val addArgs = Array("-tbn=treadle")

  it should "enIn2=false" in {
    Driver.execute(args ++ addArgs :+ dstDir + "treadle-000",
      () => new BugSurvey(enIn2 = false)) {
      c => new BugSurveyUnitTester(c) {
        reset(10)
        step(10)
      }
    } should be (true)
  }

  it should "enIn2=true" in {
    Driver.execute(args ++ addArgs :+ dstDir + "treadle-001",
      () => new BugSurvey(enIn2 = true)) {
      c => new BugSurveyUnitTester(c) {
        reset(10)
        step(10)
      }
    } should be (true)
  }
}

class BugSurveyTesterVerilator extends BugSurveyBaseTester {

  val addArgs = Array("-tbn=verilator")

  it should "enIn2=false" in {
    Driver.execute(args ++ addArgs :+ dstDir + "verilator-000",
      () => new BugSurvey(enIn2 = false)) {
      c => new BugSurveyUnitTester(c) {
        reset(10)
        step(10)
      }
    } should be (true)
  }

  it should "enIn2=true" in {
    Driver.execute(args ++ addArgs :+ dstDir + "verilator-001",
      () => new BugSurvey(enIn2 = true)) {
      c => new BugSurveyUnitTester(c) {
        reset(10)
        step(10)
      }
    } should be (true)
  }
}
