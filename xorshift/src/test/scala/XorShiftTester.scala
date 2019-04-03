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
    val mask = "f" * 8
    y = y ^ (y << 13) & BigInt(mask, 16)
    y = y ^ (y >> 17) & BigInt(mask, 16)
    y = y ^ (y << 5)  & BigInt(mask, 16)
    y & BigInt(mask, 16)
  }

  /**
    * XorShift64の期待値生成
    * @param seed シード
    * @return XorShift64で生成した乱数値
    */
  def xorshift64(seed: BigInt): BigInt = {
    var y = seed
    val mask = "f" * 16
    y = y ^ (y << 13) & BigInt(mask, 16)
    y = y ^ (y >> 7)  & BigInt(mask, 16)
    y = y ^ (y << 17)  & BigInt(mask, 16)
    y & BigInt(mask, 16)
  }

  /**
    * XorShift96の期待値生成
    * @param seed シード
    * @return XorShift96で生成した乱数値
    */
  def xorshift96(seed: BigInt): BigInt = {
    var y = seed
    val mask = "f" * 24
    y = y ^ (y << 13) & BigInt(mask, 16)
    y = y ^ (y >> 7)  & BigInt(mask, 16)
    y = y ^ (y << 17)  & BigInt(mask, 16)
    y & BigInt(mask, 16)
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
    val dst = "xorshift32"
    val dstDir = "test_run_dir/" + dst

    for (bits <- 1 until 32) {
      Driver.execute(defaultArgs ++
        Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
        () => new XorShift(XorShift32, BigInt(1), bits)) {
        c =>
          new XorShiftUnitTester(c) {
            reset()

            var rand = BigInt(1)
            for (i <- 0 until 1000) {
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

  behavior of "XorShift64"

  it should "updateがHighの時には毎サイクル乱数が生成される" in {
    val dst = "xorshift64"
    val dstDir = "test_run_dir/" + dst

    for (bits <- 1 until 32) {
      Driver.execute(defaultArgs ++
        Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
        () => new XorShift(XorShift64, BigInt(1), bits)) {
        c =>
          new XorShiftUnitTester(c) {
            reset()

            var rand = BigInt(1)
            for (i <- 0 until 1000) {
              rand = xorshift64(rand)
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

  behavior of "XorShift96"

  it should "updateがHighの時には毎サイクル乱数が生成される" in {
    val dst = "xorshift96"
    val dstDir = "test_run_dir/" + dst

    for (bits <- 1 until 32) {
      Driver.execute(defaultArgs ++
        Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
        () => new XorShift(XorShift64, BigInt(1), bits)) {
        c =>
          new XorShiftUnitTester(c) {
            reset()

            var rand = BigInt(1)
            for (i <- 0 until 1000) {
              rand = xorshift96(rand)
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
