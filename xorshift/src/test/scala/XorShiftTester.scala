// See LICENSE for license details.

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

trait XorShiftCalc {
  /**
    * XorShift32の期待値生成
    * @param seed シード
    * @return XorShift32で生成した乱数値
    */
  def xorshift32(seed: BigInt): BigInt = {
    var y = seed
    y = y ^ (y << 13) & BigInt("ffffffff", 16)
    y = y ^ (y >> 17) & BigInt("ffffffff", 16)
    y = y ^ (y << 5)  & BigInt("ffffffff", 16)
    y & BigInt("ffffffff", 16)
  }
}

class XorShift32FixedUnitTester(c: XorShift32Fixed) extends PeekPokeTester(c) with XorShiftCalc
class XorShiftUnitTester(c: XorShift) extends PeekPokeTester(c) with XorShiftCalc


class XorShiftTester extends ChiselFlatSpec {

  behavior of "XorShift32Fixed"

  val defaultArgs: Array[String] = Array("--generate-vcd-output=on")

  it should "updateがHighの時には毎サイクル乱数が生成される" in {
    val dst = "xorshift32fixed"
    val dstDir = "test_run_dir/" + dst
    Driver.execute(defaultArgs ++
      Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
      () => new XorShift32Fixed(BigInt(1), List(0, 1024))) {
      c => new XorShift32FixedUnitTester(c) {
        reset()

        var exp = BigInt(1)
        for (i <- 0 until 10) {
          exp = xorshift32(exp)
          println(s"(i, exp) = ($i, 0x${exp.toInt.toHexString})")
          poke(c.io.update, true)
          step(1)
          expect(c.io.randOut, exp & BigInt("3ff", 16))
        }
      }
    } should be (true)
  }

  behavior of "XorShift32"

  it should "updateがHighの時には毎サイクル乱数が生成される" in {
    val dst = "xorshift"
    val dstDir = "test_run_dir/" + dst

    for (bits <- 10 until 11) {
      Driver.execute(defaultArgs ++
        Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
        () => new XorShift(XorShift32, BigInt(1), bits)) {
        c =>
          new XorShiftUnitTester(c) {
            reset()

            var rand = BigInt(1)
            for (i <- 0 until 10000) {
              rand = xorshift32(rand)
              val exp = rand & BigInt(math.pow(2, bits).toLong - 1)
              println(s"(i, exp) = ($i, 0x${exp.toInt.toHexString})")
              poke(c.io.update, true)
              step(1)
              expect(c.io.randOut, exp)
            }
          }
      } should be(true)
    }
  }

}
