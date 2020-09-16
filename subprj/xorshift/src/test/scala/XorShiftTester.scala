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
    y & BigInt(mask.substring(0, 7), 16)
  }

  /**
    * XorShift96の期待値生成
    * @param in_x シード1
    * @param in_y シード2
    * @param in_z シード3
    * @return
    */
  def xorshift96(in_x: BigInt, in_y: BigInt, in_z: BigInt): (BigInt, BigInt, BigInt) = {
    val mask = "f" * 8
    var x = (in_x << 3) & BigInt(mask, 16)
    var y = (in_y >> 19) & BigInt(mask, 16)
    var z = (in_z << 6) & BigInt(mask, 16)
    var t = (in_x ^ (x << 3)) ^ (in_y ^ (y >> 19)) ^ (in_z ^ (z << 6))

    x = y
    y = z
    z = t

    (x, y, z)
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

    for (bits <- 32 until 33) {
      Driver.execute(defaultArgs ++
        Array(s"--target-dir=$dstDir", s"--top-name=$dst"),
        () => new XorShift(XorShift64, BigInt(1), bits)) {
        c =>
          new XorShiftUnitTester(c) {
            reset()

            var rand_x = BigInt(1)
            var rand_y = BigInt(0)
            var rand_z = BigInt(0)
            for (i <- 0 until 10) {
              val randOrg = xorshift96(rand_x, rand_y, rand_z)
              val (rand_xx, rand_yy, rand_zz) = randOrg
              val rand = rand_z
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
